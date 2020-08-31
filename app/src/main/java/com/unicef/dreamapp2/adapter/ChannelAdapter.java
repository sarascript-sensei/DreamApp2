package com.unicef.dreamapp2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unicef.dreamapp2.R;

import java.util.List;

/**
 * @author Iman Augustine
 *
 * MessageAdapter is used as messages adapter for the RecyclerView
 * used to display the chat messages between two users.
 *
 * */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MessageViewHolder> {

    private List<String> messageList;
    private Context context;

    public ChannelAdapter(List<String> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    public void setValues(List<String> messageList) {
         this.messageList.clear();
         if(messageList!=null) {
             this.messageList.addAll(messageList);
         }
         this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channel_item, parent, false);

        return new MessageViewHolder(itemView);
    }

    // On bind view holder
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
      //  holder.name.setText(("Иман Уулу:\n"+messageList.get(position)));
      //  holder.lastMessage.setText(("Вы:\n"+messageList.get(position)));
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        // Global variables
        private TextView name;
        private TextView lastMessage;

        // Message view holder
        private MessageViewHolder(View view) {
            super(view);
            // Views initialize
            name = view.findViewById(R.id.name);
            lastMessage = view.findViewById(R.id.lastMessage);

        }
    }
}
