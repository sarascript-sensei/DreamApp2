package com.unicef.dreamapp2.ui.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.unicef.dreamapp2.application.MyPreferenceManager;
import com.unicef.dreamapp2.application.Utility;
import com.unicef.dreamapp2.model.ChatModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
    private DatabaseReference customerDatabase;
    private DatabaseReference volunteerDatabase;

    // Adapters
    private MessageAdapter messageAdapter;

    // String
    public static final String MESSAGE_TYPE = "TEXT";
    private String chatterName = null; // Chatter name [could be either Customer or Volunteer]
    private String customerId = null; // Customer ID
    private String volunteerId = null; // Volunteer ID
    private String customerName = null; // Customer name
    private String volunteerName = null; // Volunteer name
    private String mUserType = null; // User role
    private String chatID = null; // Chat ID
    private String key = null; // Key
    private String userId = null; // User id

    // RecyclerView
    private RecyclerView messageList;
    private LinearLayoutManager layoutManager; // layout manager

    // ImageView
    private ImageView sendBtn;

    // EditText
    private EditText messageEdit;

    // Chat model
    private ChatModel chatModel;

    // Menu item
    private MenuItem like;
    private MenuItem dislike;

    // Shared preferences
    private SharedPreferences shared;

    // On creation of activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            // Initializing variables
            initValues();

            // Creating channel
            setupChannel();

            // Initializes views
            initView();

            // Sets up listeners
            setUpListeners();

            // Load messages
            loadMessages();

        } catch(NullPointerException error) {
            Log.d(TAG, "onCreate: error: "+error);
        }
    }

    // Toolbar menu inflated
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Inflating menu
        this.like = menu.findItem(R.id.thumbUp); // Accessing Like menu item
        this.dislike = menu.findItem(R.id.thumbDown); // Accessing Dislike menu item
        return true;
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.thumbUp: // User is thankful for the volunteer
                Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.thumbDown: // Did not help or did it badly
                Toast.makeText(this, "Disliked!", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home: // On home arrow pressed
                finish();
        }
        return true;
    }

    // Initialize variable values
    private void initValues() {
        // Firebase Messages database
        messageRef = FirebaseDatabase.getInstance().getReference().child(Utility.MESSAGES); // Messages database

        // Regular users database
        customerDatabase = FirebaseDatabase.getInstance().getReference().child(Utility.USERS)
                .child(MyPreferenceManager.REGULAR_USER);

        // Volunteer users database
        volunteerDatabase = FirebaseDatabase.getInstance().getReference().child(Utility.USERS)
                .child(MyPreferenceManager.VOLUNTEER);

        // Shared preferences
        shared = MyPreferenceManager.getMySharedPreferences(this);

        // User type
        mUserType = shared.getString(MyPreferenceManager.USER_TYPE, null);

        // Current user ID
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Setting correspondent's name as toolbar title and enabling home button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Creating chat model with sender id and message type
        chatModel = new ChatModel(userId, MESSAGE_TYPE);
    }

    // Initializes views
    private void initView() {
        sendBtn = findViewById(R.id.sendMessage); // Send button
        messageEdit = findViewById(R.id.messageEdit); // Message text edit
        messageEdit.requestFocus(); // Requesting focus on edit text
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        messageList = findViewById(R.id.messageList); // RecyclerView
        // Setting adapter
        layoutManager = new LinearLayoutManager(this); // Linear layout manager
        messageAdapter = new MessageAdapter(new ArrayList<String>(), this); // Messages adapter
        messageList.setLayoutManager(layoutManager); // Setting layout manager
        messageList.setAdapter(messageAdapter); // Setting messages adapter
        // Deciding visibility of menu items
        if(mUserType.equals(MyPreferenceManager.REGULAR_USER)) {
            this.like.setVisible(true); // Making visible
            this.dislike.setVisible(true); // Making visible
        }
    }

    // Load messages
    private void loadMessages() {
        // Listens to value changes etc inside Messages database
        messageRef.addValueEventListener(new ValueEventListener() {
            // On data change
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If there is any data
                if(snapshot.exists() && snapshot.getChildrenCount()>0) {
                    ArrayList<String> messages = extractMessageList(snapshot); // Extracts messages list
                    if(messages.size()>0) { // If there is at least one message
                        messageAdapter.setValues(messages); // Sets adapter's value to the messages list
                        layoutManager.smoothScrollToPosition(messageList, null, messages.size() - 1); // Scrolls down to the last message
                        messageAdapter.notifyDataSetChanged(); // Notifies the adapter of changes
                    }
                } else { // No chats at all
                    Log.d(TAG, "onDataChange: completely no conversations. Not a problem, we are the first users of the app.");
                }
            }
            // On cancellation
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,"Message cancelled!",
                        Toast.LENGTH_LONG).show();
            }
        });
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
    }

    // Create channel
    private void setupChannel() {
        chatterName = getIntent().getStringExtra("chatterName"); // Chatter name
        customerId = getIntent().getStringExtra("customerId"); // Customer ID
        volunteerId = getIntent().getStringExtra("volunteerId"); // Volunteer ID
        customerName = getIntent().getStringExtra("customerName"); // Chatter name
        volunteerName = getIntent().getStringExtra("volunteerName"); // Volunteer name
        Objects.requireNonNull(getSupportActionBar()).setTitle(chatterName); // Setting chatter name
        // This is the first time a volunteer has text to the person who needs help
        chatID = volunteerId + customerId; // Generating chat id by concatenating user id's
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
            bindToChannel(); // Binds chat members to the one channel
        } else {
            // Prompt the user to enter message
            Toast.makeText(this, "Введите сообщение!", Toast.LENGTH_SHORT).show();
        }
    }

    // Connect chatters to the channel
    private void bindToChannel() {
        // Binding customer to the volunteer
        DatabaseReference messagesCustomer = customerDatabase.child(customerId).child(Utility.MESSAGES).child(chatID);
        messagesCustomer.child(Utility.CHATTER_ID).setValue(volunteerId); // Chatter id
        messagesCustomer.child(Utility.CHATTER_NAME).setValue(volunteerName); // Volunteer name
        // Binding volunteer to the customer
        DatabaseReference messageVolunteer = volunteerDatabase.child(volunteerId).child(Utility.MESSAGES).child(chatID);
        messageVolunteer.child(Utility.CHATTER_ID).setValue(customerId); // Customer id
        messageVolunteer.child(Utility.CHATTER_NAME).setValue(customerName); // Customer name
    }

    // Extract array list from the map returned on data change in Firebase
    private ArrayList<String> extractMessageList(DataSnapshot snapshot) {
        ArrayList<String> messagesList = new ArrayList<>(); // List of messages that will be returned
        HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getValue();
        HashMap<String, Object> chatMap = (HashMap<String, Object>) map.get(chatID); // Messages
        if(chatMap!=null) {
            HashMap<String, Object> messages; // Messages map
            Object[] keySet = chatMap.keySet().toArray();
            // Loops through messages HashMap
            for (int i = 0; i < keySet.length; i++) {
                // Getting message map by key
                messages = (HashMap<String, Object>) chatMap.get(keySet[i].toString());
                messagesList.add(messages.get("message").toString()); // Adding into the list
            }
        }
        return messagesList; // Return
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}