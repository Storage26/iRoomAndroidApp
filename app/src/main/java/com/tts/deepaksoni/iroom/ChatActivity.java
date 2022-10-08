package com.tts.deepaksoni.iroom;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatActivity extends AppCompatActivity {

    ArrayList<Integer> type;
    ArrayList<String> text;
    ArrayList<String> sn;

    LinearLayout typing_lyt;
    ImageView typing_img;
    TextView typing_text;

    String ti_member;
    String ti_member_id;
    long ti_time;

    Dialog dialog;

    Handler handler;
    Runnable runnable;

    LinearLayout msgBox;

    ImageView back;

    AdapterA adapter;
    ListView listView;
    ImageView sendButton;
    ImageView endRoom;
    EditText input;

    TextView roomLabel;

    String username;
    String code;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        socket = null;

        // Defaults
        ti_time = 0;
        ti_member = "";
        ti_member_id = "";

        // Dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // Action Bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // GET
        username = getIntent().getExtras().getString("username").trim();
        code = getIntent().getExtras().getString("code").trim();

        if (username.isEmpty() || code.isEmpty())
        {
            Toast.makeText(this, "Failed to join room", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Variable
        type = new ArrayList<>();
        text = new ArrayList<>();
        sn = new ArrayList<>();
        listView = findViewById(R.id.listView);
        sendButton = findViewById(R.id.send_btn);
        input = findViewById(R.id.input);
        msgBox = findViewById(R.id.msg_box);
        back = findViewById(R.id.back_btn);
        roomLabel = findViewById(R.id.room_label);
        endRoom = findViewById(R.id.endRoom);
        typing_lyt = findViewById(R.id.typing_lyt);
        typing_text = findViewById(R.id.typing_text);
        typing_img = findViewById(R.id.typing_img);

        // Animations
        Animation blinking_anim = new AlphaAnimation(0.0f, 1.0f);
        blinking_anim.setDuration(250);
        blinking_anim.setRepeatMode(Animation.REVERSE);
        blinking_anim.setRepeatCount(Animation.INFINITE);
        typing_img.startAnimation(blinking_anim);

        back.setOnClickListener(v -> finish());
        String dhh = "Room - " + code;
        roomLabel.setText(dhh);

        // Adapter Configuration
        AdapterDefine();
        ServerMessage("iRoom");

        // OnClick Listeners
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (socket != null && socket.connected())
                {
                    socket.emit("typing");
                }
            }
        });
        endRoom.setOnClickListener(view -> {
            if (socket != null)
            {
                socket.emit("end_room");
            }
        });
        sendButton.setOnClickListener(view -> {
            String msg = input.getText().toString().trim();

            if (!msg.isEmpty())
            {
                Send(msg);
                input.setText("");
            }
        });

        // Socket Connection
        SocketConnect();

        // Loop
        StartLoop();
    }

    private void ToggleTyping(boolean toggle)
    {
        if (toggle) typing_lyt.setVisibility(View.VISIBLE);
        else typing_lyt.setVisibility(View.GONE);
    }

    private void SetTyping(String s)
    {
        String ss = s + " is typing...";
        typing_text.setText(ss);
    }

    private void StartLoop()
    {
        handler = new Handler();
        runnable = () -> {
            // Check
            if (ti_member.isEmpty() || ti_time == 0)
            {
                ToggleTyping(false);
            }
            else
            {
                long current_time = System.currentTimeMillis();

                if (current_time - ti_time < 2000)
                {
                    SetTyping(ti_member);
                    ToggleTyping(true);
                }
                else
                {
                    ToggleTyping(false);
                }
            }

            // Repeat
            handler.postDelayed(runnable, 1);
        };

        handler.post(runnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (socket != null)
        {
            handler.removeCallbacks(runnable);
            socket.disconnect();
            socket = null;
            finish();
        }
    }

    private void AdapterDefine()
    {
        adapter = new AdapterA(this, type, text, sn);
        listView.setAdapter(adapter);
    }

    private void Send(String s)
    {
        if (socket != null)
        {
            // Send online
            socket.emit("message", s);

            // Add offline
            type.add(1);
            text.add(s.trim());
            sn.add("");

            adapter.notifyDataSetChanged();
            listView.setSelection(adapter.getCount() - 1);
        }
    }

    private void ServerMessage(String s)
    {
        type.add(2);
        text.add(s.trim());
        sn.add("");

        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
    }

    private void Receive(String s, String sender_name)
    {
        type.add(0);
        text.add(s.trim());
        sn.add(sender_name);

        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
    }

    private void SocketConnect()
    {
        dialog.show();

        try
        {
            IO.Options options = new IO.Options();
            options.reconnection = false;
            options.query = "user_name=" + username + "&room_id=" + code;
            socket = IO.socket("https://voila-meetup.herokuapp.com/active_room", options);
        }
        catch (URISyntaxException e)
        {
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Connect
        socket.connect();
        socket.on("connect", args -> runOnUiThread(() -> {
            dialog.hide();
            Call("Connected");
        }));
        socket.on("connect_error", args -> runOnUiThread(() -> {
            Call("Unable to join room");
            finish();
        }));
        socket.on("disconnect", args -> runOnUiThread(() -> {
            Call("Disconnected");
            finish();
        }));
        socket.on("room_ended", args -> runOnUiThread(() -> {
            String _name = Arrays.toString(args).substring(1);
            String name = _name.substring(0, _name.length() - 1);
            ServerMessage("Room ended by " + name);
            msgBox.setVisibility(View.GONE);
        }));
        socket.on("user_left", args -> runOnUiThread(() -> {
            String _name = Arrays.toString(args).substring(1);
            String name = _name.substring(0, _name.length() - 1);
            ServerMessage(name + " left");
        }));
        socket.on("user_joined", args -> runOnUiThread(() -> {
            String _name = Arrays.toString(args).substring(1);
            String name = _name.substring(0, _name.length() - 1);
            ServerMessage(name + " joined");
        }));
        socket.on("receive_message", obj -> runOnUiThread(() -> {
            try
            {
                JSONArray jsonArray = new JSONArray(Arrays.toString(obj));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject sender = jsonObject.getJSONObject("sender");
                String sender_id = sender.getString("id");
                String sender_name = sender.getString("name");
                String message = jsonObject.getString("message");

                if (socket != null && !sender_id.equals(socket.id()))
                {
                    Receive(message, sender_name);
                }
            }
            catch (JSONException e) {/**/}
        }));
        socket.on("typing_info", obj -> runOnUiThread(() -> {
            try
            {
                JSONArray jsonArray = new JSONArray(Arrays.toString(obj));
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                ti_member = jsonObject.getString("member");
                ti_member_id = jsonObject.getString("member_id");
                ti_time = jsonObject.getLong("time");
            }
            catch (JSONException e) {/**/}
        }));
    }

    private void Call(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}