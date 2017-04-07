package rimp.rild.com.android.repel_game;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private GameView mGameView;
    private SensorManager mSensorManager;

    private ProgressBar mProgressBarScore; // red
    private ProgressBar mProgressBarLife; // blue
    private ImageButton mButtonRetry;
    private ImageButton mButtonControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGameView = (GameView) findViewById(R.id.main_gameview);
        mProgressBarScore = (ProgressBar) findViewById(R.id.progressbar_score);
        mProgressBarLife = (ProgressBar) findViewById(R.id.progressbar_life);
        mButtonRetry = (ImageButton) findViewById(R.id.button_retry);
        mButtonControl = (ImageButton) findViewById(R.id.button_control);

        mProgressBarLife.setProgress(mGameView.life);
        mProgressBarScore.setProgress(mGameView.score);

        mGameView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mGameView.setZOrderOnTop(true);
        mGameView.setOnHitListener(new GameView.OnHitListener() {
            @Override
            public void onHit() {
                mGameView.score += 10;
                mProgressBarScore.setProgress(mGameView.score);
            }
        });

        mGameView.setOnMissListener(new GameView.OnMissListener() {
            @Override
            public void onMiss() {
                mGameView.life -= 10;
                mProgressBarLife.setProgress(mGameView.life);
            }
        });

        mButtonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGameView.initPoints(100, 0);
                mProgressBarScore.setProgress(mGameView.score);
                mProgressBarLife.setProgress(mGameView.life);
            }
        });

        mButtonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonControl.getTag().equals("pause")) {
                    mButtonControl.setTag("play");
                    mButtonControl.setImageResource(R.drawable.icon_play);
                    mGameView.pause();
                } else { // "play"
                    mButtonControl.setTag("pause");
                    mButtonControl.setImageResource(R.drawable.icon_pause);
                    mGameView.start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (!sensors.isEmpty()) {
            mSensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mGameView.player != null) {
            mGameView.player.move(-event.values[0]);
        }
    }
}
