package com.unicef.dreamapp2.ui.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.unicef.dreamapp2.BaseContract;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.adapter.ChannelAdapter;
import com.unicef.dreamapp2.model.ChannelModel;

import java.util.ArrayList;

/**
 * @author Iman Augustine*/

public class ChatListActivity extends AppCompatActivity {

    // Global variables
    private DatabaseReference channelsDatabase; // Channels database
    private RecyclerView channelsList; // Channels list
    private ChannelAdapter channelAdapter; // Channels adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);

        initView();
    }

    // Initializes views
    private void initView() {
        channelsList = findViewById(R.id.channelsList);
        channelAdapter = new ChannelAdapter(new ArrayList<ChannelModel>(), BaseContract.);
    }
}