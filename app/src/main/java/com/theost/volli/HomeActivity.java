package com.theost.volli;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.theost.volli.models.Event;
import com.theost.volli.utils.AnimationUtils;
import com.theost.volli.utils.DisplayUtils;
import com.theost.volli.utils.PermissionUtils;
import com.theost.volli.utils.ResUtils;
import com.theost.volli.widgets.EventDecorator;
import com.theost.volli.widgets.OnGestureListener;
import com.theost.volli.widgets.TodayDecorator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private static final String DATABASE_USER = "user-";
    private static final String DATABASE_NOTE = "note-";
    private static final String DATABASE_TITLE = "title";
    private static final String DATABASE_TEXT = "text";
    private static final String DATABASE_DAY = "day";
    private static final String DATABASE_MONTH = "month";
    private static final String DATABASE_YEAR = "year";
    private static final String DATABASE_HOURS = "hours";
    private static final String DATABASE_MINUTES = "minutes";

    private static final String DATE_PATTERN = "dd.MM.yyyy - HH:mm";
    private static final String DATE_PATTERN_DAY = "dd";
    private static final String DATE_PATTERN_MONTH = "MM";
    private static final String DATE_PATTERN_YEAR = "yyyy";
    private static final String DATE_PATTERN_HOUR = "HH";
    private static final String DATE_PATTERN_MINUTE = "mm";

    private static final int DEFAULT_NOTE_TIME = 12;

    private static final int MODE_HOME = 0;
    private static final int MODE_CREATION = 1;
    private static final int MODE_CREATION_CONTENT = 2;

    private static final int MODE_DATE_YEAR = 0;
    private static final int MODE_DATE_MONTH = 1;
    private static final int MODE_DATE_DAY = 2;
    private static final int MODE_DATE_HOUR = 3;
    private static final int MODE_DATE_MINUTE = 4;

    private static final int MODE_VOICE_TITLE = 1;
    private static final int MODE_VOICE_TEXT = 2;

    private static final int BLOCK_ANIMATION_DURATION = 800;
    private static final int TEXT_ANIMATION_DURATION = 300;
    private static final int TEXT_ANIMATION_DELAY = 800;
    private static final float BLOCK_ANIMATION_SCALE = 2f;

    private static final int ANIMATION_CODE_X = 0;
    private static final int ANIMATION_CODE_Y = 1;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseUserReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private GestureDetector gestureDetector;
    private MaterialCalendarView calendarView;
    private EventDecorator eventDecorator;
    private TodayDecorator todayDecorator;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private List<Event> eventsList;

    private View mBlockTop;
    private View mBlockRight;
    private View mBlockBottom;
    private View mBlockLeft;

    private TextView mTextTopView;
    private TextView mTextRightView;
    private TextView mTextBottomView;
    private TextView mTextLeftView;

    private TextView mNoteDateView;
    private TextView mNoteTitleView;
    private TextView mNoteTextView;

    private boolean isTouchLocked;
    private boolean isVoiceEnabled;
    private boolean isEditing;
    private boolean isTodaySelected;
    private boolean isLoaded;

    private String[] currentActions;

    private int currentMode = MODE_HOME;
    private int currentDateMode = MODE_DATE_YEAR;
    private int currentVoiceMode = MODE_VOICE_TITLE;

    private Calendar calendar;
    private CalendarDay prevCalendarDay;
    private SimpleDateFormat dateFormat;

    private Date todayDate;
    private CalendarDay todayDay;

    private int currentVoiceRequest;
    private int themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Volli);
        setContentView(R.layout.activity_home);

        mBlockTop = findViewById(R.id.main_block_top);
        mBlockRight = findViewById(R.id.main_block_right);
        mBlockBottom = findViewById(R.id.main_block_bottom);
        mBlockLeft = findViewById(R.id.main_block_left);
        mTextTopView = findViewById(R.id.main_text_top);
        mTextRightView = findViewById(R.id.main_text_right);
        mTextBottomView = findViewById(R.id.main_text_bottom);
        mTextLeftView = findViewById(R.id.main_text_left);

        mNoteDateView = findViewById(R.id.note_date);
        mNoteTitleView = findViewById(R.id.note_title);
        mNoteTextView = findViewById(R.id.note_text);

        dateFormat = new SimpleDateFormat(DATE_PATTERN,
                getResources().getConfiguration().locale);

        calendarView = findViewById(R.id.calendarView);
        if (Locale.getDefault().equals(new Locale("RU", "ru"))) {
            calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getStringArray(R.array.weeks_russian)));
            calendarView.setTitleMonths(getResources().getStringArray(R.array.months_russian));
        } else {
            calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getStringArray(R.array.weeks_english)));
        }

        themeColor = ContextCompat.getColor(this, R.color.blue);
        eventDecorator = new EventDecorator(themeColor);

        currentActions = getResources().getStringArray(R.array.actions_home);
        gestureDetector = new GestureDetector(this, gestureListener);

        createSpeechRecognizer();

        updateNoteInfo(true);
    }

    private void startAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuth();
        registerReceiver();
        updateTodayDate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSpeechRecognizer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroySpeechRecognizer();
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

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            onLongTapped();
        }
    };

    private final RecognitionListener voiceListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            onTextRecognized(data.get(0));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                if (!isEditing) updateNoteInfo(true);
                updateTodayDate();
            }
        }
    };

    private void registerReceiver() {
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void unregisterReceiver() {
        if (tickReceiver != null) {
            unregisterReceiver(tickReceiver);
        }
    }

    private boolean startSpeechRecognizer() {
        if (speechRecognizer != null && speechRecognizerIntent != null) {
            if (PermissionUtils.checkPermissions(this)) {
                speechRecognizer.startListening(speechRecognizerIntent);
                return true;
            }
        }
        return false;
    }

    private void stopSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    private void destroySpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private void setSpeechRecognizerRequest(int requestCode) {
        currentVoiceRequest = requestCode;
    }

    private void createSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(voiceListener);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "ru-RU");
        speechRecognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{});
    }

    private void loadDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        firebaseUserReference = firebaseDatabase.getReference().child(DATABASE_USER + firebaseUser.getUid());
        firebaseUserReference.addValueEventListener(databaseListener);
    }

    private void databaseCreateNote(String title, String text) {
        DatabaseReference noteReference = firebaseUserReference.child(DATABASE_NOTE + UUID.randomUUID().toString());
        noteReference.child(DATABASE_DAY).setValue(calendar.get(Calendar.DAY_OF_MONTH));
        noteReference.child(DATABASE_MONTH).setValue(calendar.get(Calendar.MONTH) + 1);
        noteReference.child(DATABASE_YEAR).setValue(calendar.get(Calendar.YEAR));
        noteReference.child(DATABASE_HOURS).setValue(calendar.get(Calendar.HOUR_OF_DAY));
        noteReference.child(DATABASE_MINUTES).setValue(calendar.get(Calendar.MINUTE));
        noteReference.child(DATABASE_TITLE).setValue(title);
        noteReference.child(DATABASE_TEXT).setValue(text);
    }

    private final ValueEventListener databaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (!isLoaded) {
                eventsList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    eventsList.add(event);
                }
                loadCalendarDates();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            showDatabaseError();
        }
    };

    private void loadCalendarDates() {
        for (Event event : eventsList) {
            changeDayEvent(CalendarDay.from(event.getYear(), event.getMonth(),  event.getDay()), true);
        }
        isLoaded = true;
    }

    private void showDatabaseError() {
        DisplayUtils.showToast(this, R.string.network_not_available);
    }

    private void checkAuth() {
        if (firebaseAuth == null) firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        ArrayList<Boolean> isAuthorized = new ArrayList<>();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email != null) {
                firebaseAuth.signInWithEmailAndPassword(email, "null").addOnCompleteListener(this, task -> {
                    try {
                        Exception e = task.getException();
                        if (e != null) throw e;
                    } catch (FirebaseAuthInvalidUserException invalidEmail) {
                        isAuthorized.add(false);
                    } catch (Exception ignored) {
                    }
                });
            }
        } else {
            isAuthorized.add(false);
        }
        if (isAuthorized.contains(false)) {
            firebaseAuth.signOut();
            startAuthActivity();
        } else if (firebaseDatabase == null) {
            loadDatabase();
        }
    }

    private void onLongTapped() {
        // hardcoded usage example
        changeDayEvent(CalendarDay.from(2021, 2, 25), false);
        changeDayEvent(CalendarDay.from(2021, 2, 25), true);
        changeDayEvent(CalendarDay.from(2021, 2, 11), true);
        changeDayEvent(CalendarDay.from(2021, 2, 13), true);
        changeDayEvent(CalendarDay.from(2021, 2, 16), true);
        // play instructions
    }

    private void onDoubleTapped() {
        if (!isVoiceEnabled) {
            isVoiceEnabled = true;
            DisplayUtils.showToast(this, R.string.voice_control_enabled);
            // enable voice recognize
        } else {
            isVoiceEnabled = false;
            DisplayUtils.showToast(this, R.string.voice_control_disabled);
            // disable voice recognize
        }
    }

    private void onTextRecognized(String text) {
        if (currentVoiceRequest == MODE_VOICE_TITLE) {
            mNoteTitleView.setText(text);
            updateNoteSpan();
        } else if (currentVoiceRequest == MODE_VOICE_TEXT) {
            mNoteTextView.setText(text);
            updateNoteSpan();
        }
        // } else { do commands }
        currentVoiceRequest = -1;
    }

    private void onMovementDetected(OnGestureListener.Direction direction) {
        boolean isNeedChange = performAction(direction.getIndex());
        stopSpeechRecognizer();
        switch (direction) {
            case UP:
                animateMovement(mBlockTop, ANIMATION_CODE_X);
                break;
            case RIGHT:
                animateMovement(mBlockRight, ANIMATION_CODE_Y);
                break;
            case DOWN:
                animateMovement(mBlockBottom, ANIMATION_CODE_X);
                break;
            case LEFT:
                animateMovement(mBlockLeft, ANIMATION_CODE_Y);
                break;
        }
        if (isNeedChange) {
            updateButtons();
        }
    }

    private void updateButtons() {
        AnimationUtils.animateFadeOutIn(new TextView[]{mTextTopView, mTextRightView, mTextBottomView, mTextLeftView}, currentActions, TEXT_ANIMATION_DURATION, TEXT_ANIMATION_DELAY);
    }

    private void animateMovement(View view, int animationCode) {
        isTouchLocked = true;
        if (animationCode == ANIMATION_CODE_X) {
            AnimationUtils.animateScaleX(view, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
        } else if (animationCode == ANIMATION_CODE_Y) {
            AnimationUtils.animateScaleY(view, BLOCK_ANIMATION_SCALE, BLOCK_ANIMATION_DURATION);
        }
        new CountDownTimer(BLOCK_ANIMATION_DURATION * 2, 10000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                isTouchLocked = false;
            }
        }.start();
    }

    private void updateCurrentData(int mode, int arrayId) {
        currentActions = getResources().getStringArray(arrayId);
        currentMode = mode;
        if (currentMode == MODE_HOME) {
            updateNoteInfo(true);
        }
    }

    private boolean replaceCurrentData(int oldStringId, int newStringId) {
        boolean isFound = false;
        int i = currentActions.length - 1;
        while (i >= 0) {
            if (currentActions[i].equals(getString(oldStringId))) {
                currentActions[i] = getString(newStringId);
                isFound = true;
                break;
            }
            i--;
        }
        return isFound;
    }

    private void updateNoteInfo(boolean isHome) {
        calendar = Calendar.getInstance();
        calendarView.setCurrentDate(CalendarDay.from(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
        mNoteDateView.setText(dateFormat.format(calendar.getTime()));
        if (isHome) {
            mNoteTitleView.setText(R.string.example_note_title);
            mNoteTextView.setText(R.string.example_note_text);
        } else {
            resetNoteTitle();
            resetNoteText();
        }
    }

    private void updateDate() {
        if (currentDateMode != MODE_DATE_HOUR && currentDateMode != MODE_DATE_MINUTE) {
            CalendarDay selectedDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            selectDay(selectedDay);
            resetPrevSelectedDay(selectedDay);
            prevCalendarDay = selectedDay;
        }
        mNoteDateView.setText(dateFormat.format(calendar.getTime()));
        updateDateSpan(false);
    }

    private void updateTodayDate() {
        Calendar calendar = Calendar.getInstance();
        Date todayNewDate = calendar.getTime();
        if (todayDate == null || !todayDate.equals(todayNewDate)) {
            todayDate = todayNewDate;
            todayDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            if (todayDecorator == null) {
                todayDecorator = new TodayDecorator(todayDay, ContextCompat.getDrawable(this, R.drawable.calendar_today), themeColor);
            } else {
                calendarView.removeDecorator(todayDecorator);
                todayDecorator.changeDay(todayDay);
            }
            calendarView.addDecorator(todayDecorator);
        }
    }

    private void moveToday() {
        calendar = Calendar.getInstance();
        calendarView.setCurrentDate(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
    }

    private void restoreTodayDate() {
        if (isTodaySelected) {
            calendarView.addDecorator(todayDecorator);
        }
    }

    private void changeDayEvent(CalendarDay day, boolean isSelected) {
        boolean isChanged;
        if (isSelected) {
            isChanged = eventDecorator.addDay(day);
        } else {
            isChanged = eventDecorator.removeDay(day);
        }
        if (isChanged) {
            calendarView.removeDecorator(eventDecorator);
            calendarView.addDecorator(eventDecorator);
        }
    }

    private void updateDateSpan(boolean isReset) {
        SpannableString dateSpannableString = new SpannableString(mNoteDateView.getText().toString());
        if (!isReset) {
            int[] indexes = new int[]{0, 0};
            if (currentDateMode == MODE_DATE_YEAR) {
                indexes = findStringIndexes(DATE_PATTERN_YEAR);
            } else if (currentDateMode == MODE_DATE_MONTH) {
                indexes = findStringIndexes(DATE_PATTERN_MONTH);
            } else if (currentDateMode == MODE_DATE_DAY) {
                indexes = findStringIndexes(DATE_PATTERN_DAY);
            } else if (currentDateMode == MODE_DATE_HOUR) {
                indexes = findStringIndexes(DATE_PATTERN_HOUR);
            } else if (currentDateMode == MODE_DATE_MINUTE) {
                indexes = findStringIndexes(DATE_PATTERN_MINUTE);
            }
            if (indexes[0] >= 0 && indexes[0] != indexes[1]) {
                setSpan(dateSpannableString, indexes[0], indexes[1]);
            }
        }
        mNoteDateView.setText(dateSpannableString);
    }

    private void updateNoteSpan() {
        SpannableString titleSpan = new SpannableString(mNoteTitleView.getText().toString());
        SpannableString textSpan = new SpannableString(mNoteTextView.getText().toString());
        if (currentMode == MODE_CREATION_CONTENT) {
            if (currentVoiceMode == MODE_VOICE_TITLE) {
                setSpan(titleSpan, 0, titleSpan.length());
            } else if (currentVoiceMode == MODE_VOICE_TEXT) {
                setSpan(textSpan, 0, textSpan.length());
            }
        }
        mNoteTitleView.setText(titleSpan);
        mNoteTextView.setText(textSpan);
    }

    private void setSpan(SpannableString span, int start, int end) {
        span.setSpan(new ForegroundColorSpan(themeColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private int[] findStringIndexes(String pattern) {
        int start = DATE_PATTERN.indexOf(pattern);
        int end = start + pattern.length();
        return new int[]{start, end};
    }

    @SuppressLint("NonConstantResourceId")
    private boolean performAction(int index) {
        int actionId = ResUtils.getStringId(this, currentActions[index]);
        if (currentMode == MODE_HOME) {
            return performHomeScreenAction(actionId);
        } else if (currentMode == MODE_CREATION) {
            return performAddScreenAction(actionId);
        } else if (currentMode == MODE_CREATION_CONTENT) {
            return preformAddRecordScreenAction(actionId);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    private boolean performHomeScreenAction(int actionId) {
        switch (actionId) {
            case R.string.read:
                // do
                return true;
            case R.string.edit:
                // do
                return true;
            case R.string.create:
                addEvent();
                return true;
            case R.string.settings:
                // do
                return true;
        }
        return true;
    }

    private void addEvent() {
        isEditing = true;
        updateCurrentData(MODE_CREATION, R.array.actions_list);
        currentDateMode = MODE_DATE_YEAR;
        updateNoteInfo(false);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_NOTE_TIME);
        calendar.set(Calendar.MINUTE, 0);
        resetNoteTitle();
        resetNoteText();
        updateDate();
    }

    private void resetNoteTitle() {
        mNoteTitleView.setText(getString(R.string.new_note_title));
        updateNoteSpan();
    }

    private void resetNoteText() {
        mNoteTextView.setText(getString(R.string.new_note_text));
        updateNoteSpan();
    }

    @SuppressLint("NonConstantResourceId")
    private boolean performAddScreenAction(int actionId) {
        switch (actionId) {
            case R.string.previous:
                changeDate(-1);
                return false;
            case R.string.choose:
                return changeDateMode(true);
            case R.string.next:
                changeDate(1);
                return false;
            case R.string.back:
                return changeDateMode(false);
        }
        return true;
    }

    private void changeDate(int direction) {
        if (currentDateMode == MODE_DATE_YEAR) {
            calendar.add(Calendar.YEAR, direction);
        } else if (currentDateMode == MODE_DATE_MONTH) {
            calendar.add(Calendar.MONTH, direction);
        } else if (currentDateMode == MODE_DATE_DAY) {
            calendar.add(Calendar.DAY_OF_MONTH, direction);
        } else if (currentDateMode == MODE_DATE_HOUR) {
            calendar.add(Calendar.HOUR_OF_DAY, direction);
        } else if (currentDateMode == MODE_DATE_MINUTE) {
            calendar.add(Calendar.MINUTE, 5 * direction);
        }
        updateDate();
    }

    private void selectDay(CalendarDay selectedDay) {
        if (selectedDay.equals(todayDay)) {
            isTodaySelected = true;
            calendarView.removeDecorator(todayDecorator);
        } else {
            restoreTodayDate();
        }
        calendarView.setDateSelected(selectedDay, true);
        calendarView.setCurrentDate(selectedDay);
    }

    private void resetPrevSelectedDay(CalendarDay selectedDay) {
        if (prevCalendarDay != null && !prevCalendarDay.equals(selectedDay)) {
            calendarView.setDateSelected(prevCalendarDay, false);
        }
    }

    private boolean changeDateMode(boolean isNext) {
        if (isNext) {
            if (currentDateMode == MODE_DATE_MINUTE) {
                updateCurrentData(MODE_CREATION_CONTENT, R.array.actions_voice);
                updateDateSpan(true);
                updateNoteSpan();
                return true;
            } else {
                currentDateMode += 1;
                updateDateSpan(false);
                return false;
            }
        } else {
            if (currentDateMode == MODE_DATE_YEAR) {
                updateCurrentData(MODE_HOME, R.array.actions_home);
                updateDateSpan(true);
                resetPrevSelectedDay(null);
                restoreTodayDate();
                return true;
            } else {
                currentDateMode -= 1;
                updateDateSpan(false);
                return false;
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private boolean preformAddRecordScreenAction(int actionId) {
        switch (actionId) {
            case R.string.reset:
                return resetVoice();
            case R.string.choose:
                return changeVoiceMode(true);
            case R.string.record:
                recognizeNoteVoice(false);
                return true;
            case R.string.read:
                recognizeNoteVoice(true);
                return true;
            case R.string.back:
                return changeVoiceMode(false);
        }
        return true;
    }

    private void recognizeNoteVoice(boolean isListening) {
        if (!isListening) {
            setSpeechRecognizerRequest(currentVoiceMode);
            boolean isStarted = startSpeechRecognizer();
            if (isStarted) {
                if (currentVoiceMode == MODE_VOICE_TITLE) {
                    mNoteTitleView.setText(getString(R.string.recording));
                } else if (currentVoiceMode == MODE_VOICE_TEXT) {
                    mNoteTextView.setText(getString(R.string.recording));
                }
                replaceCurrentData(R.string.record, R.string.read);
            }
        } else {
            stopSpeechRecognizer();
            replaceCurrentData(R.string.read, R.string.record);
            updateNoteVoice();
        }
        updateNoteSpan();
    }

    private void updateNoteVoice() {
        if (currentVoiceMode == MODE_VOICE_TITLE) {
            if (mNoteTitleView.getText().toString().equals(getString(R.string.recording))) {
                mNoteTitleView.setText(getString(R.string.new_note_title));
            }
        } else if (currentVoiceMode == MODE_VOICE_TEXT) {
            if (mNoteTextView.getText().toString().equals(getString(R.string.recording))) {
                mNoteTextView.setText(getString(R.string.new_note_text));
            }
        }
    }

    private boolean resetVoice() {
        if (currentVoiceMode == MODE_VOICE_TITLE) {
            resetNoteTitle();
        } else if (currentVoiceMode == MODE_VOICE_TEXT) {
            resetNoteText();
        }
        stopSpeechRecognizer();
        return replaceCurrentData(R.string.read, R.string.record);
    }

    private boolean changeVoiceMode(boolean isNext) {
        updateNoteVoice();
        if (isNext) {
            if (currentVoiceMode == MODE_VOICE_TITLE) {
                currentVoiceMode = MODE_VOICE_TEXT;
                updateNoteSpan();
                return replaceCurrentData(R.string.read, R.string.record);
            } else {
                isEditing = false;
                currentVoiceMode = MODE_VOICE_TITLE;
                databaseCreateNote(mNoteTitleView.getText().toString(), mNoteTextView.getText().toString());
                changeDayEvent(prevCalendarDay, true);
                updateCurrentData(MODE_HOME, R.array.actions_home);
                updateNoteSpan();
                resetPrevSelectedDay(null);
                restoreTodayDate();
                moveToday();
                return true;
            }
        } else {
            if (currentVoiceMode == MODE_VOICE_TITLE) {
                updateCurrentData(MODE_CREATION, R.array.actions_list);
                updateDateSpan(false);
                updateNoteSpan();
                return true;
            } else {
                currentVoiceMode = MODE_VOICE_TITLE;
                updateNoteSpan();
                return replaceCurrentData(R.string.read, R.string.record);
            }
        }
    }

}