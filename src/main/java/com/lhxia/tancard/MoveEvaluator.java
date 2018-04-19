package com.lhxia.tancard;

import android.animation.TypeEvaluator;

import com.lhxia.tancard.MoveInfo;

/**
 * Created by lhxia on 2018/1/29.
 */
public class MoveEvaluator implements TypeEvaluator<MoveInfo> {

    @Override
    public MoveInfo evaluate(float fraction, MoveInfo startValue, MoveInfo moveInfo) {
        MoveInfo.Builder builder = new MoveInfo.Builder();
        MoveInfo mi = builder
                .setLeft((int) (startValue.getLeft() + (moveInfo.getLeft() - startValue.getLeft()) * fraction))
                .setRotation((1 - fraction) * startValue.getRotation())
                .setTop((int) (startValue.getTop() + (moveInfo.getTop() - startValue.getTop()) * fraction))
                .setWidth((int) (startValue.getWidth() + (moveInfo.getWidth() - startValue.getWidth()) * fraction))
                .setHeight((int) (1f * builder.getWidth() * moveInfo.getHeight() / moveInfo.getWidth()))
                .build();
        return mi;
    }
}
