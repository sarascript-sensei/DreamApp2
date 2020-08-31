package com.unicef.dreamapp2.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
* @author Iman Augustine
 *
 * Chat model.
 *
 * */

public class ChannelModel {
    // Global variables
    public String chatterName = ""; // Chat id
    public String lastMessage; // Message id

    @Exclude
    public String localMediaUrl = "";

    @Exclude
    public int id = 0;

    @Exclude
    public long blockTime = 0;

    public ChannelModel(){
    }

    public String getChatterName() {
        return chatterName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setChatterName(String chatterName) {
        this.chatterName = chatterName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
