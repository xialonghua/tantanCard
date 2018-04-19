package com.lhxia.tancard;

public interface CardEventListener {

    boolean swipeContinue(float distanceX);

    void discarded(boolean left);

    void topCardTapped();
}