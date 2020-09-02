package com.unicef.dreamapp2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unicef.dreamapp2.BaseInterface;
import com.unicef.dreamapp2.R;
import com.unicef.dreamapp2.model.ChannelModel;
import com.unicef.dreamapp2.singleclicklistener.OnSingleClickListener;

import java.util.List;

/**
 * @author Iman Augustine
 *
 * MessageAdapter is used as messages adapter for the RecyclerView
 * used to display the chat messages between two users.
 *
 * */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MessageViewHolder> {

    // Global variables
    private List<ChannelModel> channelsList;
    private BaseInterface.OnItemClickListener onItemClickListener;
    private Context context;

    // Adapter constructor
    public ChannelAdapter(List<ChannelModel> channelsList, Context context, BaseInterface.OnItemClickListener onItemClickListener) {
        this.channelsList = channelsList; // Channels list
        this.context = context; // Context
        this.onItemClickListener = onItemClickListener;
    }

    // Sets values
    public void setValues(List<ChannelModel > messageList) {
         this.channelsList.clear();
         if(messageList!=null) { // If the list is not empty
             this.channelsList.addAll(messageList); // Adding all message lists
         }
         this.notifyDataSetChanged(); // Notify data set change
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channel_item, parent, false);

        return new MessageViewHolder(itemView, onItemClickListener);
    }

    // On bind view holder
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.name.setText((channelsList.get(position).getChatterName()));
        // holder.lastMessage.setText(("Вы:\n"+messageList.get(position)));
    }

    @Override
    public int getItemCount() {
        return channelsList != null ? channelsList.size() : 0;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        // Global variables
        private TextView name;
        private TextView lastMessage;

        // Message view holder
        private MessageViewHolder(View view, final BaseInterface.OnItemClickListener onItemClickListener) {
            super(view);
            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    onItemClickListener.onItemClick(channelsList.get(getAdapterPosition()));
                }
            });
            // Views initialize
            name = view.findViewById(R.id.chatterName);
            lastMessage = view.findViewById(R.id.lastMessage);
        }
    }
}
