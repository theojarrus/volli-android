package com.theost.volli;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.theost.volli.utils.AccelerometerUtils;

public class HomeActivity extends AppCompatActivity {

    private static final int ROLL_DETECT_ANGLE = 50;

    private SensorManager sensorManager;
    private AppCompatActivity activity;

    private View rootView;
    private View mBlockTop;
    private View mBlockRight;
    private View mBlockBottom;
    private View mBlockLeft;

    private TextView mTextTop;
    private TextView mTextRight;
    private TextView mTextBottom;
    private TextView mTextLeft;

    private int[] orientationCacheData;

    private boolean isAccelerometerEnabled;
    private boolean isTouchListenerEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        activity = this;

        rootView = findViewById(R.id.rootView);
        mBlockTop = findViewById(R.id.main_block_top);
        mBlockRight = findViewById(R.id.main_block_right);
        mBlockBottom = findViewById(R.id.main_block_bottom);
        mBlockLeft = findViewById(R.id.main_block_left);
        mTextTop = findViewById(R.id.main_text_top);
        mTextRight = findViewById(R.id.main_text_right);
        mTextBottom = findViewById(R.id.main_text_bottom);
        mTextLeft = findViewById(R.id.main_text_left);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        enableAccelerometer();
        enableTouchListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableAccelerometer();
        disableTouchListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableAccelerometer();
        enableTouchListener();
    }

    private void enableAccelerometer() {
        if (!isAccelerometerEnabled) {
            isAccelerometerEnabled = true;
            orientationCacheData = new int[]{};
            AccelerometerUtils.addAccelerometerListener(sensorListener, sensorManager);
        }
    }

    private void disableAccelerometer() {
        if (isAccelerometerEnabled) {
            isAccelerometerEnabled = false;
            orientationCacheData = new int[]{};
            AccelerometerUtils.removeAccelerometerListener(sensorListener, sensorManager);
        }
    }

    private void enableTouchListener() {
        if (!isTouchListenerEnabled) {
            isTouchListenerEnabled = true;
            rootView.setOnTouchListener(touchListener);
        }
    }

    private void disableTouchListener() {
        if (isTouchListenerEnabled) {
            isTouchListenerEnabled = false;
            rootView.setOnTouchListener(null);
        }
    }

    private void onDoubleTapped() {
        // hardcoded usage example
        String message = "Unlocked";
        if (isAccelerometerEnabled) {
            disableAccelerometer();
            message = "Locked";
        } else {
            enableAccelerometer();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void onMovementDetected(int movement) {
        switch (movement) {
            case AccelerometerUtils.MOVEMENT_PITCH_TOP:
                mBlockTop.setBackgroundColor(Color.BLACK);
                mTextTop.setText(getString(R.string.changed));
                break;
            case AccelerometerUtils.MOVEMENT_PITCH_BOTTOM:
                mBlockBottom.setBackgroundColor(Color.BLACK);
                mTextBottom.setText(getString(R.string.changed));
                break;
            case AccelerometerUtils.MOVEMENT_ROLL_RIGHT:
                mBlockRight.setBackgroundColor(Color.BLACK);
                mTextRight.setText(getString(R.string.changed));
                break;
            case AccelerometerUtils.MOVEMENT_ROLL_LEFT:
                mBlockLeft.setBackgroundColor(Color.BLACK);
                mTextLeft.setText(getString(R.string.changed));
                break;
        }
    }

    private void detectMovement(SensorEvent event) {
        int[] orientationData = AccelerometerUtils.getOrientationData(event.values.clone()); // [pitch, roll, azimuth]
        int movement = AccelerometerUtils.getMovement(orientationData, orientationCacheData, ROLL_DETECT_ANGLE);
        orientationCacheData = orientationData;
        onMovementDetected(movement);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            detectMovement(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    };

    private final View.OnTouchListener touchListener = new View.OnTouchListener() {

        private final GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                onDoubleTapped();
                return super.onDoubleTap(e);
            }
        });

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

    };

}