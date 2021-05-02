package CameraApp;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.QuickContactBadge;

import com.example.saveandroid.R;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WINDOW_SERVICE;


public class CustomDialogBox {
    private final View csView;
    private final WindowManager windowManager;
    private final LocationTrackerService context;
    private Timer closingTimer = null;

    public CustomDialogBox(LocationTrackerService context) {
        this.context = context;
        csView = LayoutInflater.from(context).inflate(R.layout.crashservice_dialog, null);
        WindowManager.LayoutParams lParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        lParams.gravity = Gravity.TOP | Gravity.LEFT;
        lParams.x = 100;
        lParams.y = 300;

        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        windowManager.addView(csView, lParams);

        if (closingTimer == null) {
            closingTimer = new Timer();
            closingTimer.scheduleAtFixedRate(new DialogCloser(), 1000, 1000);
        }

        Button btnYes = csView.findViewById(R.id.btnYes);
        Button btnNo = csView.findViewById(R.id.btnNo);

        View.OnClickListener clickListener=new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                context.onFloatingIconClicked(view);
            }
        };
        btnYes.setOnClickListener(clickListener);
        btnNo.setOnClickListener(clickListener);
    }

    public void Remove() {
        if (closingTimer != null) {
            closingTimer.cancel();
            closingTimer = null;
        }
        windowManager.removeView(csView);
    }

    private class DialogCloser extends TimerTask {
        int countDown;

        public DialogCloser() {
            countDown = 5;
        }

        @Override
        public void run() {
            countDown--;
            if (countDown <= 0) {
                context.dlgTimeOut();
            }
        }
    }
}
