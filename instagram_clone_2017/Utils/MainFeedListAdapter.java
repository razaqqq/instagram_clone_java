package com.example.instagram_clone_2017.Utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instagram_clone_2017.Home.HomeFragment;
import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Like;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.Profile.ProfileActivity;
import com.example.instagram_clone_2017.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainFeedListAdapter extends ArrayAdapter<Photo> {

    private static final String TAG = "mainFeedListAdapter";

    public interface OnLoadMoreItemsListeners
    {
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListeners mOnLoadMoreItemsListeners;

    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference reference;
    private String currentUsername = "";

    public MainFeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        reference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder
    {
        CircleImageView mprofileImage;
        String likesString;
        TextView username, timeDetla, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UsersSettings settings = new UsersSettings();
        Users users = new Users();
        StringBuilder userStringBuilder;
        String mLikeStrings;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
        ImageView more;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = mLayoutInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.more = (ImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_ic_more_vert);
            holder.username = (TextView) convertView.findViewById(R.id.layout_mainfeed_list_item_username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_hearth_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_heart_white);
            holder.comment = (ImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_speech_buble);
            holder.likes = (TextView) convertView.findViewById(R.id.layout_mainfeed_list_item_image_like);
            holder.comments = (TextView) convertView.findViewById(R.id.layout_mainfeed_list_item_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.layout_mainfeed_list_item_image_captions);
            holder.timeDetla = (TextView) convertView.findViewById(R.id.layout_mainfeed_list_item_time_postedk);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.layout_mainfeed_list_item_profile_image);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.userStringBuilder = new StringBuilder();

            convertView.setTag(holder);

        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        //get current username before checking like string
        getCurrentUsername();

        //set the captions
        holder.caption.setText(getItem(position).getCaptions());

        //get like string
        getLikeString(mContext, holder);

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_more, popupMenu.getMenu());
                popupMenu.setForceShowIcon(true);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.ic_house:
                                Toast.makeText(mContext, "Anjing", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.ic_search:
                                Toast.makeText(mContext, "WKWKKW", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.ic_circle:
                                Toast.makeText(mContext, "ASUUUU", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        //set the comment
        List<Comment> commentList = getItem(position).getComments();
        holder.comments.setText("View all " + commentList.size() + " comments");
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: loading commant thread " + getItem(position).getPhoto_id());
                ((MainActivity)mContext).onCommentThreadSelected(getItem(position),
                        mContext.getString(R.string.main_activity));

                // Going need do some thinh else what it is ?
                ((MainActivity)mContext).hideLayout();

            }
        });

        //set the time it was posted
        String timeStampDiference = getTimeStampDiffereance(getItem(position));
        if (!timeStampDiference.equals("0"))
        {
            holder.timeDetla.setText(timeStampDiference + " DAYS AGO");
        }
        else
        {
            holder.timeDetla.setText("TODAY");
        }

        //set profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        //get the profile image and username
        Query query = reference
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user = "
                            + singleSnapshot.getValue(UsersSettings.class).getUsername());
                    //currentUsername = singleSnapshot.getValue(UsersSettings.class).getUsername();
                    holder.username.setText(singleSnapshot.getValue(UsersSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: navigating to profile of : " +
                                    holder.users.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity)
                            );
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.users);
                            mContext.startActivity(intent);
                        }
                    });
                    imageLoader.displayImage(singleSnapshot.getValue(UsersSettings.class).getProfile_photo(),
                            holder.mprofileImage
                            );
                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: navigating to profile of : " +
                                    holder.users.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity)
                            );
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.users);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings = singleSnapshot.getValue(UsersSettings.class);
                    holder.comments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: comment clicked");
                            ((MainActivity)mContext).onCommentThreadSelected(getItem(position),
                                    mContext.getString(R.string.main_activity));
                            //another thing
                            ((MainActivity)mContext).hideLayout();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //get the user object
        Query userQuery = reference
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user ");
                    singleSnapshot.getValue(Users.class).getUsername();

                    holder.users = singleSnapshot.getValue(Users.class);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (reachedEndOfTheList(position))
        {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfTheList(int position)
    {
        return position == getCount() - 1;
    }

    private void loadMoreData()
    {
        try {
             mOnLoadMoreItemsListeners = (OnLoadMoreItemsListeners) getContext();
        }catch (ClassCastException e)
        {
            Log.e(TAG, "loadMoreData: ClassCastException " + e.getMessage() );
        }

        try {
            mOnLoadMoreItemsListeners.onLoadMoreItems();
        }catch (NullPointerException e)
        {
            Log.e(TAG, "NullPointer EXceptions " + e.getMessage() );
        }

    }



    private void addNewLike(Context mContext, ViewHolder holder)
    {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeId = reference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);
        reference.child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        holder.heart.toggleLike();
        getLikeString(mContext, holder);

    }

    private void getLikeString(Context mContext, ViewHolder holder)
    {
        Log.d(TAG, "getLikeString: getting like string");

        try
        {
        Query query = reference
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.userStringBuilder = new StringBuilder();
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {

                    Query query = reference
                            .child(mContext.getString(R.string.instagram_clone))
                            .child(mContext.getString(R.string.users))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot singleSnapshot : snapshot.getChildren())
                            {
                                Log.d(TAG, "onDataChange: found like: asolole" +
                                        singleSnapshot.getValue(Users.class).getUsername());
                                Log.d(TAG, "onDataChange: username UsersClass = " + singleSnapshot.getValue(Users.class).getUsername());
                                holder.userStringBuilder.append(singleSnapshot.getValue(Users.class).getUsername());
                                holder.userStringBuilder.append(",");
                            }

                            String[] splitUser = holder.userStringBuilder.toString().split(",");


                            Log.d(TAG, "onDataChange: username Holder.Users = " + holder.users.getUsername());


//                            I Found the error it is because userStringBuilder doesnt countain holder.user.getUsername
//                                    userStringBuilder = razaq, holder.getUsername = razaqqq the username is not the same
//                                    its make the if coundition always retur false
                            if (holder.userStringBuilder.toString().contains(currentUsername + ","))
                            {
                                Log.d(TAG, "onDataChange: stringbuilder countains users.getusername");
                                holder.likeByCurrentUser = true;
                            }
                            else
                            {
                                Log.d(TAG, "onDataChange: string builder not countains users.getusername");
                                holder.likeByCurrentUser = false;
                            }

                            int length = splitUser.length;

                            if (length == 1)
                            {
                                holder.likesString = "liked by " + splitUser[0];
                            }
                            else if (length == 2)
                            {
                                holder.likesString = "liked by " + splitUser[0]
                                        + " and " + splitUser[1];
                            }
                            else if (length == 3)
                            {
                                holder.likesString = "liked by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + " and " + splitUser[2]
                                ;
                            }
                            else if (length == 4)
                            {
                                holder.likesString = "liked by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + ", " + splitUser[2]
                                        + " and " + splitUser[3]
                                ;
                            }
                            else if (length > 4)
                            {
                                holder.likesString = "likes by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + ", " + splitUser[2]
                                        + " and " + (splitUser.length - 3) + " others"
                                ;
                            }
                            //set up like string
                            setUpLikeString(holder, holder.likesString);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                if (!snapshot.exists())
                {
                    holder.likesString = "";
                    holder.likeByCurrentUser = false;
                    //setup like string
                    setUpLikeString(holder, holder.likesString);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }catch (NullPointerException e)
        {
            Log.e(TAG, "getLikeString: NullPointerException " + e.getMessage() );
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup like string
            setUpLikeString(holder, holder.likesString);
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: gestureListener asuuu");
            Query query = reference
                    .child(mContext.getString(R.string.instagram_clone))
                    .child(mContext.getString(R.string.photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    {

                        Log.d(TAG, "onDataChange: like class userId " + singleSnapshot.getValue(Like.class)
                                .getUser_id());
                        Log.d(TAG, "onDataChange: currentUser id " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                        String keyId = singleSnapshot.getKey();
                        // case 1 : when user already like the photo
                        if (mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Like.class)
                                        .getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            Log.d(TAG, "onDataChange: if true");
                            reference.child(mContext.getString(R.string.instagram_clone))
                                    .child(mContext.getString(R.string.photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();
                            reference.child(mContext.getString(R.string.instagram_clone))
                                    .child(mContext.getString(R.string.users_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikeString(mContext, mHolder);

                        }

                        // case 2 : when user has not like the photo
                        else if (!mHolder.likeByCurrentUser)
                        {
                            // add new like
                            addNewLike(mContext, mHolder);
                            break;
                        }
                        
                        else
                        {
                            Log.d(TAG, "onDataChange: if else");
                        }
                        
                    }
                    if (!snapshot.exists())
                    {
                        // add new like
                        addNewLike(mContext, mHolder);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return true;
        }
    }

    private void setUpLikeString(ViewHolder holder, String likeString)
    {
        Log.d(TAG, "setUpLikeString: like string:" + holder.likesString);
        if (holder.likeByCurrentUser)
        {
            Log.d(TAG, "setUpLikeString: photo has been liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }
        else
        {
            Log.d(TAG, "setUpLikeString: photo has been not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }
        holder.likes.setText(likeString);
    }

    

    private String getTimeStampDiffereance(Photo photo) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch ( ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
    private void getCurrentUsername()
    {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");
        Query query = reference
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    currentUsername = singleSnapshot.getValue(UsersSettings.class).getUsername();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
