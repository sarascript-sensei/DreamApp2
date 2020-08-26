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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<String> messageList;
    private Context context;

    public MessageAdapter(List<String> messageList, Context context) {
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
                .inflate(R.layout.message_item, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.messageText1.setText(("Иман Уулу:\n"+messageList.get(position)));
        holder.messageText2.setText(("Вы:\n"+messageList.get(position)));
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        //
        private TextView messageText1;
        private TextView messageText2;

        private MessageViewHolder(View view) {
            super(view);
            // Views initialize
            messageText1 = view.findViewById(R.id.messageText1);
            messageText1.setVisibility(View.GONE);
            messageText2 = view.findViewById(R.id.messageText2);

        }
    }
}
