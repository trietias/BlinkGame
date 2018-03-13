package com.tmk.facedetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by trietias on 3/7/18.
 */

class Cannon {
    public int x;
    public int y;
    public int speed;
    Bitmap image;

    public Cannon(int x, int y, Bitmap image, int speed) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.speed = speed;
    }
}