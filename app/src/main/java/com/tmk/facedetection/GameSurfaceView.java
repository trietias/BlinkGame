package com.tmk.facedetection;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by trietias on 3/7/18.
 */

public class GameSurfaceView extends SurfaceView implements Runnable{
    private boolean isRunning = false;
    private Thread gameThread;
    private SurfaceHolder holder;
    private final static int MAX_FPS = 30;
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;
    private Sprite[] sprites;
    private int screenWidth;
    private int screenHeight;

    public GameSurfaceView(Context context){
        super(context);

        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        sprites = new Sprite[] {
                new Sprite (100, 100, BitmapFactory.decodeResource(this.getResources(), R.drawable.kprsticker))
        };
    }

    public GameSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        sprites = new Sprite[] {
                new Sprite (100, 100, BitmapFactory.decodeResource(this.getResources(), R.drawable.kprsticker))
        };
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        sprites = new Sprite[] {
                new Sprite (100, 100, BitmapFactory.decodeResource(this.getResources(), R.drawable.kprsticker))
        };
    }

    @Override
    public void run() {
        while(isRunning) {

            if(! holder.getSurface().isValid()) {
                continue;
            }

            long started = System.currentTimeMillis();

            // update
            step();

            // draw
            Canvas canvas = holder.lockCanvas();
            if(canvas != null) {
                render(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
            float deltaTime = (System.currentTimeMillis() - started);

            int sleepTime = (int) (FRAME_PERIOD - deltaTime);
            if(sleepTime > 0) {
                try {
                    gameThread.sleep(sleepTime);
                } catch (InterruptedException e) {

                }
            }

            while (sleepTime < 0) {
                sleepTime += FRAME_PERIOD;
            }
        }

    }

    protected void step() {
        for (int index = 0, length = sprites.length; index < length; index++) {
            Sprite sprite = sprites[index];

            if ((sprite.x < 0) || ((sprite.x + sprite.image.getWidth()) > screenWidth)) {
                sprite.directionX *= -1;
            }
            if ((sprite.y < 0) || ((sprite.y + sprite.image.getHeight()) > screenHeight)) {
                sprite.directionY *= -1;
            }

            sprite.x += (sprite.directionX * sprite.speed);
            sprite.y += (sprite.directionY * sprite.speed);
        }
    }

    protected void render(Canvas canvas) {
        int x = 100;
        int y = 100;
        Sprite sprite = new Sprite(x,y);
        sprite.image = BitmapFactory.decodeResource(this.getResources(),R.drawable.kprsticker);

        canvas.drawBitmap(sprite.image, sprite.x, sprite.y, null);
    }


    public void resume() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
    }
}
