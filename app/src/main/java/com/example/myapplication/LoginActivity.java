package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signInWithGoogle());

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // Verification successful
                                    new UserTierManager(this).syncUserToCloud();
                                    
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    if (getIntent().getBooleanExtra("reauth_for_deletion", false)) {
                                        intent.putExtra("reauth_for_deletion", true);
                                    }
                                    
                                    // Check if this email was recently deleted
                                    String lastDeleted = getSharedPreferences("VitaeMonetizationPrefs", MODE_PRIVATE)
                                            .getString("last_deleted_email", "");
                                    
                                    if (!lastDeleted.isEmpty() && lastDeleted.equalsIgnoreCase(email)) {
                                        showNewAccountConfirmation(intent);
                                    } else {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    // Not verified - Show dialog and sign out
                                    showVerificationRequiredDialog(user);
                                    mAuth.signOut();
                                }
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.login_failed_prefix) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> showForgotPasswordDialog());

        findViewById(R.id.tvGoToSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        if (getIntent().getBooleanExtra("reauth_for_deletion", false)) {
            Toast.makeText(this, "Please log in again to confirm account deletion", Toast.LENGTH_LONG).show();
        }
    }

    private void showForgotPasswordDialog() {
        EditText resetEmail = new EditText(this);
        resetEmail.setHint(R.string.enter_your_email);
        resetEmail.setPadding(60, 40, 60, 40);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.reset_password)
            .setMessage(R.string.reset_password_msg)
            .setView(resetEmail)
            .setPositiveButton(R.string.send, (dialog, which) -> {
                String email = resetEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(this, R.string.email_is_required, Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.reset_link_sent_msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getString(R.string.error_prefix) + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showVerificationRequiredDialog(FirebaseUser user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.email_not_verified)
            .setMessage(R.string.verify_email_msg)
            .setPositiveButton(R.string.resend_email, (dialog, which) -> {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, R.string.verification_resent, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.failed_to_resend, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton(R.string.ok, null)
            .show();
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
                String errorMsg = getString(R.string.google_sign_in_failed_prefix) + e.getLocalizedMessage() + 
                                 "\nStatus Code: " + e.getStatusCode() + 
                                 "\nApp ID: " + getPackageName();
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                android.util.Log.e("LoginActivity", errorMsg, e);
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
                            // Sync immediately so their info appears in Admin Dashboard
                            new UserTierManager(this).syncUserToCloud(name, email);

                            if (user.isEmailVerified()) {
                                // Verified
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                if (getIntent().getBooleanExtra("reauth_for_deletion", false)) {
                                    intent.putExtra("reauth_for_deletion", true);
                                }

                                // Check if this email was recently deleted
                                String lastDeleted = getSharedPreferences("VitaeMonetizationPrefs", MODE_PRIVATE)
                                        .getString("last_deleted_email", "");

                                if (!lastDeleted.isEmpty() && lastDeleted.equalsIgnoreCase(email)) {
                                    showNewAccountConfirmation(intent);
                                } else {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                // Not verified
                                showVerificationRequiredDialog(user);
                                mAuth.signOut();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.firebase_auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNewAccountConfirmation(Intent nextIntent) {
        FirebaseUser user = mAuth.getCurrentUser();
        String email = (user != null) ? user.getEmail() : "";
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.new_account_required)
            .setMessage(getString(R.string.new_account_msg, email))
            .setPositiveButton(R.string.btn_create_new, (dialog, which) -> {
                // Clear the tracking flag once they acknowledge
                getSharedPreferences("VitaeMonetizationPrefs", MODE_PRIVATE)
                        .edit().remove("last_deleted_email").apply();
                
                nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(nextIntent);
                finish();
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                mAuth.signOut();
            })
            .setCancelable(false)
            .show();
    }
}
