package com.tmk.facedetection;

import android.graphics.Bitmap;

/**
 * Created by trietias on 3/7/18.
 */

class Sprite {
    int x;
    int y;
    int directionX = 1;
    int directionY = 1;
    int speed = 100;
    Bitmap image;

    public Sprite(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Sprite(int x, int y, Bitmap image) {
        this(x, y);
        this.image = image;
    }
}
