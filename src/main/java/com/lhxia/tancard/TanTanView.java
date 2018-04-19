package com.lhxia.tancard;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class TanTanView extends FrameLayout {

    private TanCard tanCard;
    private CardDiscardContainer cardDiscardContainer;

    public TanTanView(Context context) {
        super(context);
        init(context, null);
    }

    public TanTanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TanTanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){

        tanCard = new TanCard(context);
        cardDiscardContainer = new CardDiscardContainer(context, attrs);
        tanCard.setCardDiscardContainer(cardDiscardContainer);
        addView(tanCard, -1, -1);
        addView(cardDiscardContainer, -1, -1);

        setClipChildren(false);
        setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(false);
        }
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.attr_tantan_view);


            tanCard.setVisibleCount(array.getInteger(R.styleable.attr_tantan_view_visibleCount, 4));

            tanCard.setVerticleGap(array.getDimensionPixelSize(R.styleable.attr_tantan_view_verticalGap, 30));
            tanCard.setHorizontalGap(array.getDimensionPixelSize(R.styleable.attr_tantan_view_horizontalGap, 30));

            array.recycle();
        }
    }

    public BaseAdapter getAdapter() {
        return tanCard.getAdapter();
    }

    public View getTopView() {
        return tanCard.getTopView();
    }

    public void setAdapter(BaseAdapter adapter){
        tanCard.setAdapter(adapter);
    }

    public void setCardEventListener(CardEventListener cardEventListener){
        tanCard.setCardEventListener(cardEventListener);
    }

    public void discardHorizontal(final boolean left, boolean fromUser){
        tanCard.discardHorizontal(left, fromUser);
    }
}
