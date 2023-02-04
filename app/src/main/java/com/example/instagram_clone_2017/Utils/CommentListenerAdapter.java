package com.example.instagram_clone_2017.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListenerAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListenerAdapter(@NonNull Context context,
                                  int resource,
                                  @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView comment, timeStamp, username, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final  ViewHolder holder;

        if (convertView == null)
        {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.layout_comment_comments);
            holder.username = convertView.findViewById(R.id.layout_comment_username);
            holder.timeStamp = convertView.findViewById(R.id.layout_comment_time_posted);
            holder.reply = convertView.findViewById(R.id.layout_comment_reply);
            holder.like = convertView.findViewById(R.id.layout_comment_like);
            holder.likes = convertView.findViewById(R.id.layout_comment_number_of_like);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        // set the comment
        holder.comment.setText(getItem(position).getComment());
        // set the timestamp
        String timeStampDiffereance = getTimeStampDiffereance(getItem(position));
        if (!timeStampDiffereance.equals("0"))
        {
            holder.timeStamp.setText(timeStampDiffereance + " d");
        }
        else
        {
            holder.timeStamp.setText("Today");
        }
        // set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: success");
                for (DataSnapshot singleSnapsot : snapshot.getChildren())
                {
                    holder.username.setText(singleSnapsot.getValue(UsersSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(
                            singleSnapsot.getValue(UsersSettings.class).getProfile_photo(),
                            holder.profileImage
                    );

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (position == 0)
        {
            holder.like.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);

        }

        return convertView;
    }

    private String getTimeStampDiffereance(Comment comment) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch ( ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
}
