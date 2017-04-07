package rimp.rild.com.android.repel_game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Random;

/**
 * Created by rild on 2017/04/06.
 */

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    static final long FPS = 30;
    static final long FRAME_TIME = 1000 / FPS;

    int screenWidth, screenHeight;

    SurfaceHolder surfaceHolder;
    Thread thread;

    Ball ball;
    Enemy enemy;
    Player player;

    Bitmap enemyImg;
    Bitmap playerImg;
    Bitmap ballRedImg;
    Bitmap ballBlueImg;

    int life = 0;
    int score = 0;

    OnMissListener onMissListener;
    OnHitListener onHitListener;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnHitListener(OnHitListener onHitListener) {
        this.onHitListener = onHitListener;
    }

    public void setOnMissListener(OnMissListener onMissListener) {
        this.onMissListener = onMissListener;
    }

    private void init() {
        getHolder().addCallback(this);

        Resources res = getResources();
        enemyImg = BitmapFactory.decodeResource(res, R.drawable.triangle_dropshadow);
        playerImg = BitmapFactory.decodeResource(res, R.drawable.rectangle_dropshadow);
        ballRedImg = BitmapFactory.decodeResource(res, R.drawable.circle_red_dropshadow);
        ballBlueImg = BitmapFactory.decodeResource(res, R.drawable.circle_blue_dropshadow);

        initPoints(100, 0);
        Log.d("Image", "w = " + enemyImg.getWidth() + ", h = " + enemyImg.getHeight());
        Log.d("BallImage", "w = " + ballRedImg.getWidth() + ", h = " + ballRedImg.getHeight());
    }

    public void initPoints(int life, int score) {
        this.life = life;
        this.score = score;
    }

    public void pause() {
//        Log.d("Thread", "" + Thread.currentThread().getId() + ", " + thread.getId());
        thread = null;
    }

    public void start() {
        thread = new Thread(this);
//        Log.d("Thread", "" + Thread.currentThread().getId() + ", " + thread.getId());
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
//        thread = new Thread(this);
//        thread.start();
        start();

//        Canvas canvas = holder.lockCanvas();
//
//        canvas.drawColor(Color.WHITE);
//
//        canvas.drawBitmap(enemyImg, 10, 50, null);
//
//        holder.unlockCanvasAndPost(canvas);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }

    @Override
    public void run() {
//        enemy = new Enemy();
//        player = new Player(); // to apply "pause"
        if (enemy == null) enemy = new Enemy();
        if (player == null) player = new Player();

        Paint textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        textPaint.setTextSize(64);

        while (thread != null) {
//            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            Canvas canvas = surfaceHolder.lockCanvas();

//            canvas.drawBitmap(ballRedImg, 10, 10, null);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
//            canvas.drawColor(Color.WHITE);

//            canvas.drawText("SCORE: " + score, 100, 300, textPaint);
//            canvas.drawText("LIFE: " + life, 100, 380, textPaint);

            if (enemy != null) canvas.drawBitmap(enemyImg, enemy.x, enemy.y, null);
            if (ball != null) {
                if (ball.color == BallColor.Red) {
                    canvas.drawBitmap(ballRedImg, ball.x, ball.y, null);
                } else {
                    canvas.drawBitmap(ballBlueImg, ball.x, ball.y, null);
                }
            }

            if (enemy == null) enemy = new Enemy();
            if (ball == null) ball = enemy.createBall();

            enemy.update();

            canvas.drawBitmap(playerImg, player.x, player.y, null);


            if (player.isEnter(ball)) {
                ball = player.createBall();
            } else if (enemy.isEnter(ball)) { // Hit
                // the blue ball hits the enemy
                enemy = null;
                ball = null;

//                score += SCORE_PER_HIT;
                if (onHitListener != null) onHitListener.onHit();
            }

            if (ball != null) {
                if (ball.y > screenHeight || ball.y < 0) {
//                ball.reset();
                    if (ball.y > screenHeight) { // Miss
                        if (onMissListener != null) onMissListener.onMiss();
                    }
                    ball = null;
                } else {
                    ball.update();
                }
            }

            surfaceHolder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(FRAME_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    class Enemy {
        private final int RIGHT = 0;
        private final int LEFT = 1;

        //        private static final int WIDTH = 68;
        //        private static final int HEIGHT = 32;
        private static final int WIDTH = 204;
        private static final int HEIGHT = 96;
        float x, y;

        float deltaX = 100.0f;

        public Enemy() {
            Random random = new Random();
            x = random.nextInt(screenWidth - WIDTH); // screenWidth is used here
            y = 50;

//           <----- debug
//            x = screenWidth / 2 - WIDTH / 2;
//            y = 50;
//            debug end ------>
        }

        void update() {

            Random random = new Random();
            int flag = random.nextInt(50);
            if (flag == RIGHT && x + deltaX < screenWidth - WIDTH) { // screenWidth is used here
                x += deltaX;
            } else if (flag == LEFT && x - deltaX > 0) {
                x -= deltaX;
            }
        }

        Ball createBall() {
            Ball b = new Ball(BallColor.Red, x + WIDTH / 2 - Ball.WIDTH / 2, y);
            return b;
        }

        public boolean isEnter(Ball ball) {
            if (ball.color != BallColor.Blue) return false;

            if (ball.x + Ball.WIDTH > x && ball.x < x + WIDTH &&
                    ball.y + Ball.HEIGHT > y && ball.y < y + HEIGHT) {
                return true;
            }
            return false;
        }

    }

    enum BallColor {
        Red, Blue;
    }

    class Ball {
        //        private static final int WIDTH = 32;
        //        private static final int HEIGHT = 32;
        private static final int WIDTH = 96;
        private static final int HEIGHT = 96;

        BallColor color;
        float x, y;

//        public Ball() {
////            x = 0;
//            init();
//        }

        public Ball(BallColor color, float x, float y) {
            this.color = color;
            this.y = y;
            this.x = x;
        }

        void update() {
            switch (color) {
                case Red:
//                    y += 15.0f;
                    y += 20.0f;
                    break;
                case Blue:
//                    y -= 15.0f;
                    y -= 20.0f;
                    break;
            }
        }

//        void reset() {
//            init();
//        }
//
//        private void init() {
//            Random random = new Random();
//            x = random.nextInt(screenWidth - WIDTH); // screenWidth is used here
//            y = 0;
//        }
    }

    class Player {
        //        private static final int WIDTH = 200;
//        private static final int HEIGHT = 200;
        private static final int WIDTH = 204;
        private static final int HEIGHT = 96;
        float x, y;

        public Player() {
            x = screenWidth / 2 - WIDTH / 2;
            y = screenHeight - 150;
        }

        public void move(float diffX) {
            this.x += diffX;
            this.x = Math.max(0, x);
            this.x = Math.min(screenWidth - WIDTH, x);
        }

        public boolean isEnter(Ball ball) {
            if (ball.color != BallColor.Red) return false;

            if (ball.x + Ball.WIDTH > x && ball.x < x + WIDTH &&
                    ball.y + Ball.HEIGHT > y && ball.y < y + HEIGHT) {
                return true;
            }
            return false;
        }

        Ball createBall() {
//            Ball b = new Ball(BallColor.Blue, x + WIDTH / 2 - Ball.WIDTH / 2, y);
            Ball b = new Ball(BallColor.Blue, ball.x, ball.y);
            return b;
        }
    }

    interface OnHitListener {
        void onHit();
    }

    interface OnMissListener {
        void onMiss();
    }
}
