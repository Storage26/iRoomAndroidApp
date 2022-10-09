package com.tts.deepaksoni.iroom;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

public class HomeActivity extends AppCompatActivity {

    TextView joinButton;
    TextView createButton;

    ImageView profilePicture;

    FirebaseAuth firebaseAuth;

    ImageView logoutButton;

    RequestQueue queue;

    Dialog dialog;

    boolean taskRunning;

    EditText username_input;
    EditText room_code_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // StartUp
        StartUp();

        // Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Variables
        firebaseAuth = FirebaseAuth.getInstance();
        taskRunning = false;
        joinButton = findViewById(R.id.joinBtn);
        createButton = findViewById(R.id.create_room_btn);
        queue = Volley.newRequestQueue(this);
        username_input = findViewById(R.id.username_input);
        room_code_input = findViewById(R.id.room_code_input);
        logoutButton = findViewById(R.id.logout_btn);
        profilePicture = findViewById(R.id.profile_pic_home);

        // Load Profile Picture
        LoadProfilePicture();

        // Load Default
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        String default_u = sharedPreferences.getString("DisplayName", "");
        if (!default_u.trim().isEmpty())
        {
            username_input.setText(default_u);
        }

        // Dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Listeners
        logoutButton.setOnClickListener(view -> Logout());
        joinButton.setOnClickListener(v -> {
            if (!taskRunning)
            {
                taskRunning = true;
                dialog.show();

                SharedPreferences sharedPreferences1 = getSharedPreferences("Data", MODE_PRIVATE);
                String username = sharedPreferences1.getString("DisplayName", "").trim();
                if (username.isEmpty())
                {
                    username = "Human Being";
                }
                String room_code = room_code_input.getText().toString().trim();

                if (!room_code.isEmpty())
                {
                    // ACTUAL REQUEST
                    String finalUsername = username;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://iroom-site.herokuapp.com/room-status?id=" + room_code, null, response -> {
                        try
                        {
                            if (response.has("success"))
                            {
                                if (response.getBoolean("success"))
                                {
                                    if (response.has("roomActive"))
                                    {
                                        if (response.getBoolean("roomActive"))
                                        {
                                            JoinRoom(finalUsername, room_code);
                                        }
                                        else
                                        {
                                            Toast.makeText(this, "No such room", Toast.LENGTH_SHORT).show();
                                        }

                                        dialog.hide();
                                        taskRunning = false;
                                    }
                                    else
                                    {
                                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else
                                {
                                    if (response.has("error"))
                                    {
                                        String error = response.getString("error");
                                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                dialog.hide();
                                taskRunning = false;
                            }
                            else
                            {
                                dialog.hide();
                                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                            }

                            dialog.hide();
                            taskRunning = false;
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                            dialog.hide();
                            taskRunning = false;
                            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                        dialog.hide();
                        taskRunning = false;
                    });

                    queue.add(request);
                }
                else
                {
                    room_code_input.setError("Room Code is required");
                    room_code_input.requestFocus();

                    dialog.hide();
                    taskRunning = false;
                }
            }
        });

        createButton.setOnClickListener(v -> {
            if (!taskRunning)
            {
                taskRunning = true;
                dialog.show();

                SharedPreferences sharedPreferences1 = getSharedPreferences("Data", MODE_PRIVATE);
                String username = sharedPreferences1.getString("DisplayName", "").trim();
                if (username.isEmpty())
                {
                    username = "Human Being";
                }

                // ACTUAL REQUEST
                String finalUsername = username;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://iroom-site.herokuapp.com/create-room?name=" + username, null, response -> {
                    try
                    {
                        if (response.has("success"))
                        {
                            if (response.getBoolean("success"))
                            {
                                if (response.has("roomCode"))
                                {
                                    String grc = response.getString("roomCode");
                                    JoinRoom(finalUsername, grc);

                                    dialog.hide();
                                    taskRunning = false;
                                }
                                else
                                {
                                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else
                            {
                                if (response.has("error"))
                                {
                                    String error = response.getString("error");
                                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                                }

                            }
                            dialog.hide();
                            taskRunning = false;
                        }
                        else
                        {
                            dialog.hide();
                            taskRunning = false;
                            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                        }

                        dialog.hide();
                        taskRunning = false;
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        dialog.hide();
                        taskRunning = false;
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    dialog.hide();
                    taskRunning = false;
                });

                queue.add(request);
            }
        });
    }

    private void LoadProfilePicture()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        String profile_picture = sharedPreferences.getString("DisplayPicture", "");
        if (!profile_picture.isEmpty())
        {
            Glide.with(this).load(profile_picture).placeholder(R.drawable.user).into(profilePicture);
        }
    }

    private void JoinRoom(String username, String code)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("code", code);
        startActivity(intent);
    }

    private void StartUp()
    {
        AsyncTask.execute(() -> {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://iroom-site.herokuapp.com/i", null, null, null);
            requestQueue.add(request);
        });
    }

    private void Logout()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("DisplayName");
        editor.remove("DisplayPicture");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}