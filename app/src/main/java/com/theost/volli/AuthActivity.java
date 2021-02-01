package com.theost.volli;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.stephentuso.welcome.WelcomeHelper;
import com.theost.volli.utils.AuthUtils;
import com.theost.volli.utils.DisplayUtils;
import com.theost.volli.utils.NetworkUtils;
import com.theost.volli.utils.PrefUtils;

public class AuthActivity extends AppCompatActivity {

    private static final int REQUEST_SIGN_UP = 0;
    private static final int REQUEST_SIGN_IN = 1;
    private static final int REQUEST_FORGOT_PASSWORD = 2;
    private static final int REQUEST_SIGN_UP_NAME = 3;

    private static final int PASSWORD_MIN_LENGTH = 6;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout usernameLayout;

    private MaterialButton signInButton;
    private MaterialButton backButton;

    private String authUsername;
    private String authEmail;

    private boolean isAuthorized;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        showWelcomeScreen();

        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        usernameLayout = findViewById(R.id.name_layout);

        signInButton = findViewById(R.id.sign_in_button);
        backButton = findViewById(R.id.back_button);

        signInButton.setOnClickListener(v -> onSignIn());
        backButton.setOnClickListener(v -> onBack());
        findViewById(R.id.sign_up_button).setOnClickListener(v -> onSignUp());
        findViewById(R.id.forgot_password_button).setOnClickListener(v -> onForgotPassword());
    }

    @Override
    public void onBackPressed() {
        if (isAuthorized) {
            super.onBackPressed();
        }
    }

    private void showWelcomeScreen() {
        WelcomeHelper welcomeScreen = new WelcomeHelper(this, WelcomeActivity.class);
        welcomeScreen.forceShow();
    }

    private String getFieldText(TextInputLayout parent) {
        EditText field = parent.getEditText();
        if (field != null) {
            return field.getText().toString().trim().toLowerCase();
        } else {
            return "";
        }
    }

    private boolean isAuthCorrect(int requestCode) {
        DisplayUtils.hideKeyboard(this);
        if (NetworkUtils.isNetworkAvailable(this)) {
            if (requestCode == REQUEST_SIGN_UP || requestCode == REQUEST_SIGN_IN || requestCode == REQUEST_FORGOT_PASSWORD) {
                String authPassword = getFieldText(passwordLayout);
                authEmail = getFieldText(emailLayout);
                if (AuthUtils.isValidEmail(authEmail)) {
                    emailLayout.setError(null);
                    if (requestCode == REQUEST_FORGOT_PASSWORD || AuthUtils.isValidPassword(authPassword, PASSWORD_MIN_LENGTH)) {
                        passwordLayout.setError(null);
                        if (requestCode == REQUEST_SIGN_IN) {
                            return AuthUtils.requestAuth();
                        }
                        return true;
                    } else {
                        passwordLayout.setError(getString(R.string.password_is_too_short));
                    }
                } else {
                    emailLayout.setError(getString(R.string.wrong_email));
                }
            } else if (requestCode == REQUEST_SIGN_UP_NAME) {
                return true;
            }
        } else {
            DisplayUtils.showToast(this, R.string.network_not_available);
        }
        return false;
    }

    private void updateNameField(boolean isVisible) {
        if (isVisible) {
            emailLayout.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.GONE);
            signInButton.setVisibility(View.GONE);
            usernameLayout.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
        } else {
            emailLayout.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            usernameLayout.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
        }
    }

    private void onSignUp() {
        if (usernameLayout.getVisibility() == View.GONE) {
            if (isAuthCorrect(REQUEST_SIGN_UP)) {
                updateNameField(true);
            }
        } else {
            authUsername = getFieldText(usernameLayout).trim();
            if (isAuthCorrect(REQUEST_SIGN_UP_NAME) && authUsername.length() > 0) {
                // todo add account to database
                authorizeUser();
            } else {
                usernameLayout.setError(getString(R.string.wrong_name));
            }
        }
    }

    private void onSignIn() {
        if (isAuthCorrect(REQUEST_SIGN_IN)) {
            authorizeUser();
        }
    }

    private void onForgotPassword() {
        if (isAuthCorrect(REQUEST_FORGOT_PASSWORD)) {
            DisplayUtils.showToast(this, R.string.feature_not_available);
        }
    }

    private void onBack() {
        updateNameField(false);
    }

    private void authorizeUser() {
        isAuthorized = true;
        updateSharedPreferences();
        onBackPressed();
    }

    private void updateSharedPreferences() {
        SharedPreferences preferences = PrefUtils.getSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PrefUtils.PREFERENCES_KEY_USERNAME, authUsername);
        editor.putString(PrefUtils.PREFERENCES_KEY_EMAIL, authEmail);
        editor.apply();
    }

}
