package com.yinyoupoet.flags;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;


/**
 * Created by hasee on 2018/2/25.
 */

public class DragView extends LinearLayout {
    String TAG = "DragView";
    float moveX;
    float moveY;

    public DragView(Context context){
        super(context);
    }
    public DragView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                moveX = event.getX();
                moveY = event.getY();
                Log.d(TAG, "onTouchEvent: Down");
                break;
            case MotionEvent.ACTION_MOVE:
                //setTranslationX(getX() + (event.getX() - moveX));
                //setTranslationY(getY() + (event.getY() - moveY));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(this.getLayoutParams());
                lp.setMargins(0,(int)(event.getY() - moveY),0,0);
                this.setLayoutParams(lp);
                Log.d(TAG, "onTouchEvent: Move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: Up");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "onTouchEvent: Cancel");
                break;
        }

        return true;
    }
}
