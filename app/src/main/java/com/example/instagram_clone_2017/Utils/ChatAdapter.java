package com.example.instagram_clone_2017.Utils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone_2017.Model.MessageModel;
import com.example.instagram_clone_2017.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter {

    private static final String TAG = "CHAT_ADAPTER";

    ArrayList<MessageModel> messageModels;
    Context mContext;
    String recId;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    int TIME_DIFFEREANCE_VIEW_TYPE = 3;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context mContext) {
        this.messageModels = messageModels;
        this.mContext = mContext;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context mContext, String recId) {
        this.messageModels = messageModels;
        this.mContext = mContext;
        this.recId = recId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE)
        {
            View view = LayoutInflater.from(this.mContext).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(this.mContext).inflate(R.layout.sample_receiver, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            return SENDER_VIEW_TYPE;
        }
        else
        {
            return RECEIVER_VIEW_TYPE;
        }
//        // TODO : What Should I Do ??????????????????
//        else if (!messageModels.get(position).getTimeStamp().equals(0))
//        {
//
//            return TIME_DIFFEREANCE_VIEW_TYPE;
//        }
//        else
//        {
//
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModels.get(position);



        if (messageModel.getUserId().toString().equals(FirebaseAuth.getInstance().getUid()))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Delete Message")
                            .setMessage("Are You Sure Want To Delete This Message")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                                  database.getReference().child(mContext.getString(R.string.instagram_clone))
                                            .child(mContext.getString(R.string.chat_message))
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid() + recId)
                                            .child(messageModel.getMessageId())
                                            .setValue(null);

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                    return true;
                }
            });
        }
        else
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(mContext)
                            .setMessage("Are You Sure Want To Delete This Message")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(mContext, "You Cannot Delete This Message", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                    return true;
                }
            });
        }
        
        if (holder.getClass() == SenderViewHolder.class)
        {

            ((SenderViewHolder)holder).senderMessage.setText(messageModel.getMessage());

            Date date = new Date(messageModel.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate =simpleDateFormat.format(date);
            ((SenderViewHolder)holder).senderTime.setText(strDate);
        }
        else
        {
            ((RecieverViewHolder)holder).receiverMessage.setText(messageModel.getMessage());
            Date date = new Date(messageModel.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate =simpleDateFormat.format(date);
            ((RecieverViewHolder)holder).receiveTime.setText(strDate);
        }



    }

    private String getTimeStampDiffereance(MessageModel messageModel) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = String.valueOf(messageModel.getTimeStamp());
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch ( ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder
    {
        TextView receiverMessage, receiveTime;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessage = itemView.findViewById(R.id.sample_receiver_receiver_text);
            receiveTime = itemView.findViewById(R.id.sample_receiver_time);

        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView senderMessage, senderTime;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.sample_sender_sender_text);
            senderTime = itemView.findViewById(R.id.sample_sender_time);

        }
    }

    public class TimeDiffereanceViewHolder extends RecyclerView.ViewHolder
    {
        TextView timeDiffereance;


        public TimeDiffereanceViewHolder(@NonNull View itemView) {
            super(itemView);
            timeDiffereance = itemView.findViewById(R.id.sample_time_differeance_tv);
        }
    }

}
