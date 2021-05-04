package CameraApp;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.saveandroid.R;

import static android.content.Context.WINDOW_SERVICE;

interface ICustomBubbleListener {
    public void onFloatingIconClicked(View v);
}

public class FloatingIcon {
    private final View csView;
    private final WindowManager windowManager;
    private final ICustomBubbleListener listener;
    private final Context context;

    public FloatingIcon(Context context, ICustomBubbleListener listener)
    {
        this.context = context;
        this.listener = listener;
        csView = LayoutInflater.from(context).inflate(R.layout.floating_icon, null);
        WindowManager.LayoutParams lParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.OPAQUE);

        lParams.gravity = Gravity.TOP | Gravity.LEFT;
        lParams.x = 30;
        lParams.y = 200;

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            private int initX;
            private int initY;
            private float touchX;
            private float touchY;
            private int lastAction;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initX = lParams.x;
                    initY = lParams.y;

                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if(lastAction == MotionEvent.ACTION_DOWN)
                    {
                        listener.onFloatingIconClicked(v);
                    }
                    lastAction = MotionEvent.ACTION_UP;
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    lParams.x=initX + (int) (event.getRawX() - touchX);
                    lParams.y=initY + (int) (event.getRawY() - touchY);
                    windowManager.updateViewLayout(csView, lParams);

                    lastAction = MotionEvent.ACTION_MOVE;
                    return true;
                }

                return false;
            }
        };

        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        windowManager.addView(csView, lParams);

        ImageView cs_Image = csView.findViewById(R.id.csbubbleimg);
        cs_Image.setOnTouchListener(touchListener);
    }

    public void Remove() {
        windowManager.removeView(csView);
    }
}
