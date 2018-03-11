package com.tmk.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
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
    private Sprite[] bgs;
    private Bitmap[] charSprites;
    private int screenWidth;
    private int screenHeight;
    public int blinks;
    public int smile;

    // game variables
    private int jumpSpeed = 20;
    private int fallSpeed = 15;
    private int jumpHeight = 200;
    private int startHeight = 500;
    private int charState = 0; // 0 = standing, 1 = jumping, 2 = falling
    private int attackState = 0; // 0 = standing, 1 = jumping, 2 = falling
    private int breath = 100;

    // sprite variables
    private long lastFrameChangeTime = 0;
    private int frameLengthInMilliseconds = 100;
    private int currentFrame = 0;
    // How many frames are there on the sprite sheet?
    private int frameCount = 2;

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

        setUpSprites();
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

        setUpSprites();
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

        setUpSprites();
    }

    private void setUpSprites() {
        sprites = new Sprite[] {
                new Sprite (100, startHeight, BitmapFactory.decodeResource(this.getResources(), R.drawable.char0)),
                new Sprite(600, startHeight, BitmapFactory.decodeResource(this.getResources(), R.drawable.yoshi))
        };

        bgs = new Sprite[] {
                new Sprite(0,0,BitmapFactory.decodeResource(this.getResources(), R.drawable.bg)),
                new Sprite(screenWidth,0,BitmapFactory.decodeResource(this.getResources(), R.drawable.bg))
        };

        charSprites = new Bitmap[] {
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char2),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char1),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char3)
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
        checkFrame();
        checkBlinks();
        checkSmile();
        bgs[0].x -= 10;
        bgs[1].x = bgs[0].x + screenWidth;
        if (bgs[0].x < (0 - screenWidth)) {
            bgs[0].x = 0;
            bgs[1].x = bgs[0].x + screenWidth;
        }
    }

    protected void checkBlinks() {
        switch (charState)
        {
            case 0:
                if(blinks > 0) {
                    charState = 1;
                } else {
                    sprites[0].image = charSprites[currentFrame];
                }
                break;
            case 1:
                sprites[0].image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                if (sprites[0].y > jumpHeight) {
                    sprites[0].y -= jumpSpeed;
                } else {
                    charState = 2;
                }
                break;
            case 2:
                sprites[0].image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                if(sprites[0].y < startHeight) {
                    sprites[0].y += fallSpeed;
                } else {
                    charState = 0;
                    blinks = 0;
                }
                break;
        }
    }

    protected void checkSmile() {
        switch (attackState) {
            case 0:
                if (smile > 0) {
                    attackState = 1;
                }
                break;
            case 1:
                if (sprites[1].y > jumpHeight) {
                    sprites[1].y -= jumpSpeed;
                } else {
                    attackState = 2;
                }
                break;
            case 2:
                if (sprites[1].y < startHeight) {
                    sprites[1].y += fallSpeed;
                } else {
                    attackState = 0;
                    smile = 0;
                }
                break;
        }
    }
    protected void checkFrame() {
        long time  = System.currentTimeMillis();
        if(charState == 0) {// Only animate if bob is moving
            if ( time > lastFrameChangeTime + frameLengthInMilliseconds) {
                lastFrameChangeTime = time;
                currentFrame ++;
                if (currentFrame >= frameCount) {
                    currentFrame = 0;
                }
            }
        }
    }

    protected void render(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (int index = 0, length = bgs.length; index < length; index++) {
            canvas.drawBitmap(bgs[index].image, null, new RectF(bgs[index].x,bgs[index].y, bgs[index].x + screenWidth, bgs[index].y + screenHeight),null);
        }

        for (int index = 0, length = sprites.length; index < length; index++) {
            canvas.drawBitmap(sprites[index].image, sprites[index].x, sprites[index].y, null);
        }

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
