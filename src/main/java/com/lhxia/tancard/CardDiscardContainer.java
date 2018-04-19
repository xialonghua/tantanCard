package com.lhxia.tancard;

import android.content.Context;
import android.os.Build;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by lhxia on 2018/1/23.
 */

public class CardDiscardContainer extends ViewGroup {
    public CardDiscardContainer(Context context) {
        super(context);
    }

    public CardDiscardContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CardDiscardContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private void init(){
        setClipChildren(false);
        setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(false);
        }


    }
}
