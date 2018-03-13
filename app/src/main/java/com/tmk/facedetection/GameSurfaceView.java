package com.tmk.facedetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by trietias on 3/7/18.
 */

public class GameSurfaceView extends SurfaceView implements Runnable {
    private final String TAG = "GameSurfaceView";
    private boolean isRunning = false;
    private Thread gameThread;
    private SurfaceHolder holder;
    private final static int MAX_FPS = 30;
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;
    private Sprite player;
    private Sprite playerLife;
    private Bitmap[] playerLifeArray;
    private Cannon[] cannons;
    private Bitmap[] charSprites;
    private int life = 3;
    private int screenWidth;
    private int screenHeight;
    private int charWidth;
    private int charHeight;
    public int blinks;
    public int smile;
    public int distance;
    Paint textPaint;

    // game variables
    private int jumpSpeed = 30;
    private int fallSpeed = 15;
    private int jumpHeight = 200;
    private int startHeight;
    private int charState = 0; // 0 = standing, 1 = jumping, 2 = falling
    private int started = 0;

    // sprite variables
    private long lastFrameChangeTime = 0;
    private int frameLengthInMilliseconds = 100;
    private int currentFrame = 0;
    // How many frames are there on the sprite sheet?
    private int frameCount = 4;

    public GameSurfaceView(Context context){
        super(context);

        setHolder();
        setUpSprites();

    }

    public GameSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setHolder();
        setUpSprites();
    }

    private void setHolder() {
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                screenWidth = width;
                screenHeight = height;
                startHeight = (height * 3) / 5;
                charHeight = height / 5;
                charWidth = width / 10;
                fallSpeed = height / 20;
                jumpSpeed = height / 18;
                textPaint =  new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(42);
                textPaint.setStrokeWidth(30);
                textPaint.setTextAlign(Paint.Align.CENTER);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private void setUpSprites() {

        player = new Sprite(100, startHeight, BitmapFactory.decodeResource(this.getResources(), R.drawable.char0));

        charSprites = new Bitmap[] {
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char0),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char1),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char2),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char3),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char4),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.char5)
        };

        cannons = new Cannon[20];

        playerLifeArray = new Bitmap[] {
                BitmapFactory.decodeResource(this.getResources(), R.drawable.blank),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.onelife),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.twolives),
                BitmapFactory.decodeResource(this.getResources(), R.drawable.threelives)
        };

        playerLife = new Sprite(200,200, playerLifeArray[life-1]);
    }

    @Override
    public void run() {
        while(isRunning) {

            if(! holder.getSurface().isValid()) {
                continue;
            }

            long started = System.currentTimeMillis();

            // update
            if(life >= 0) {
                step();
            } else {

            }



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
        distance++;
        playerLife.image = playerLifeArray[life];
        for (int i = 0; i < cannons.length; i++) {
            if ((distance / 100) > i) {
                if (cannons[i] == null) {
                    cannons[i] = new Cannon((screenWidth), (int) (Math.random() * ((screenHeight * 3) / 4)), BitmapFactory.decodeResource(this.getResources(), R.drawable.cannon), 40 + (int)(Math.random() * (61)));
                    Log.d(TAG, "Cannon created");
                }
            }
        }
        if (started == 0) {
            if (startHeight == 0) {
                player.image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                player.y += fallSpeed;
            } else {
                player.image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                player.y += fallSpeed;
                if (player.y >= startHeight) {
                    player.image = BitmapFactory.decodeResource(getResources(), R.drawable.char0);
                    started = 1;
                }
            }
        } else {
            checkFrame();
            checkBlinks();
            shootCannons();
            checkCollision();
        }
    }

    protected void reveal() {
        new Handler(Looper.getMainLooper()).post(new Runnable () {
            @Override
            public void run () {
                holder.setFixedSize(0,0);
            }
        });

    }

    protected void checkBlinks() {
        switch (charState)
        {
            case 0:
                if(blinks > 0) {
                    charState = 1;
                } else {
                    player.image = charSprites[currentFrame];
                }
                break;
            case 1:
                player.image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                if (player.y > jumpHeight) {
                    player.y -= jumpSpeed;
                } else {
                    charState = 2;
                }
                break;
            case 2:
                player.image = BitmapFactory.decodeResource(getResources(), R.drawable.charjump);
                if(player.y < startHeight) {
                    player.y += fallSpeed;
                } else {
                    charState = 0;
                    blinks = 0;
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
                if (currentFrame > frameCount) {
                    currentFrame = 0;
                }
            }
        }
    }

    protected void shootCannons() {
        for(int i = 0; i < cannons.length; i++) {
            if(cannons[i] != null) {
                cannons[i].x -= cannons[i].speed;
                if(cannons[i].x < 0) {
                    cannons[i] = null;
                }
            }
        }
    }

    protected void checkCollision() {
        Rect character = new Rect(player.x, player.y, player.x + charWidth,
                player.y + charHeight);
        for(int i = 0; i < cannons.length; i++) {
            if(cannons[i] != null) {
                Rect cannonball = new Rect(cannons[i].x, cannons[i].y,
                        cannons[i].x + cannons[i].image.getWidth(),
                        cannons[i].y + cannons[i].image.getHeight());
                if(Rect.intersects(character,cannonball)) {
                    Log.d(TAG,"Player Hit!");
                    cannons[i] = null;
                    life--;
                    if (life == -1) {
                        this.post(new Runnable() {
                            @Override
                            public void run() {
                                Activity game = (Activity) getContext();
                                ImageButton dash = game.findViewById(R.id.dash);
                                ImageButton kibbles = game.findViewById(R.id.kibbles);
                                ImageButton baby = game.findViewById(R.id.baby);
                                dash.setVisibility(VISIBLE);
                                kibbles.setVisibility(VISIBLE);
                                baby.setVisibility(VISIBLE);
                            }
                        });
                    }
                }
            }
        }
    }

    protected void render(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (started == 1) {
            canvas.drawText("Distance: " + distance,200,100,textPaint);
            canvas.drawBitmap(playerLife.image, null, new RectF(100, 200, 400, 300),null);
            canvas.drawBitmap(player.image, null, new RectF(player.x, player.y, player.x + charWidth, player.y + charHeight),null);
            for(Cannon cannon : cannons) {
                if(cannon != null) {
                    canvas.drawBitmap(cannon.image, null, new RectF(cannon.x, cannon.y, cannon.x + charWidth, cannon.y + charWidth),null);
                }
            }

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
