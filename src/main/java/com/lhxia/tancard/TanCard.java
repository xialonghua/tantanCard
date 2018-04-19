package com.lhxia.tancard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;


public class TanCard extends ViewGroup {
    private int mNumVisible = 4;
    private BaseAdapter mAdapter;

    private CardDiscardContainer cardDiscardContainer;

    private ViewDragHelper viewDragHelper;

    private int cardGap = 30;
    private int scaleGap = 30;

    private float downX = -1, downY = -1;
    private float lastX;

    private List<MoveInfo> presetMoveInfoMap = new ArrayList<>();

    private CardEventListener cardEventListener;


    public void setCardEventListener(CardEventListener cardEventListener) {
        this.cardEventListener = cardEventListener;
    }

    private void removeChildView(View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
        }
    }

    public void discardHorizontal(final boolean left, boolean fromUser) {
        final View topView = getTopView();
        if (topView == null){
            return ;
        }

        Log.d("", "discardHorizontal " + left);
        topView.setOnClickListener(null);
        removeChildView(topView);

        //将删除的item加到cardDiscardSwimPool里继续进行离开动画
        cardDiscardContainer.addView(topView, 0);

        measureChildSize(topView);

        MoveInfo topViewMoveInfo = new MoveInfo.Builder().setupByView(topView).build();
        MoveInfo endMoveInfo = new MoveInfo.Builder().setupByView(topView).build();

        if (fromUser){
            endMoveInfo.setLeft(topView.getLeft() + (left ? -1 : 1) * getMeasuredWidth()/*DimensionsKt.dip(this, CommonUtilKt.getScreenWidth(getContext()))*/);
            endMoveInfo.setTop(topView.getTop() + getMeasuredHeight() / 2/*DimensionsKt.dip(this, CommonUtilKt.getScreenHeight(getContext())) / 2*/);
        }else {
            endMoveInfo.setLeft(topView.getLeft() + getPositiveOrNegative(topView.getLeft()) * getMeasuredWidth()/*DimensionsKt.dip(this, CommonUtilKt.getScreenWidth(getContext()))*/);
            endMoveInfo.setTop(topView.getTop() + getPositiveOrNegative(topView.getTop()) * getMeasuredHeight() / 2);
        }

        ValueAnimator animator = ValueAnimator.ofObject(new MoveEvaluator(), topViewMoveInfo, endMoveInfo);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                MoveInfo info = (MoveInfo) animation.getAnimatedValue();
                topView.setLeft(info.getLeft());
                topView.setRight(info.getLeft() + info.getWidth());

                topView.setTop(info.getTop());
                topView.setBottom(info.getTop() + info.getHeight());

//                topView.setAlpha(1 * animation.getAnimatedFraction());
                measureChildSize(topView);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeChildView(topView);
            }
        });
        animator.setDuration(1000);

        //是滑动删除还是用户端按钮删除
        if (fromUser){
            animator.setInterpolator(new AnticipateOvershootInterpolator(0.5f));
        }else {
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
        }

        animator.start();

        if (cardEventListener != null){
            cardEventListener.discarded(left);
        }
        restoreChildView();
    }

    private int getPositiveOrNegative(float num){
        return num > 0 ? 1 : -1;
    }


    public TanCard(Context context) {
        super(context);
        init(null);
    }

    /**
     * 计算最大的那个item的可以占用的大小及位置
     */
    private void createPresetMoveInfo(){
        //确保只执行一次
        if (!presetMoveInfoMap.isEmpty()){
            return;
        }

        //计算item最大占用高度，因为是有错开效果，所以item高度要排除错开的高度
        int itemHeight = getExactlyHeight() - cardGap * (mNumVisible - 1);

        //生成每个位置的MoveInfo
        for (int i = 0; i < mNumVisible;i++){

            MoveInfo.Builder builder = new MoveInfo.Builder();


            int index = i;
            if (i == 0){
                //最底下一个item应该和倒数第二个一样大
                index = 1;
            }

            //左右间距根据index递增越来越小
            int gap = ((mNumVisible - index - 1) * scaleGap);
            //左右2边
            int doubleGap = gap * 2;

            //根据index设置位置，这样就达到依次错开效果
            builder.setTop((index * cardGap) + getPaddingTop());
            builder.setLeft(getPaddingLeft() + gap);

            //根据index设置宽度，高度等比缩放，这样就达到依次变小的效果
            builder.setWidth(getExactlyWidth() - doubleGap);
            builder.setHeight((int) (1f * builder.getWidth() * itemHeight / getExactlyWidth()));
            presetMoveInfoMap.add(builder.build());
        }
    }

    private void init(AttributeSet attrs){
        setClipChildren(false);
        setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(false);
        }

        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelperCallback(this));


    }

    private DataSetObserver mOb = new DataSetObserver() {
        @Override
        public void onChanged() {
            loadData();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            removeAllViews();
            loadData();
        }
    };

    //ArrayList
    public void setAdapter(final BaseAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mOb);
        }
        mAdapter = adapter;
        adapter.registerDataSetObserver(mOb);
        adapter.notifyDataSetChanged();
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public View getTopView() {
        if (getChildCount() == 0){
            return null;
        }
        return getChildAt(getChildCount() - 1);
    }

    //处理是否拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //由viewDragHelper 来判断是否应该拦截此事件
        boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
        if (result){
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.d("", "onlayout ");
//        restoreChildView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //记录按下及移动坐标
        if (event.getAction() == MotionEvent.ACTION_MOVE){

            lastX = event.getX();
            float lastY = event.getY();
            if (downY == -1){
                downY = lastY;
            }
            if (downX == -1){
                downX = lastX;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            downX = -1;
            downY = -1;
        }
        //将触摸事件传给viewDragHelper来解析处理
        viewDragHelper.processTouchEvent(event);

        //如果不是拖动状态向下传递事件
        if (viewDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE){
            return super.onTouchEvent(event);
        }

        return true;
    }


    public void loadData() {
        //如果没有数据则不再加载
        if (mAdapter.getCount() == 0){
            return;
        }

        //如果已经显示了mNumVisible个view则不再加载
        if (getChildCount() >= mNumVisible){
            return;
        }

        int adapterPosition = mAdapter.getCount() - getChildCount() - 1;
        //如果所有item已显示则不加载
        if (adapterPosition < 0){
            return;
        }

        //在view加载完成后，生成MoveInfo
        createPresetMoveInfo();

        //添加一个item
        final FrameLayout frameLayout = new FrameLayout(getContext());

        //初始位置，直接取最底下的item
        MoveInfo moveInfo = presetMoveInfoMap.get(0);
        final LayoutParams layoutParams = new LayoutParams(moveInfo.getWidth(), moveInfo.getHeight());

        //填入item数据
        View child = mAdapter.getView(adapterPosition, null, this);
        frameLayout.addView(child, -1, -1);

        //实际应该显示的MoveInfo
        int presetMoveInfoIndex = presetMoveInfoMap.size() - getChildCount() - 1;

        addView(frameLayout, 0, layoutParams);

        //进行layout
        frameLayout.measure(MeasureSpec.makeMeasureSpec(moveInfo.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(moveInfo.getHeight(), MeasureSpec.EXACTLY));
        frameLayout.layout(moveInfo.getLeft(), moveInfo.getTop(), moveInfo.getLeft() + moveInfo.getWidth(), moveInfo.getTop() + moveInfo.getHeight());

        //开始进入动画
        doEnterAnim(frameLayout, presetMoveInfoIndex);

        //设置顶部view的点击事件
        View topView = getTopView();
        if (topView != null){
            topView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (cardEventListener != null){
                        cardEventListener.topCardTapped();
                    }
                }
            });
        }
    }

    /**
     * 进入动画
     * @param view 进入的view
     * @param presetMoveInfoIndex
     */
    private void doEnterAnim(final View view, int presetMoveInfoIndex){

        MoveInfo moveInfo = presetMoveInfoMap.get(presetMoveInfoIndex);

        MoveInfo currentMoveInfo = new MoveInfo.Builder().setupByView(view).build();

        ValueAnimator discardAnim = ValueAnimator.ofObject(new MoveEvaluator(), currentMoveInfo, moveInfo);

        discardAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                MoveInfo info = (MoveInfo) animation.getAnimatedValue();
                view.setLeft(info.getLeft());
                view.setRight(info.getLeft() + info.getWidth());

                view.setTop(info.getTop());
                view.setBottom(info.getTop() + info.getHeight());

                measureChildSize(view);
            }
        });
        discardAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                measureChildSize(view);
                loadData();
            }
        });
        discardAnim.setInterpolator(new OvershootInterpolator());
        discardAnim.setDuration(300);
        discardAnim.start();
        cancelViewAnim(view);
        view.setTag(R.id.view_animator, discardAnim);
    }


    MoveInfo getTopViewPresetMoveInfo(){
        return presetMoveInfoMap.get(presetMoveInfoMap.size() - 1);
    }

    private float computeRotation(float rotation, float maxRotation){
        if (Math.abs(rotation) > maxRotation) {
            if (rotation > 0) {
                rotation = maxRotation;
            } else {
                rotation = -maxRotation;
            }
        }

        return rotation;
    }

    void invalidateTopViewMove(View changedView){

        //拿到顶部item的初始MoveInfo
        MoveInfo topViewPresetMoveInfo = getTopViewPresetMoveInfo();


        int currentLeft = changedView.getLeft();
        int currentTop = changedView.getTop();

        double topViewPresetDistance = Math.sqrt(topViewPresetMoveInfo.getWidth() * topViewPresetMoveInfo.getWidth()
                + topViewPresetMoveInfo.getHeight() * topViewPresetMoveInfo.getHeight()) / 3;

        int leftDelta = topViewPresetMoveInfo.getLeft() - currentLeft;
        int topDelta = topViewPresetMoveInfo.getTop() - currentTop;
        double distance = Math.sqrt(leftDelta * leftDelta + topDelta * topDelta);

        double fraction = distance / topViewPresetDistance;

        //fraction反正经过前面一大段计算得出移动的百分比然后归1.用作其他view的联动系数
        if (fraction > 1.0){
            fraction = 1.0;
        }

        if (cardEventListener != null){
            //回调移动，可以在item里做文章，比如随着手指移动item上显示喜欢或不喜欢
            cardEventListener.swipeContinue(currentLeft - topViewPresetMoveInfo.getLeft());
        }

        //计算旋转角度，随着手指触摸而略微旋转
        float maxRotation = 15;
        float halfRotation = 7;

        float rx = lastX - downX;

        float rotation = 0;
//        rotation = computeRotation(rotation, maxRotation);

        if (downX == -1){
            rotation = 0;
        }else {
            //根据移动X的范围与view的高度的夹角/|\来控制旋转度数
            rotation = (float) (180f / PI * Math.atan(rx / topViewPresetMoveInfo.getHeight()));
        }

        float delta = downY - topViewPresetMoveInfo.getTop();
        float h = topViewPresetMoveInfo.getHeight() / 4;

        //我观察探探，触摸按下的位置在item高度上不同，旋转角度大小不同。
        if (delta >= topViewPresetMoveInfo.getHeight() - h){
            changedView.setRotation(computeRotation(rotation, maxRotation));
        }else if (delta >= topViewPresetMoveInfo.getHeight() / 2 && delta < topViewPresetMoveInfo.getHeight() - h){
            changedView.setRotation(computeRotation(rotation, halfRotation));
        }else if (delta < topViewPresetMoveInfo.getHeight() / 2 && delta > h){
            changedView.setRotation(computeRotation(rotation, halfRotation));
        }else {
            changedView.setRotation(computeRotation(rotation, maxRotation));
        }

        //根据fraction计算每个item的位置，
        for(int i = getChildCount() - 1, j = mNumVisible - 1; i >= 0; i--, j--){
            View view = getChildAt(i);
            if (view == changedView){
                //排除顶部的item
                continue;
            }
            if (i == 0 && getChildCount() == mNumVisible){
                //排除最底部的item
                continue;
            }
            //探探看上去是一个向前平移加放大的效果
            MoveInfo currentMoveInfo = presetMoveInfoMap.get(j);
            //因此只需要根据fraction计算view的初始位置到下一个item的初始位置的中间值
            int t = (int) (cardGap * fraction);
            int s = (int) (scaleGap * fraction);

            view.setLeft(currentMoveInfo.getLeft() - s);
            view.setRight(view.getLeft() + currentMoveInfo.getWidth() + s * 2);

            int height = view.getWidth() * currentMoveInfo.getHeight() / currentMoveInfo.getWidth();
//
            view.setTop(currentMoveInfo.getTop() + t);
            view.setBottom(view.getTop() + height);

            measureChildSize(view);
        }
    }

    /**
     * 测量view的大小，因为只是设置left,top,right,bottom。
     * item渲染出来的大小并不是实际大小还是之前大小
     * 因此每次需要从新测量一下
     * @param view
     */
    private void measureChildSize(View view){
        view.measure(MeasureSpec.makeMeasureSpec(view.getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(view.getHeight(), MeasureSpec.EXACTLY));
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    void cancelTopViewAnim(){
        View topView = getTopView();
        cancelViewAnim(topView);
    }

    private void cancelViewAnim(View view){
        if (view == null){
            return;
        }
        Animator animator = (Animator) view.getTag(R.id.view_animator);
        if (animator != null){
            animator.cancel();
            loadData();
        }
        view.setTag(R.id.view_animator, null);
    }

    void restoreChildView(){
        //取消顶部view的动画
        cancelTopViewAnim();

        AnimatorSet animatorSet = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();

        //加载数据
        loadData();

        //计算每个view当前位置及初始位置进行动画归位
        for (int i = getChildCount() - 1, j = presetMoveInfoMap.size() - 1; i >= 0;i--, j--){

            final View view = getChildAt(i);

            final MoveInfo moveInfo = presetMoveInfoMap.get(j);

            MoveInfo currentMoveInfo = new MoveInfo.Builder().setupByView(view).build();

            ValueAnimator anim = ValueAnimator.ofObject(new MoveEvaluator(), currentMoveInfo, moveInfo);

            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    MoveInfo info = (MoveInfo) animation.getAnimatedValue();
                    view.setLeft(info.getLeft());
                    view.setRight(info.getLeft() + info.getWidth());

                    view.setTop(info.getTop());
                    view.setBottom(info.getTop() + info.getHeight());

                    view.setRotation(info.getRotation());

                    measureChildSize(view);

                    if (view == getTopView()){
                        if (cardEventListener != null){
                            cardEventListener.swipeContinue(view.getLeft() - moveInfo.getLeft());
                        }
                    }
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    measureChildSize(view);
                    if (view == getTopView()){
                        if (cardEventListener != null){
                            cardEventListener.swipeContinue(view.getLeft() - moveInfo.getLeft());
                        }
                    }
                }
            });
            if (getTopView() == view){
                //如果顶部的item则有一个回弹动画
                anim.setInterpolator(new OvershootInterpolator());
                anim.setDuration(700);
            }else {
                anim.setDuration(300);
            }
            animators.add(anim);
            cancelViewAnim(view);
            view.setTag(R.id.view_animator, animatorSet);
        }

        animatorSet.playTogether(animators);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loadData();
            }

        });
        animatorSet.start();
    }

    /**
     * 获取view的内容宽度
     * @return
     */
    private int getExactlyWidth(){
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
    /**
     * 获取view的内容高度
     * @return
     */
    private int getExactlyHeight(){
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }


    public void setCardDiscardContainer(CardDiscardContainer cardDiscardContainer) {
        this.cardDiscardContainer = cardDiscardContainer;
    }

    public void setVerticleGap(int cardGap){
        this.cardGap = cardGap;
    }
    public void setHorizontalGap(int scaleGap){
        this.scaleGap = scaleGap;
    }
    public void setVisibleCount(int count){
        this.mNumVisible = count;
    }
}
