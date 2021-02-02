package com.theost.volli;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.theost.volli.utils.AnimationUtils;
import com.theost.volli.widgets.OnGestureListener;

public class HomeActivity extends AppCompatActivity {

    private static final int BLOCK_ANIMATION_DURATION = 800;
    private static final int TEXT_ANIMATION_DURATION = 300;
    private static final int TEXT_ANIMATION_DELAY = 800;
    private static final float BLOCK_ANIMATION_SCALE = 2f;

    private FirebaseAuth firebaseAuth;

    private GestureDetector gestureDetector;
    private MaterialCalendarView calendarView;

    private View mBlockTop;
    private View mBlockRight;
    private View mBlockBottom;
    private View mBlockLeft;

    private TextView mTextTop;
    private TextView mTextRight;
    private TextView mTextBottom;
    private TextView mTextLeft;

    private boolean isTouchLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Volli);
        setContentView(R.layout.activity_home);

        calendarView = findViewById(R.id.calendarView);
        mBlockTop = findViewById(R.id.main_block_top);
        mBlockRight = findViewById(R.id.main_block_right);
        mBlockBottom = findViewById(R.id.main_block_bottom);
        mBlockLeft = findViewById(R.id.main_block_left);
        mTextTop = findViewById(R.id.main_text_top);
        mTextRight = findViewById(R.id.main_text_right);
        mTextBottom = findViewById(R.id.main_text_bottom);
        mTextLeft = findViewById(R.id.main_text_left);

        gestureDetector = new GestureDetector(this, gestureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouchLocked) gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private final OnGestureListener gestureListener = new OnGestureListener() {
        @Override
        public boolean onSwipe(OnGestureListener.Direction direction) {
            onMovementDetected(direction);
            return super.onSwipe(direction);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTapped();
            return super.onDoubleTap(e);
        }
    };

    private void checkAuth() {
        if (firebaseAuth == null) firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email != null) {
                firebaseAuth.signInWithEmailAndPassword(email, "null").addOnCompleteListener(this, task -> {
                    try {
                        Exception e = task.getException();
                        if (e != null) throw e;
                    } catch (FirebaseAuthInvalidUserException invalidEmail) {
                        firebaseAuth.signOut();
                        startAuthActivity();
                    } catch (Exception ignored) {
                    }
                });
            }
        } else {
            startAuthActivity();
        }
    }

    private void onDoubleTapped() {
        // hardcoded usage example
        calendarView.setDateSelected(CalendarDay.from(2021, 2, 25), true);
        calendarView.setDateSelected(CalendarDay.from(2021, 2, 11), true);
        calendarView.setDateSelected(CalendarDay.from(2021, 2, 13), true);
        calendarView.setDateSelected(CalendarDay.from(2021, 2, 16), true);
    }

    private void onMovementDetected(OnGestureListener.Direction direction) {
        isTouchLocked = true;
        switch (direction) {
            case UP:
                AnimationUtils.animateScaleX(mBlockTop, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
                break;
            case RIGHT:
                AnimationUtils.animateScaleY(mBlockRight, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
                break;
            case DOWN:
                AnimationUtils.animateScaleX(mBlockBottom, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
                break;
            case LEFT:
                AnimationUtils.animateScaleY(mBlockLeft, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
                break;
        }
        animateTextUpdate(mTextTop, mTextRight, mTextBottom, mTextLeft);
        new CountDownTimer(BLOCK_ANIMATION_DURATION * 2, 10000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                isTouchLocked = false;
            }
        }.start();
    }

    private void animateTextUpdate(View... views) {
        for (View v : views) {
            AnimationUtils.animateFadeOutIn(v, TEXT_ANIMATION_DURATION, TEXT_ANIMATION_DELAY);
        }
    }

    private void startAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

}