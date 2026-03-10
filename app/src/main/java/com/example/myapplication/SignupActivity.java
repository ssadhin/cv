package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9002;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etFullName, etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Successful Account Creation
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // 1. Update Firebase Profile Name
                                String finalName = name.isEmpty() ? getString(R.string.new_user) : name;
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(finalName)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                    // 2. Sync to Registry so Admin Dashboard knows they exist
                                    new UserTierManager(SignupActivity.this).syncUserToCloud(finalName, email);

                                    // 3. Force Email Verification Flow
                                    user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this,
                                                R.string.account_created_verify_msg,
                                                Toast.LENGTH_LONG).show();
                                            Log.d("SignupActivity", "Verification email sent.");
                                        } else {
                                            Log.e("SignupActivity", "Failed to send verification email.", emailTask.getException());
                                            Toast.makeText(SignupActivity.this,
                                                R.string.account_created_email_fail_msg,
                                                Toast.LENGTH_LONG).show();
                                        }

                                        // 4. SECURE LOGOUT: Force them out until they verify
                                        mAuth.signOut();

                                        // 5. Return to Login
                                        finish();
                                    });
                                });
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.signup_failed_prefix) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.btnGoogleSignUp).setOnClickListener(v -> signInWithGoogle());

        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> finish());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName(), account.getEmail());
                }
            } catch (ApiException e) {
                String errorMsg = getString(R.string.google_sign_up_failed_prefix) + e.getLocalizedMessage() + 
                                 "\nStatus Code: " + e.getStatusCode() + 
                                 "\nApp ID: " + getPackageName();
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                android.util.Log.e("SignupActivity", errorMsg, e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String name, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Sync to Cloudflare immediately so they appear in the dashboard
                            new UserTierManager(this).syncUserToCloud(name, email);

                            if (user.isEmailVerified()) {
                                // Verified (Common for Google)
                                finish();
                            } else {
                                // Not verified - send email and block
                                user.sendEmailVerification();
                                mAuth.signOut();
                                Toast.makeText(this, R.string.verify_google_email_msg, Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.firebase_auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
