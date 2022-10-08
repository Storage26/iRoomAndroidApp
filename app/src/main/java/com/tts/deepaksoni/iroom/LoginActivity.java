package com.tts.deepaksoni.iroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int GOOGLE_SI_CODE = 2;

    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    LinearLayout loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckLoginStatus();
        setContentView(R.layout.activity_login);

        // Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Variables
        firebaseAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.login_btn);

        // Initialize
        InitializeGoogle();
    }

    private void InitializeGoogle()
    {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("125969034370-k5bbcllidv6ismg78aj22b3ea9aj0f52.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        loginButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, GOOGLE_SI_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SI_CODE)
        {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful())
            {
                // Success
                try
                {
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);

                    if (googleSignInAccount != null)
                    {
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

                        // Firebase Sign In
                        firebaseAuth.signInWithCredential(authCredential)
                                .addOnCompleteListener(this, task -> {
                                    // Check task
                                    if (task.isSuccessful())
                                    {
                                        FirebaseUser user = task.getResult().getUser();
                                        String photoUri = "";

                                        if (user != null)
                                        {
                                            if (user.getPhotoUrl() != null)
                                            {
                                                photoUri = user.getPhotoUrl().toString();
                                            }

                                            PostLogin(user.getDisplayName(), photoUri);
                                        }
                                        else
                                        {
                                            firebaseAuth.signOut();
                                            Toast.makeText(this, "Unable to sign in", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(this, e -> Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show());
                    }
                }
                catch (ApiException ignored) {}
            }
            else
            {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void PostLogin(String displayNameGiven, String pictureUri)
    {
        // Fetch Data
        String displayName = displayNameGiven;
        if (displayName == null || displayName.isEmpty())
        {
            displayName = "";
        }
        else
        {
            displayName = displayName.trim();
        }

        // Save Name & Picture
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("DisplayName", displayName);
        editor.putString("DisplayPicture", pictureUri);
        editor.apply();

        // Logout
        firebaseAuth.signOut();
        googleSignInClient.signOut();

        // Home
        LoggedIn();
    }

    private void CheckLoginStatus()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        String name = sharedPreferences.getString("DisplayName", "");
        if (!name.trim().isEmpty())
        {
            LoggedIn();
        }
    }

    private void LoggedIn()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

        finish();
    }
}