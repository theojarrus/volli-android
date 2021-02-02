package com.theost.volli;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.stephentuso.welcome.WelcomeHelper;
import com.theost.volli.utils.AuthUtils;
import com.theost.volli.utils.DisplayUtils;
import com.theost.volli.utils.NetworkUtils;

public class AuthActivity extends AppCompatActivity {

    private static final int REQUEST_SIGN_UP = 0;
    private static final int REQUEST_SIGN_IN = 1;
    private static final int REQUEST_FORGOT_PASSWORD = 2;
    private static final int REQUEST_SIGN_UP_NAME = 3;

    private static final int PASSWORD_MIN_LENGTH = 6;

    private FirebaseAuth firebaseAuth;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout usernameLayout;

    private MaterialButton signInButton;
    private MaterialButton backButton;
    private TextView forgotPasswordButton;

    private String authEmail;
    private String authPassword;

    private boolean isAuthorized;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        showWelcomeScreen();

        firebaseAuth = FirebaseAuth.getInstance();

        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        usernameLayout = findViewById(R.id.name_layout);

        signInButton = findViewById(R.id.sign_in_button);
        backButton = findViewById(R.id.back_button);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);

        signInButton.setOnClickListener(v -> onSignIn());
        backButton.setOnClickListener(v -> onBack());
        forgotPasswordButton.setOnClickListener(v -> onForgotPassword());
        findViewById(R.id.sign_up_button).setOnClickListener(v -> onSignUp());
    }

    @Override
    public void onBackPressed() {
        if (isAuthorized) {
            super.onBackPressed();
        }
    }

    private void authSignIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                authorizeUser();
            } else {
                displayAuthError(task);
            }
        });
    }

    private void authSignUp(String email, String password, String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    user.updateProfile(profileUpdates);
                    authorizeUser();
                }
            } else {
                onBack();
                displayAuthError(task);
            }
        });
    }

    private void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DisplayUtils.showToast(this, getString(R.string.auth_restore_password) + email);
            } else {
                displayAuthError(task);
            }
        });
    }

    private void displayAuthError(Task task) {
        try {
            Exception e = task.getException();
            if (e != null) throw e;
        } catch (FirebaseAuthInvalidUserException e) {
            emailLayout.setError(getString(R.string.email_not_exist));
        } catch (FirebaseAuthInvalidCredentialsException e) {
            passwordLayout.setError(getString(R.string.wrong_password));
        } catch (FirebaseAuthUserCollisionException e) {
            emailLayout.setError(getString(R.string.email_already_exist));
        } catch (FirebaseNetworkException e) {
            DisplayUtils.showToast(this, R.string.network_not_available);
        } catch (Exception e) {
            DisplayUtils.showToast(this, R.string.unknown_error);
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
                authEmail = getFieldText(emailLayout);
                authPassword = getFieldText(passwordLayout);
                if (AuthUtils.isValidEmail(authEmail)) {
                    emailLayout.setError(null);
                    if (requestCode == REQUEST_FORGOT_PASSWORD || AuthUtils.isValidPassword(authPassword, PASSWORD_MIN_LENGTH)) {
                        passwordLayout.setError(null);
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
            forgotPasswordButton.setVisibility(View.GONE);
            usernameLayout.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
        } else {
            emailLayout.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            forgotPasswordButton.setVisibility(View.VISIBLE);
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
            String authUsername = getFieldText(usernameLayout).trim();
            if (isAuthCorrect(REQUEST_SIGN_UP_NAME) && authUsername.length() > 0) {
                authSignUp(authEmail, authPassword, authUsername);
            } else {
                usernameLayout.setError(getString(R.string.wrong_name));
            }
        }
    }

    private void onSignIn() {
        if (isAuthCorrect(REQUEST_SIGN_IN)) {
            authSignIn(authEmail, authPassword);
        }
    }

    private void onForgotPassword() {
        if (isAuthCorrect(REQUEST_FORGOT_PASSWORD)) {
            resetPassword(authEmail);
        }
    }

    private void onBack() {
        DisplayUtils.hideKeyboard(this);
        updateNameField(false);
    }

    private void authorizeUser() {
        isAuthorized = true;
        onBackPressed();
    }

}
