package com.tts.deepaksoni.iroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.tts.deepaksoni.iroom.R;

import java.util.ArrayList;
import java.util.logging.Handler;

public class AdapterA extends ArrayAdapter<String>
{
    Activity activity;
    ArrayList<Integer> type;
    ArrayList<String> text;
    ArrayList<String> sn;
    ArrayList<String> picture;

    public AdapterA(Activity activity, ArrayList<Integer> type, ArrayList<String> text, ArrayList<String> sn, ArrayList<String> picture) {
        super(activity, R.layout.message, text);

        this.activity = activity;
        this.type = type;
        this.text = text;
        this.sn = sn;
        this.picture = picture;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View row = activity.getLayoutInflater().inflate(R.layout.message, parent, false);

        TextView msg = row.findViewById(R.id.msg);
        TextView msg2 = row.findViewById(R.id.msg_2);
        TextView msg2_sn = row.findViewById(R.id.msg_2_head);
        LinearLayout sent_lyt = row.findViewById(R.id.sent_lyt);
        LinearLayout rec_lyt = row.findViewById(R.id.received_lyt);
        TextView other_lyt = row.findViewById(R.id.other_lyt);
        TextView ended_lyt = row.findViewById(R.id.ended_lyt);

        if (type.get(position) == 0)
        {
            // Received
            msg2_sn.setText(sn.get(position));
            msg2.setText(text.get(position));
            rec_lyt.setVisibility(View.VISIBLE);

            // Check picture
            if (!picture.get(position).isEmpty())
            {
                ImageView imageView = row.findViewById(R.id.sender_img_msg_rec);
                Glide.with(getContext()).load(Uri.parse(picture.get(position))).into(imageView);
            }
        }
        else if (type.get(position) == 1)
        {
            // Sent
            msg.setText(text.get(position));
            sent_lyt.setVisibility(View.VISIBLE);
        }
        else if (type.get(position) == 10)
        {
            // Room Ended
            ended_lyt.setText(text.get(position));
            ended_lyt.setVisibility(View.VISIBLE);
        }
        else
        {
            // Other
            other_lyt.setText(text.get(position));
            other_lyt.setVisibility(View.VISIBLE);
        }

        return row;
    }
}
