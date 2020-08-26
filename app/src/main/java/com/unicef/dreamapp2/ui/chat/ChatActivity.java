package com.unicef.dreamapp2.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.adapter.MessageAdapter;
import com.unicef.dreamapp2.application.MyApplication;
import com.unicef.dreamapp2.application.Utility;
import com.unicef.dreamapp2.model.ChatModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Iman Augustine
 *
 * ChatActivity. This is basically where users interact with each other
 * through send text messages to each other. It is build entirely
 * on Firebase realtime database.
 *
 * */

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // Firebase database reference
    private DatabaseReference messageRef;

    // Adapters
    private MessageAdapter messageAdapter;

    // String
    public static final String MESSAGE_TYPE = "TEXT";
    private String customerID = null;
    private String volunteerID = null;
    private String chatID = null;
    private String key = null;

    // RecyclerView
    private RecyclerView messageList;

    // ImageView
    private ImageView sendBtn;

    // EditText
    private EditText messageEdit;

    // Chat model
    private ChatModel chatModel;

    // On creation of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Accessing customer and volunteer id's
        customerID = getIntent().getStringExtra("customerID"); // Customer ID
        volunteerID = getIntent().getStringExtra("volunteerID"); // Volunteer ID
        chatID = customerID + "_" + volunteerID; // Conjured up chat id

        // Firebase database
        messageRef = FirebaseDatabase.getInstance().getReference()
                .child(Utility.MESSAGES); // Messages database
                // Chat id [customerID + volunteerID]

        // Initializes views
        initView();

        // Adapter
        messageAdapter = new MessageAdapter(new ArrayList<String>(), MyApplication.getInstance());
        messageList.setLayoutManager(new LinearLayoutManager(this));
        messageList.setAdapter(messageAdapter);

        // Creating chat model
        chatModel = new ChatModel();
        chatModel.senderId = FirebaseAuth.getInstance().getUid(); // The sender's uid
        chatModel.messageType = MESSAGE_TYPE; // Message type should be TEXT

        // Sets up listeners
        setUpListeners();
    }

    // Initializes views
    private void initView() {
        sendBtn = findViewById(R.id.sendMessage); // Send button
        messageEdit = findViewById(R.id.messageEdit); // Message text edit
        messageList = findViewById(R.id.messageList); // RecyclerView
    }

    // Sets up listeners on widgets
    private void setUpListeners() {
        // Setting on click listener
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the entered text and insert into the database
                sendMessage(messageEdit.getText().toString());
            }
        });

        // Listens to value changes etc inside Messages database
        messageRef.addValueEventListener(new ValueEventListener() {
            // On data change
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageAdapter.setValues(extractMessageList(snapshot));
                messageAdapter.notifyDataSetChanged();
                // Log.d(TAG, "extractArrayList: "+snapshot.toString());
            }

            // On cancellation
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,"Message cancelled!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Sends message
    private void sendMessage(String message) {
        if(!message.isEmpty()) { // If there's a message text entered
            key = messageRef.push().getKey(); // Gets generated key
            chatModel.chatId = key; // Chat id set to key
            chatModel.messageId = key; // Message id
            chatModel.message = message; // Message body
            messageRef.child(chatID).child(key).setValue(chatModel); // "Sending" the actual message
            messageEdit.setText(null); // Resetting mesage text field
        } else {
            // Prompt the user to enter message
            Toast.makeText(this, "Введите сообщение!", Toast.LENGTH_SHORT).show();
        }
    }

    // Extract array list from the map returned on data change in Firebase
    private ArrayList<String> extractMessageList(DataSnapshot snapshot) {
        ArrayList<String> messagesList = new ArrayList<>();
        HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
        HashMap<String, Object> chatMap = (HashMap<String, Object>) map.get(chatID);
        HashMap<String, Object> messages;
        Object[] keySet = (Object[]) chatMap.keySet().toArray();

        StringBuilder message = new StringBuilder();
        for(int i=0; i<keySet.length; i++) {
            messages = (HashMap<String, Object>) chatMap.get(keySet[i].toString());
            // message.append(messages.get("message").toString()).append("\n");
            messagesList.add(messages.get("message").toString());
        }
        // Log.d(TAG, "extractArrayList: " + messagesList.toString());
        return messagesList;
    }

    // On pause
    @Override
    protected void onPause() {
        super.onPause();
    }

    // On destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}