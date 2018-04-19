package com.lhxia.tancard;

import android.view.View;

/**
 * Created by lhxia on 2018/1/17.
 */

public class MoveInfo {

    private int width;
    private int height;
    private int top;
    private int left;
    private float rotation;

    public static class Builder {

        private MoveInfo moveinfo;

        public Builder() {
            moveinfo = new MoveInfo();
        }

        public Builder setWidth(int width){
            moveinfo.width = width;
            return this;
        }

        public Builder setHeight(int height){
            moveinfo.height = height;
            return this;
        }

        public Builder setTop(int top){
            moveinfo.top = top;
            return this;
        }

        public Builder setLeft(int left){
            moveinfo.left = left;
            return this;
        }

        public Builder setRotation(float rotation){
            moveinfo.rotation = rotation;
            return this;
        }

        public int getWidth(){
            return moveinfo.width;
        }

        public Builder setupByView(View view){
            moveinfo.width = view.getWidth();
            moveinfo.height = view.getHeight();
            moveinfo.top = view.getTop();
            moveinfo.left = view.getLeft();
            moveinfo.rotation = view.getRotation();
            return this;
        }

        public MoveInfo build(){
            return moveinfo;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "moveinfo=" + moveinfo +
                    '}';
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTop() {
        return top;
    }

    public int getLeft() {
        return left;
    }

    public float getRotation() {
        return rotation;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "MoveInfo{" +
                "width=" + width +
                ", height=" + height +
                ", top=" + top +
                ", left=" + left +
                '}';
    }
}
