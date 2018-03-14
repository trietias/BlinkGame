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
import android.widget.Toast;

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
    private Sprite baby;
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
    private String gender;
    public int blinks;
    public int smile;
    public int distance;
    private boolean invincible;
    private int invincibleTimer;
    private int alpha;
    private boolean increase;
    Paint textPaint;
    Paint alphaPaint;

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
                invincible = false;
                increase = false;
                invincibleTimer = 0;
                alpha = 255;
                alphaPaint = new Paint();
                alphaPaint.setAlpha(alpha);
                fallSpeed = height / 20;
                jumpSpeed = height / 18;
                textPaint =  new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(screenWidth / 40);
                textPaint.setStrokeWidth(screenWidth / 40);
                textPaint.setTextAlign(Paint.Align.LEFT);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private void setUpSprites() {

        player = new Sprite(100, startHeight, BitmapFactory.decodeResource(this.getResources(), R.drawable.char0));
        baby = new Sprite(0,100, BitmapFactory.decodeResource(this.getResources(), R.drawable.sonogram));

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
            if(life > 0) {
                step();
            } else {
                reveal();
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
        // add logic

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
        if(!invincible) {
            Rect character = new Rect(player.x, player.y, player.x + charWidth,
                    player.y + charHeight);
            for (int i = 0; i < cannons.length; i++) {
                if (cannons[i] != null) {
                    Rect cannonball = new Rect(cannons[i].x, cannons[i].y,
                            cannons[i].x + cannons[i].image.getWidth(),
                            cannons[i].y + cannons[i].image.getHeight());
                    if (Rect.intersects(character, cannonball)) {
                        invincible = true;
                        cannons[i] = null;
                        life--;
                        if (life == 0) {
                            this.post(new Runnable() {
                                @Override
                                public void run() {
                                    Activity game = (Activity) getContext();
                                    ImageButton dash = game.findViewById(R.id.dash);
                                    ImageButton kibbles = game.findViewById(R.id.kibbles);
                                    ImageButton boy = game.findViewById(R.id.babyboy);
                                    ImageButton girl = game.findViewById(R.id.babygirl);
                                    dash.setVisibility(VISIBLE);
                                    dash.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getContext(), "Dash is too full to move right now...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    kibbles.setVisibility(VISIBLE);
                                    kibbles.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getContext(), "Kibbles is too timid to help you right now...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    boy.setVisibility(VISIBLE);
                                    boy.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            life = -2;
                                            gender = "He";
                                        }
                                    });
                                    girl.setVisibility(VISIBLE);
                                    girl.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            life = -2;
                                            gender = "She";
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    protected void render(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if(life > 0) {
            if (started == 1) {
                if (invincible) {
                    if (increase) {
                        alpha += 25;
                        invincibleTimer++;
                        if (alpha > 255) {
                            alpha = 255;
                            increase = false;
                        }
                    } else {
                        alpha -= 25;
                        invincibleTimer++;
                        if(alpha < 0) {
                            alpha = 0;
                            increase = true;
                        }
                    }
                }
                if(invincibleTimer >= 75) {
                    invincible = false;
                    invincibleTimer = 0;
                }

                alphaPaint.setAlpha(alpha);

                canvas.drawText("Distance: " + distance, 200, 100, textPaint);
                canvas.drawBitmap(playerLife.image, null, new RectF(100, 200, 400, 300), null);
                canvas.drawBitmap(player.image, null, new RectF(player.x, player.y, player.x + charWidth, player.y + charHeight), alphaPaint);
                for (Cannon cannon : cannons) {
                    if (cannon != null) {
                        canvas.drawBitmap(cannon.image, null, new RectF(cannon.x, cannon.y, cannon.x + charWidth, cannon.y + charWidth), null);
                    }
                }
            }
        } else if (life == 0){
            canvas.drawColor(Color.WHITE);
            canvas.drawText("Requesting backup! Choose someone to help you.", 100, (screenHeight / 10) * 2, textPaint);
            canvas.drawText("Requesting backup! Choose someone to help you.", 100, (screenHeight / 10) * 7, textPaint);
        } else if (life == -2){
            canvas.drawColor(Color.WHITE);
            canvas.drawText("Help is on its way! " + gender + " will be here in approximately 7 more months!", 0, 50, textPaint);
            canvas.drawBitmap(baby.image, null, new RectF(0,100, (screenWidth * 4) / 5, (screenHeight * 4) / 5), null);
            canvas.drawText("For now, click on STOP to save the video!", 0, (screenHeight / 10) * 9, textPaint);
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
