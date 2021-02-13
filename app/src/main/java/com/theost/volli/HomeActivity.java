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
import com.theost.volli.widgets.TextSpeaker;
import com.theost.volli.widgets.TodayDecorator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private static final String DATABASE_USER = "user-";
    private static final String DATABASE_NOTE = "note-";
    private static final String DATABASE_NOTE_ID = "id";
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

    private static final int[] DAY_TIME_INTERVALS = new int[]{6, 12, 18};

    private static final int SYNC_COUNTDOWN_MILLIS = 10000;

    private static final int DEFAULT_NOTE_TIME = 12;

    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_CREATION = 1;
    private static final int SCREEN_CREATION_CONTENT = 2;
    private static final int SCREEN_READING = 3;

    private static final int MODE_DATE_YEAR = 0;
    private static final int MODE_DATE_MONTH = 1;
    private static final int MODE_DATE_DAY = 2;
    private static final int MODE_DATE_HOUR = 3;
    private static final int MODE_DATE_MINUTE = 4;

    private static final int MODE_VOICE_ACTION = 0;
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
    private TextSpeaker textSpeaker;
    private MaterialCalendarView calendarView;
    private EventDecorator eventDecorator;
    private TodayDecorator todayDecorator;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private TreeMap<Long, ArrayList<Event>> eventsMap;

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
    private boolean isTodaySelected;
    private boolean isLoaded;
    private boolean isWelcomePlayed;
    private boolean isSyncWaiting;
    private boolean isConfirmation;
    private boolean isVoiceControlLocked;
    private boolean disableDatabaseListener;

    private String[] currentActions;
    private String[] russianMonths;

    private int currentScreen = SCREEN_HOME;
    private int currentDateMode = MODE_DATE_YEAR;
    private int currentVoiceMode = MODE_VOICE_TITLE;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private Date todayDate;
    private CalendarDay todayDay;
    private Event todayEvent;
    private String todayDateId;

    private long nextEventKey;
    private long currentReadKey;
    private int currentReadIndex;

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

        gestureDetector = new GestureDetector(this, gestureListener);

        textSpeaker = new TextSpeaker(this);

        Locale russianLocale = new Locale("RU", "ru");
        dateFormat = new SimpleDateFormat(DATE_PATTERN, russianLocale);

        russianMonths = getResources().getStringArray(R.array.months_russian);

        calendarView = findViewById(R.id.calendarView);
        if (Locale.getDefault().equals(russianLocale)) {
            calendarView.setTitleMonths(russianMonths);
            calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getStringArray(R.array.weeks_russian)));
        } else {
            calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getStringArray(R.array.weeks_english)));
        }

        eventsMap = new TreeMap<>();

        themeColor = ContextCompat.getColor(this, R.color.blue);
        eventDecorator = new EventDecorator(themeColor);

        updateTodayInfo(true);

        createSpeechRecognizer();
    }

    private void startAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        updateTodayDate();
        syncData();
        cloudSyncTask.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSpeechRecognizer();
        cloudSyncTask.cancel();
        textSpeaker.stop();
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
    public void onBackPressed() {
        // skip false positive
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouchLocked && gestureDetector != null) gestureDetector.onTouchEvent(event);
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
            isVoiceEnabled = false;
            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                onTextNotRecognized();
            }
        }

        @Override
        public void onResults(Bundle results) {
            isVoiceEnabled = false;
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
                if (currentScreen == SCREEN_HOME && todayEvent == null) {
                    updateTodayInfo(true);
                }
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
        if (speechRecognizer != null && speechRecognizerIntent != null && !isVoiceEnabled) {
            if (PermissionUtils.checkPermissions(this)) {
                speechRecognizer.startListening(speechRecognizerIntent);
                isVoiceEnabled = true;
                return true;
            }
        }
        return false;
    }

    private void stopSpeechRecognizer() {
        if (speechRecognizer != null && isVoiceEnabled) {
            speechRecognizer.stopListening();
            isVoiceEnabled = false;
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

    private void getDatabase() {
        if (firebaseDatabase == null || firebaseUserReference == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
            firebaseUserReference = firebaseDatabase.getReference().child(DATABASE_USER + firebaseUser.getUid());
        }
        firebaseUserReference.addValueEventListener(databaseListener);
    }

    private void createNewEvent(String title, String text) {
        disableDatabaseListener = true;
        new Thread() {
            @Override
            public void run() {
                Event event = new Event();
                event.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                event.setMonth(calendar.get(Calendar.MONTH) + 1);
                event.setYear(calendar.get(Calendar.YEAR));
                event.setHours(calendar.get(Calendar.HOUR_OF_DAY));
                event.setMinutes(calendar.get(Calendar.MINUTE));
                String noteId = UUID.randomUUID().toString();
                event.setId(noteId);
                event.setTitle(title);
                event.setText(text);
                updateEventMap(eventsMap, event);
                syncEventDatabase(event);
                try {
                    sleep(2000);
                    disableDatabaseListener = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void syncEventDatabase(Event event) {
        DatabaseReference noteReference = firebaseUserReference.child(DATABASE_NOTE + event.getId());
        noteReference.child(DATABASE_DAY).setValue(event.getDay());
        noteReference.child(DATABASE_MONTH).setValue(event.getMonth());
        noteReference.child(DATABASE_YEAR).setValue(event.getYear());
        noteReference.child(DATABASE_HOURS).setValue(event.getHours());
        noteReference.child(DATABASE_MINUTES).setValue(event.getMinutes());
        noteReference.child(DATABASE_NOTE_ID).setValue(event.getId());
        noteReference.child(DATABASE_TITLE).setValue(event.getTitle());
        noteReference.child(DATABASE_TEXT).setValue(event.getText());
    }

    private final ValueEventListener databaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (!disableDatabaseListener) {
                TreeMap<Long, ArrayList<Event>> databaseEventMap = new TreeMap<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event event = ds.getValue(Event.class);
                    if (event != null) {
                        updateEventMap(databaseEventMap, event);
                    }
                }
                onDatabaseEventsChange(databaseEventMap);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            showMessage(R.string.network_not_available);
        }
    };

    private void updateEventMap(TreeMap<Long, ArrayList<Event>> eventsMap, Event event) {
        long dateId = getDateId(event);
        ArrayList<Event> eventsList = new ArrayList<>();
        if (eventsMap.containsKey(dateId)) {
            eventsList.addAll(eventsMap.get(dateId));
        }
        eventsList.add(event);
        eventsMap.put(dateId, eventsList);
    }

    private long getDateId(Event event) {
        return Long.parseLong(event.getYear() + getDigitStringTime(event.getMonth()) + getDigitStringTime(event.getDay()) + getDigitStringTime(event.getHours()) + getDigitStringTime(event.getMinutes()));
    }

    private void onDatabaseEventsChange(TreeMap<Long, ArrayList<Event>> databaseEventMap) {
        if (eventsMap.size() == 0 || !new HashSet<>(databaseEventMap.values()).equals(new HashSet<>(eventsMap.values()))) {
            eventsMap = new TreeMap<>(databaseEventMap);
            clearDayEvents();
            loadCalendarDates();
            if (currentScreen == SCREEN_HOME) {
                updateTodayInfo(true);
                if (!isConfirmation) updateScreen(SCREEN_HOME, R.array.actions_home);
                if (!isWelcomePlayed && firebaseUser != null) {
                    isWelcomePlayed = true;
                    playSync();
                    playWelcome();
                }
            }
        }
    }

    private void loadCalendarDates() {
        for (ArrayList<Event> eventList : eventsMap.values()) {
            for (Event event : eventList) {
                CalendarDay day = CalendarDay.from(event.getYear(), event.getMonth(), event.getDay());
                changeDayEvent(day, true);
            }
        }
        isLoaded = true;
    }

    private void showMessage(int errorId) {
        DisplayUtils.showToast(this, errorId);
        textSpeaker.speakAfter(getString(errorId));
    }

    private void showMessageNow(int errorId) {
        DisplayUtils.showToast(this, errorId);
        textSpeaker.speak(getString(errorId));
    }

    private final CountDownTimer cloudSyncTask = new CountDownTimer(SYNC_COUNTDOWN_MILLIS, SYNC_COUNTDOWN_MILLIS) {
        public void onTick(long millisUntilFinished) {
        }

        public void onFinish() {
            if (currentScreen == SCREEN_HOME) {
                syncData();
            }
            start();
        }
    };

    private void syncData() {
        if (firebaseAuth == null) firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email != null) {
                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    boolean isValid = !task.getResult().getSignInMethods().isEmpty();
                    if (isValid) {
                        getDatabase();
                    } else {
                        firebaseAuth.signOut();
                        startAuthActivity();
                    }
                });
            }
        } else {
            firebaseAuth.signOut();
            startAuthActivity();
        }
    }

    private boolean checkLoaded() {
        if (!isLoaded) {
            isSyncWaiting = true;
            showMessage(R.string.data_is_loading);
        }
        return isLoaded;
    }

    private void onLongTapped() {
        playActions();
    }

    private void onDoubleTapped() {
        if (!isVoiceControlLocked) {
            if (!isVoiceEnabled) {
                enableVoiceControl();
            } else {
                disableVoiceControl();
            }
        }
    }

    private void enableVoiceControl() {
        showMessageNow(R.string.voice_control_enabled);
        setSpeechRecognizerRequest(MODE_VOICE_ACTION);
        startSpeechRecognizer();
    }

    private void disableVoiceControl() {
        showMessageNow(R.string.voice_control_disabled);
        setSpeechRecognizerRequest(currentVoiceMode);
        stopSpeechRecognizer();
    }

    private void onTextRecognized(String text) {
        text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        if (currentScreen == SCREEN_CREATION_CONTENT) {
            if (currentVoiceRequest == MODE_VOICE_TITLE) {
                currentVoiceRequest = MODE_VOICE_ACTION;
                mNoteTitleView.setText(text);
                updateNoteSpan();
                isVoiceControlLocked = false;
                textSpeaker.speakAfter(getString(R.string.recorded));
                playActions();
            } else if (currentVoiceRequest == MODE_VOICE_TEXT) {
                currentVoiceRequest = MODE_VOICE_ACTION;
                mNoteTextView.setText(text);
                updateNoteSpan();
                isVoiceControlLocked = false;
                textSpeaker.speakAfter(getString(R.string.recorded));
                playActions();
            }
        } else if (currentVoiceRequest == MODE_VOICE_ACTION) {
            text = text.toLowerCase();
            String actionOne = currentActions[0].trim().toLowerCase();
            String actionTwo = currentActions[1].trim().toLowerCase();
            String actionThree = currentActions[2].trim().toLowerCase();
            String actionFour = currentActions[3].trim().toLowerCase();
            if (text.contains(actionOne.substring(0, (actionOne.length() > 5 ? actionOne.length() - 2 : actionOne.length())))) {
                onMovementDetected(OnGestureListener.Direction.UP);
            } else if (!actionOne.equals("") && text.contains(actionTwo.substring(0, (actionTwo.length() > 5 ? actionTwo.length() - 2 : actionTwo.length())))) {
                onMovementDetected(OnGestureListener.Direction.RIGHT);
            } else if (text.contains(actionThree.substring(0, (actionThree.length() > 5 ? actionThree.length() - 2 : actionThree.length())))) {
                onMovementDetected(OnGestureListener.Direction.DOWN);
            } else if (!actionFour.equals("") && text.contains(actionFour.substring(0, (actionFour.length() > 5 ? actionFour.length() - 2 : actionFour.length())))) {
                onMovementDetected(OnGestureListener.Direction.LEFT);
            }
            startSpeechRecognizer();
        }
    }

    private void onTextNotRecognized() {
        startSpeechRecognizer();
    }

    private void onMovementDetected(OnGestureListener.Direction direction) {
        if (isVoiceEnabled && currentVoiceRequest != MODE_VOICE_ACTION) stopSpeechRecognizer();
        if (checkLoaded()) {
            boolean isNeedChange = performAction(direction.getIndex());
            playActions();
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

    private void updateScreen(int mode, int arrayId) {
        currentActions = getResources().getStringArray(arrayId);
        currentScreen = mode;
        if (currentScreen == SCREEN_HOME) {
            updateTodayInfo(true);
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

    private void updateTodayInfo(boolean isHome) {
        calendar = Calendar.getInstance();
        calendarView.setCurrentDate(CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
        if (isHome) {
            todayEvent = getTodayEvent(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            if (todayEvent != null) {
                updateNoteInfo(todayEvent);
            } else {
                mNoteTitleView.setText(R.string.example_note_title);
                mNoteTextView.setText(R.string.example_note_text);
                mNoteDateView.setText(dateFormat.format(calendar.getTime()));
            }
        } else {
            resetNoteTitle();
            resetNoteText();
        }
    }

    private void updateNoteInfo(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, event.getYear());
        calendar.set(Calendar.MONTH, event.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, event.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, event.getHours());
        calendar.set(Calendar.MINUTE, event.getMinutes());
        mNoteTitleView.setText(event.getTitle());
        mNoteTextView.setText(event.getText());
        mNoteDateView.setText(dateFormat.format(calendar.getTime()));
        if (currentScreen == SCREEN_READING) playEvent(event);
    }

    private Event getTodayEvent(int year, int month, int day, int hours, int minutes) {
        todayDateId = year + getDigitStringTime(month) + getDigitStringTime(day);
        long todayTimeId = Long.parseLong(todayDateId + getDigitStringTime(hours) + getDigitStringTime(minutes));
        long tomorrowTimeId = Long.parseLong(year + getDigitStringTime(month) + getDigitStringTime(day + 1) + "0000");
        for (Long key : eventsMap.keySet()) {
            if (String.valueOf(key).contains(todayDateId) && key > todayTimeId) {
                nextEventKey = key;
                return eventsMap.get(key).get(0);
            } else {
                nextEventKey = key;
                if (key >= tomorrowTimeId) {
                    break;
                }
            }
        }
        return null;
    }

    private String getDigitStringTime(int twoDigitValue) {
        String date = String.valueOf(twoDigitValue);
        return (date.length() == 2 ? date : "0" + date);
    }

    private void updateDate() {
        if (currentDateMode != MODE_DATE_HOUR && currentDateMode != MODE_DATE_MINUTE) {
            CalendarDay selectedDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            updateSelectedDay(selectedDay);
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
            isTodaySelected = false;
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

    private void clearDayEvents() {
        if (eventDecorator != null) {
            boolean isUpdated = eventDecorator.clear();
            if (isUpdated) {
                calendarView.removeDecorator(eventDecorator);
                calendarView.addDecorator(eventDecorator);
            }
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
        if (currentScreen == SCREEN_CREATION_CONTENT) {
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
        textSpeaker.speak(String.format(String.valueOf(getString(R.string.actions_choose)), currentActions[index]));
        if (currentScreen == SCREEN_HOME) {
            return performHomeScreenAction(actionId);
        } else if (currentScreen == SCREEN_CREATION) {
            return performAddScreenAction(actionId);
        } else if (currentScreen == SCREEN_CREATION_CONTENT) {
            return preformAddRecordScreenAction(actionId);
        } else if (currentScreen == SCREEN_READING) {
            return preformReadScreenAction(actionId);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    private boolean performHomeScreenAction(int actionId) {
        switch (actionId) {
            case R.string.read:
                readEvent();
                return true;
            case R.string.instructions:
                playInstructions();
                return false;
            case R.string.create:
                addEvent();
                return true;
            case R.string.sign_out:
                isConfirmation = true;
                confirmQuestion(SCREEN_HOME);
                return true;
            case R.string.yes:
                isConfirmation = false;
                updateScreen(SCREEN_HOME, R.array.actions_home);
                updateButtons();
                firebaseAuth.signOut();
                startAuthActivity();
                return false;
            case R.string.no:
                isConfirmation = false;
                cancelSignOut();
                return true;
        }
        return false;
    }

    private void playSync() {
        if (isSyncWaiting) {
            textSpeaker.speakAfter(getString(R.string.data_is_loaded));
        }
    }

    private void playWelcome() {
        StringBuilder welcomeText = new StringBuilder();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if (hours < DAY_TIME_INTERVALS[0]) {
            welcomeText.append(getString(R.string.good_night));
        } else if (hours < DAY_TIME_INTERVALS[1]) {
            welcomeText.append(getString(R.string.good_morning));
        } else if (hours < DAY_TIME_INTERVALS[2]) {
            welcomeText.append(getString(R.string.good_day));
        } else {
            welcomeText.append(getString(R.string.good_evening));
        }
        welcomeText.append(". ");
        if (todayEvent != null) {
            welcomeText.append(String.format(getString(R.string.today_events), dateFormat.format(calendar.getTime()))).append(". ");
            List<Event> todayEventList = eventsMap.get(nextEventKey);
            for (int i = 0; i < todayEventList.size(); i++) {
                Event event = todayEventList.get(i);
                welcomeText.append(event.getTitle()).append(" ").append(getString(R.string.at)).append(" ").append(getDigitStringTime(event.getHours())).append(":").append(getDigitStringTime(event.getMinutes()));
                if (i != todayEventList.size() - 1) {
                    welcomeText.append(" ").append(getString(R.string.and)).append(" ");
                }
            }
        } else {
            welcomeText.append(String.format(getString(R.string.no_today_events), dateFormat.format(calendar.getTime())));
        }
        textSpeaker.speakAfter(String.format(welcomeText.toString(), firebaseUser.getDisplayName()));
        playActions();
    }

    private void playActions() {
        if (checkLoaded() && !isVoiceControlLocked) {
            String actionsInstructions = getString(R.string.available_actions) + ". ";
            if (currentActions[1].trim().equals("") || currentActions[3].trim().equals("")) {
                actionsInstructions += String.format(String.valueOf(getString(R.string.actions_confirm_app)),
                        currentActions[0], currentActions[2]);
            } else {
                actionsInstructions += String.format(String.valueOf(getString(R.string.actions_app)),
                        currentActions[0], currentActions[1], currentActions[2], currentActions[3]);
            }
            textSpeaker.speakAfter(actionsInstructions);
        }
    }

    private void playInstructions() {
        if (textSpeaker != null) {
            textSpeaker.speakAfter(getString(R.string.instructions_app));
        }
    }

    private void playEvent(Event event) {
        String eventText = getString(R.string.event) + " " + event.getTitle() + ". " + getString(R.string.event_date) + ". ";
        String eventDateId = String.valueOf(getDateId(event));
        eventText += getEventSpeakDate(event) + ". " + getVoiceDay(eventDateId) + getString(R.string.event_text) + ". " + event.getText() + ". ";
        textSpeaker.speakAfter(eventText);
    }

    private String getVoiceDay(String eventDateId) {
        if (eventDateId.contains(todayDateId)) {
            return getString(R.string.today) + ". ";
        } else if (eventDateId.contains(String.valueOf(Integer.parseInt(todayDateId) + 1))) {
            return getString(R.string.tomorrow) + ". ";
        } else if (eventDateId.contains(String.valueOf(Integer.parseInt(todayDateId) - 1))) {
            return getString(R.string.yesterday) + ". ";
        }
        return "";
    }

    private String getEventSpeakDate(Event event) {
        return getDigitStringTime(event.getDay()) + "." + getDigitStringTime(event.getMonth()) + "." + event.getYear() + " "
                + getDigitStringTime(event.getHours()) + ":" + getDigitStringTime(event.getMinutes());
    }

    private void cancelSignOut() {
        updateScreen(SCREEN_HOME, R.array.actions_home);
    }

    private void addEvent() {
        updateScreen(SCREEN_CREATION, R.array.actions_list);
        currentDateMode = MODE_DATE_YEAR;
        updateTodayInfo(false);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_NOTE_TIME);
        calendar.set(Calendar.MINUTE, 0);
        resetNoteTitle();
        resetNoteText();
        updateDate();
        speakDate();
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
                speakDate();
                return false;
            case R.string.choose:
                return changeDateMode(true);
            case R.string.next:
                changeDate(1);
                speakDate();
                return false;
            case R.string.back:
                return changeDateMode(false);
        }
        return true;
    }

    private void speakDate() {
        String dateText = "";
        if (currentDateMode == MODE_DATE_YEAR) {
            dateText += getString(R.string.choose_year) + ". " + String.format(String.valueOf(getString(R.string.current_date)), calendar.get(Calendar.YEAR));
        } else if (currentDateMode == MODE_DATE_MONTH) {
            dateText += getString(R.string.choose_month) + ". " + String.format(String.valueOf(getString(R.string.current_date)), russianMonths[calendar.get(Calendar.MONTH)]);
        } else if (currentDateMode == MODE_DATE_DAY) {
            String eventDateId = calendar.get(Calendar.YEAR) + getDigitStringTime(calendar.get(Calendar.MONTH) + 1) + getDigitStringTime(calendar.get(Calendar.DAY_OF_MONTH)) + "0000";
            dateText += getString(R.string.choose_day) + ". " + String.format(String.valueOf(getString(R.string.current_date)), calendar.get(Calendar.DAY_OF_MONTH)) + ". " + getVoiceDay(eventDateId);
        } else if (currentDateMode == MODE_DATE_HOUR) {
            dateText += getString(R.string.choose_hour) + ". " + String.format(String.valueOf(getString(R.string.current_time)), getDigitStringTime(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getDigitStringTime(calendar.get(Calendar.MINUTE)));
        } else if (currentDateMode == MODE_DATE_MINUTE) {
            dateText += getString(R.string.choose_minute) + ". " + String.format(String.valueOf(getString(R.string.current_time)), getDigitStringTime(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + getDigitStringTime(calendar.get(Calendar.MINUTE)));
        }
        textSpeaker.speakAfter(dateText);
    }

    private void changeDate(int direction) {
        if (currentDateMode == MODE_DATE_YEAR) {
            calendar.add(Calendar.YEAR, direction);
        } else if (currentDateMode == MODE_DATE_MONTH) {
            calendar.add(Calendar.MONTH, direction);
        } else if (currentDateMode == MODE_DATE_DAY) {
            calendar.add(Calendar.DAY_OF_MONTH, direction);
        } else if (currentDateMode == MODE_DATE_HOUR) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY) + direction;
            if (hour >= 24) {
                hour -= 24;
            } else if (hour < 0) {
                hour += 24;
            }
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        } else if (currentDateMode == MODE_DATE_MINUTE) {
            int minute = calendar.get(Calendar.MINUTE) + 5 * direction;
            if (minute >= 60) {
                minute -= 60;
            } else if (minute < 0) {
                minute += 60;
            }
            calendar.set(Calendar.MINUTE, minute);
        }
        updateDate();
    }

    private void updateSelectedDay(CalendarDay selectedDay) {
        if (selectedDay.equals(todayDay)) {
            isTodaySelected = true;
            calendarView.removeDecorator(todayDecorator);
        } else {
            restoreTodayDate();
        }
        calendarView.setSelectedDate(selectedDay);
        calendarView.setCurrentDate(selectedDay);
    }

    private boolean changeDateMode(boolean isNext) {
        if (isNext) {
            if (currentDateMode == MODE_DATE_MINUTE) {
                currentVoiceMode = MODE_VOICE_TITLE;
                updateScreen(SCREEN_CREATION_CONTENT, R.array.actions_voice);
                updateDateSpan(true);
                updateNoteSpan();
                speakDate();
                return true;
            } else {
                currentDateMode += 1;
                updateDateSpan(false);
                speakDate();
                return false;
            }
        } else {
            if (currentDateMode == MODE_DATE_YEAR) {
                updateScreen(SCREEN_HOME, R.array.actions_home);
                updateDateSpan(true);
                calendarView.clearSelection();
                restoreTodayDate();
                speakDate();
                return true;
            } else {
                currentDateMode -= 1;
                updateDateSpan(false);
                speakDate();
                return false;
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private boolean preformAddRecordScreenAction(int actionId) {
        isVoiceControlLocked = false;
        switch (actionId) {
            case R.string.reset:
                resetVoice();
                return replaceCurrentData(R.string.read, R.string.record);
            case R.string.choose:
                return changeVoiceMode(true);
            case R.string.record:
                recognizeNoteVoice();
                return true;
            case R.string.read:
                readNoteVoice();
                return true;
            case R.string.back:
                return changeVoiceMode(false);
        }
        return true;
    }

    private void recognizeNoteVoice() {
        if (!isVoiceEnabled) {
            isVoiceControlLocked = true;
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
            updateNoteSpan();
        }
    }

    private void readNoteVoice() {
        stopSpeechRecognizer();
        updateNoteVoice();
        updateNoteSpan();
        if (currentVoiceMode == MODE_VOICE_TITLE) {
            textSpeaker.speakAfter(getString(R.string.event_title) + ". " + mNoteTitleView.getText().toString());
        } else if (currentVoiceMode == MODE_VOICE_TEXT) {
            textSpeaker.speakAfter(getString(R.string.event_text) + ". " + mNoteTextView.getText().toString());
        }
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

    private void resetVoice() {
        stopSpeechRecognizer();
        if (currentVoiceMode == MODE_VOICE_TITLE) {
            resetNoteTitle();
        } else if (currentVoiceMode == MODE_VOICE_TEXT) {
            resetNoteText();
        }
    }

    private boolean changeVoiceMode(boolean isNext) {
        updateNoteVoice();
        if (isNext) {
            if (currentVoiceMode == MODE_VOICE_TITLE) {
                currentVoiceMode = MODE_VOICE_TEXT;
                updateNoteSpan();
                return replaceCurrentData(R.string.read, R.string.record);
            } else {
                currentVoiceMode = MODE_VOICE_TITLE;
                createNewEvent(mNoteTitleView.getText().toString(), mNoteTextView.getText().toString());
                changeDayEvent(calendarView.getSelectedDate(), true);
                calendarView.clearSelection();
                updateScreen(SCREEN_HOME, R.array.actions_home);
                updateNoteSpan();
                restoreTodayDate();
                moveToday();
                return true;
            }
        } else {
            if (currentVoiceMode == MODE_VOICE_TITLE) {
                updateScreen(SCREEN_CREATION, R.array.actions_list);
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

    @SuppressLint("NonConstantResourceId")
    private boolean preformReadScreenAction(int actionId) {
        switch (actionId) {
            case R.string.previous:
                changeReadEvent(-1);
                return false;
            case R.string.clear:
                isConfirmation = true;
                confirmQuestion(SCREEN_READING);
                return true;
            case R.string.yes:
                isConfirmation = false;
                cancelDeletion();
                deleteEvent();
                return true;
            case R.string.no:
                isConfirmation = false;
                cancelDeletion();
                return true;
            case R.string.next:
                changeReadEvent(1);
                return false;
            case R.string.back:
                readEventBack();
                return true;
        }
        return true;
    }

    private void readEvent() {
        if (eventsMap.size() > 0 && eventsMap.containsKey(nextEventKey)) {
            currentReadKey = nextEventKey;
            currentReadIndex = eventsMap.get(currentReadKey).size() - 1;
            updateScreen(SCREEN_READING, R.array.actions_read);
            updateEventInfo();
        } else {
            showMessage(R.string.no_events_found);
        }
    }

    private void updateEventInfo() {
        if (eventsMap.containsKey(currentReadKey)) {
            ArrayList<Event> dateEvents = eventsMap.get(currentReadKey);
            if (dateEvents.size() > 0) {
                Event currentEvent = dateEvents.get(currentReadIndex);
                CalendarDay selectedDay = CalendarDay.from(currentEvent.getYear(), currentEvent.getMonth(), currentEvent.getDay());
                updateSelectedDay(selectedDay);
                updateNoteInfo(currentEvent);
            }
        }
    }

    private void changeReadEvent(int direction) {
        if (currentReadIndex + direction >= 0 && currentReadIndex + direction < eventsMap.get(currentReadKey).size()) {
            currentReadIndex += direction;
        } else {
            ArrayList<Long> keyList = new ArrayList<>(eventsMap.keySet());
            int nextKeyIndex = keyList.indexOf(currentReadKey) + direction;
            int keyListSize = keyList.size();
            if (nextKeyIndex > keyListSize - 1) {
                nextKeyIndex -= keyListSize;
            } else if (nextKeyIndex < 0) {
                nextKeyIndex += keyListSize;
            }
            currentReadKey = keyList.get(nextKeyIndex);
            if (direction >= 0) {
                currentReadIndex = 0;
            } else {
                currentReadIndex = eventsMap.get(currentReadKey).size() - 1;
            }
        }
        updateEventInfo();
    }

    private void confirmQuestion(int mode) {
        updateScreen(mode, R.array.actions_confirmation);
        textSpeaker.speakAfter(getString(R.string.confirm));
    }

    private void cancelDeletion() {
        updateScreen(SCREEN_READING, R.array.actions_read);
    }

    private void deleteEvent() {
        Event event = eventsMap.get(currentReadKey).get(currentReadIndex);
        firebaseUserReference.child(DATABASE_NOTE + event.getId()).removeValue();
        changeDayEvent(CalendarDay.from(event.getYear(), event.getMonth(), event.getDay()), false);
        showMessageNow(R.string.event_removed);
        if (eventsMap.size() > 1) {
            ArrayList<Long> keyList = new ArrayList<>(eventsMap.keySet());
            if (keyList.indexOf(currentReadKey) == keyList.size() - 1) {
                changeReadEvent(-1);
            } else {
                changeReadEvent(1);
            }
        } else {
            showMessage(R.string.no_events_found);
            readEventBack();
        }
        eventsMap.remove(currentReadKey);
    }

    private void readEventBack() {
        updateScreen(SCREEN_HOME, R.array.actions_home);
        updateNoteSpan();
        calendarView.clearSelection();
        restoreTodayDate();
        moveToday();
    }

}