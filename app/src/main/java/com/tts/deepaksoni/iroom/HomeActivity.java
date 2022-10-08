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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

public class HomeActivity extends AppCompatActivity {

    TextView joinButton;
    TextView createButton;

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

                String username = username_input.getText().toString().trim();
                String room_code = room_code_input.getText().toString().trim();

                if (!username.isEmpty())
                {
                    if (!room_code.isEmpty())
                    {
                        // ACTUAL REQUEST
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
                                                JoinRoom(username, room_code);
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
                else
                {
                    username_input.setError("Username is required");
                    username_input.requestFocus();

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

                String username = username_input.getText().toString().trim();

                if (!username.isEmpty())
                {
                    // ACTUAL REQUEST
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
                                        JoinRoom(username, grc);

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
                else
                {
                    username_input.setError("Username is required");
                    username_input.requestFocus();

                    dialog.hide();
                    taskRunning = false;
                }
            }
        });
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