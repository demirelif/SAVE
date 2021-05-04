package CameraApp;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.saveandroid.R;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.WINDOW_SERVICE;


enum DialogType {
    Fatigue,
    Collision,
    Anger,
    AbnormalBPM
}

interface ICustomDialogListener {
    public void onClick(View view);

    void dlgTimeOut();
}

public class CustomDialogBox {
    private final View csView;
    private final WindowManager windowManager;
    private final Context context;
    private final ICustomDialogListener listener;
    private final DialogType type;
    private final int autoCloseTimeInSec;
    private Timer closingTimer = null;

    public CustomDialogBox(Context context, ICustomDialogListener listener, DialogType type, int autoCloseTimeInSec) {
        this.context = context;
        this.listener = listener;
        this.type = type;
        this.autoCloseTimeInSec = autoCloseTimeInSec;
        csView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null);

        int height;
        if (type == DialogType.AbnormalBPM)
            height = 220;
        else
            height = 200;

        float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (height * scale + 0.5f);

        WindowManager.LayoutParams lParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                pixels,
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
            if (autoCloseTimeInSec > 0)
                closingTimer.scheduleAtFixedRate(new DialogCloser(), 1000, 1000);
        }

        TextView tvCDHeader = csView.findViewById(R.id.tvCDHeader);
        TextView tvCDText1 = csView.findViewById(R.id.tvCDText1);
        TextView tvCDText2 = csView.findViewById(R.id.tvCDText2);
        Button btnYes = csView.findViewById(R.id.btnYes);
        Button btnNo = csView.findViewById(R.id.btnNo);

        if (type == DialogType.Fatigue) {
            tvCDHeader.setText("Fatigue Detected!");
            tvCDText1.setText("Please consider resting until you feel recharged.");
            tvCDText2.setText("Do you want us to take you to near resting place?");
            btnYes.setText("Yes");
            btnNo.setText("No");
        } else if (type == DialogType.Collision) {
            tvCDHeader.setText("Collision Detected!");
            tvCDText1.setText("We sensed a concerning change of speed tell us if you are okay.");
            tvCDText2.setText("If no input received for a time we will inform emergency contact.");
            btnYes.setText("Inform Emergency Contact");
            btnNo.setText("I'm Okay");
        } else if (type == DialogType.Anger) {
            tvCDHeader.setText("Anger Detected!");
            tvCDText1.setText("We sensed anger.");
            tvCDText2.setText("Would you like to play some calming music?");
            btnYes.setText("Yes");
            btnNo.setText("No");
        } else if (type == DialogType.AbnormalBPM) {
            tvCDHeader.setText("Abnormal BPM Detected!");
            tvCDText1.setText("We sensed abnormality on heart rate. Please tell us if you are okay.");
            tvCDText2.setText("If no input received for a time we will inform emergency contact.");
            btnYes.setText("Inform Emergency Contact");
            btnNo.setText("I'm Okay");
        }

        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                listener.onClick(view);
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
            countDown = autoCloseTimeInSec;
        }

        @Override
        public void run() {
            if (autoCloseTimeInSec > 0) {
                countDown--;
                if (countDown <= 0) {
                    listener.dlgTimeOut();
                }
            }
        }
    }
}
