package com.hanshow.sdk.widgets;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by mfw on 2018/8/5
 * 支持EditText中内容上下滑动，解决与ScrollView滑动冲突的问题
 */
public class ScrollEditText extends EditText {

    private final int MOVE_SLOP = 20; //移动距离临界

    //滑动距离的最大边界
    private int mOffsetHeight;

    //是否到顶或者到底的标志
    private boolean mBottomFlag = false;
    private boolean isCanScroll = false;//标记内容是否触发了滚动
    private float lastY = 0;

    public ScrollEditText(Context context) {
        this(context,null);
    }

    public ScrollEditText(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public ScrollEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int paddingTop;
        int paddingBottom;
        int mHeight;
        int mLayoutHeight;

        //获得内容面板
        Layout mLayout = getLayout();
        mLayoutHeight = mLayout.getHeight();

        paddingTop = getTotalPaddingTop();
        paddingBottom = getTotalPaddingBottom();

        //获得控件的实际高度
        mHeight = getHeight();

        //计算滑动距离的边界(H_content - H_view = H_scroll)
        mOffsetHeight = mLayoutHeight + paddingTop + paddingBottom - mHeight;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            //手指按下事件，重置状态
            mBottomFlag = false;
            isCanScroll = false;
            lastY=0;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        //如果是需要拦截，则再拦截，这个方法会在onScrollChanged方法之后再调用一次
        if (!mBottomFlag)
            getParent().requestDisallowInterceptTouchEvent(true);
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (lastY == 0){
                lastY = event.getRawY();
            }
            //条件：手指move了一段距离，但是onScrollChanged函数未调用，说明文字无法滚动了，则将触摸处理权交还给ParentView
            if (Math.abs(lastY - event.getRawY()) > MOVE_SLOP){
                if (!isCanScroll){
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            //Log.d("Javine","ActionMove: "+ lastY + "," + event.getRawY());
        }
        return result;
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        isCanScroll = true;
        //Log.d("Javine","onScrolled "+vert);
        if (vert == mOffsetHeight || vert == 0) {
            //这里将处理权交还给父控件
            getParent().requestDisallowInterceptTouchEvent(false);
            mBottomFlag = true;
        }
    }
}
