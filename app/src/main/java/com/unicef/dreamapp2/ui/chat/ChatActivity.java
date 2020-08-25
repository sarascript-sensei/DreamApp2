package com.unicef.dreamapp2.ui.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.application.Utility;
import com.unicef.dreamapp2.model.ChatModel;

/**
 * @author Iman Augustine
 *
 * ChatActivity.
 * */

public class ChatActivity extends AppCompatActivity {

    // Firebase database reference
    private DatabaseReference messageRef;
    // String
    private String customerID = null;
    private String volunteerID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initializes views
        initView();

        customerID = getIntent().getStringExtra("customerID")+"kfnsfnd";
        volunteerID = getIntent().getStringExtra("volunteerID");

        messageRef = FirebaseDatabase.getInstance().getReference()
                .child(Utility.MESSAGES)
                .child(customerID)
                .child(volunteerID);

        String key = messageRef.push().getKey();
        ChatModel chatModel = new ChatModel();
        chatModel.messageId = key;
        chatModel.chatId = "chat_id";
        chatModel.id = 1;
        chatModel.senderId = volunteerID;
        chatModel.messageType = "TEXT";
        chatModel.message = "Programming is awesome!!!";
        assert key != null;
        messageRef = messageRef.child(key);
        messageRef.setValue(chatModel);
    }

    //
    private void initView() {

    }
}