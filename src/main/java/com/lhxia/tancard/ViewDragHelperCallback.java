package com.lhxia.tancard;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;

public class ViewDragHelperCallback extends ViewDragHelper.Callback {

    private TanCard tanCard;

    public ViewDragHelperCallback(TanCard tanCard) {
        this.tanCard = tanCard;
    }

    @Override
    public boolean tryCaptureView(View child, int pointerId) {
        //如果是顶部的item则允许拖动
        if (child == tanCard.getTopView()){
            //取消顶部item的动画，让ViewDragHelper来接手
            tanCard.cancelTopViewAnim();
            return true;
        }
        return false;
    }

    @Override
    public int clampViewPositionHorizontal(View child, int left, int dx) {
        return left;
    }

    @Override
    public int getViewHorizontalDragRange(View child) {
        return tanCard.getMeasuredWidth();
    }

    @Override
    public int getViewVerticalDragRange(View child) {
        return tanCard.getMeasuredHeight();
    }

    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        super.onViewPositionChanged(changedView, left, top, dx, dy);
        //不断刷新所有的view
        tanCard.invalidateTopViewMove(changedView);
    }

    @Override
    public int clampViewPositionVertical(View child, int top, int dy) {
        return top;
    }


    @Override
    public void onViewCaptured(View capturedChild, int activePointerId) {
        super.onViewCaptured(capturedChild, activePointerId);
    }

    @Override
    public void onViewReleased(View releasedChild, float xvel, float yvel) {
        super.onViewReleased(releasedChild, xvel, yvel);
        //横向速度大于400DIP则认为删除
        if (Math.abs(xvel) > 800/*DimensionsKt.dip(CardStack.this, 400)*/){
            tanCard.discardHorizontal(xvel < 0, false);
        }else {
            //移动超过一定数字则认为删除
            MoveInfo moveInfo = tanCard.getTopViewPresetMoveInfo();
            if (Math.abs(releasedChild.getLeft()) > ((moveInfo.getLeft() + moveInfo.getWidth()) * 2 / 7)){
                tanCard.discardHorizontal(releasedChild.getLeft() < 0, false);
            }else {
                //否则所有item归位
                tanCard.restoreChildView();
            }
        }
    }
}
