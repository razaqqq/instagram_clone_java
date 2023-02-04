package com.example.instagram_clone_2017;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Like;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.Utils.BottomNavigationViewHelper;
import com.example.instagram_clone_2017.Utils.Heart;
import com.example.instagram_clone_2017.Utils.SquareImageView;
import com.example.instagram_clone_2017.Utils.UniversalImageLoader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment()
    {
        super();
        setArguments(new Bundle());
    }

    public interface OnCommentThreadSelectedListener
    {
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikeByCurrentUser;
    private StringBuilder mUsers;
    private String mLikeString = "";

    private UsersSettings mUsersSetting;



    private Photo mPhoto;
    private SquareImageView postImage;
    private BottomNavigationView bottomNavigationView;
    private int mActivityNumber = 0;
    private TextView mBackLabel, mCaptions, mUsername, mTimeStamp, mLikes, mComments;
    private ImageView mBackArrow, mEllipse, mHeartRed, mHearthWhite, mProfileImage, mComment;
    private Users mCurrentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        initFirebase();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (currentUser != null)
                {
                    init(view);

                    // ToDo Give Some Options to Delete the Photo
                    mEllipse.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getContext(), "Anjing", Toast.LENGTH_SHORT).show();
                        }
                    });


                    mHeart = new Heart(mHearthWhite, mHeartRed);

                    mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
                    setUpNavigationView(getActivity());

                    try {

//                        mPhoto = getPhotoFromBundle();
//                        UniversalImageLoader.setImage(mPhoto.getImage_path(), postImage, null, "");
//                        mActivityNumber = getActivityNumberFromBundle();
//                        getPhotoDetails();
//                        getLikeString();
//                        if (mPhoto.getComments().size() > 0)
//                        {
//                            mComments.setText("View all " + (mPhoto.getComments().size()+1) + " comments");
//                        }
//                        else
//                        {
//                            mComments.setText("No Comment");
//                        }

                        UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), postImage, null, "");
                        mActivityNumber = getActivityNumberFromBundle();
                        String photo_id = getPhotoFromBundle().getPhoto_id();

                        Query query = firebaseDatabase.getReference()
                                .child(getString(R.string.instagram_clone))
                                .child(getString(R.string.photos))
                                .orderByChild(getString(R.string.field_photo_id))
                                .equalTo(photo_id);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                                {
                                    Photo newPhoto = new Photo();
                                    Map<String, Object> objectmap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    newPhoto.setCaptions(objectmap.get(getString(R.string.field_captions)).toString());
                                    newPhoto.setTags(objectmap.get(getString(R.string.field_tags)).toString());
                                    newPhoto.setPhoto_id(objectmap.get(getString(R.string.field_photo_id)).toString());
                                    newPhoto.setUser_id(objectmap.get(getString(R.string.field_user_id)).toString());
                                    newPhoto.setDate_created(objectmap.get(getString(R.string.field_date_created)).toString());
                                    newPhoto.setImage_path(objectmap.get(getString(R.string.field_image_path)).toString());

                                    List<Comment> commentList = new ArrayList<Comment>();

                                    for (DataSnapshot dSnapsot : singleSnapshot.child(getString(R.string.field_comments)).getChildren())
                                    {
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapsot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapsot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapsot.getValue(Comment.class).getDate_created());
                                        commentList.add(comment);
                                    }
                                    newPhoto.setComments(commentList);
                                    mPhoto = newPhoto;

                                    getCurrentUser();
                                    getPhotoDetails();
                                    //getLikeString();

                                    if (mPhoto.getComments().size() > 0)
                                    {
                                        mComments.setText("View all " + (mPhoto.getComments().size()+1) + " comments");
                                    }
                                    else
                                    {
                                        mComments.setText("No Comment");
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    catch (NullPointerException e)
                    {
                        Log.e(TAG, "onCreateView: NullPointerExceptions, photo was null from bundle" + e );
                    }

                    //setUpWidgetMethods();




                }
                else
                {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }

        };





        return view;
    }

    private void init()
    {

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (isAdded())
//        {
//            init();
//        }
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        }catch(ClassCastException e)
        {
            Log.e(TAG, "onAttach: ClassCastExceptions" + e.getMessage());

        }
    }

    private void getLikeString()
    {
        Log.d(TAG, "getLikeString: getting like string");

        DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {

                    DatabaseReference reference = firebaseDatabase.getReference();
                    Query query = reference
                            .child(getString(R.string.instagram_clone))
                            .child(getString(R.string.users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot singleSnapshot : snapshot.getChildren())
                            {
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(Users.class).getUsername());
                                mUsers.append(singleSnapshot.getValue(Users.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUser = mUsers.toString().split(",");

                            if (mUsers.toString().contains(mCurrentUser.getUsername() + ","))
                            {
                                mLikeByCurrentUser = true;
                            }
                            else
                            {
                                mLikeByCurrentUser = false;
                            }

                            int length = splitUser.length;

                            if (length == 1)
                            {
                                mLikeString = "liked by " + splitUser[0];
                            }
                            else if (length == 2)
                            {
                                mLikeString = "liked by " + splitUser[0]
                                + " and " + splitUser[1];
                            }
                            else if (length == 3)
                            {
                                mLikeString = "liked by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + " and " + splitUser[2]
                                ;
                            }
                            else if (length == 4)
                            {
                                mLikeString = "liked by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + ", " + splitUser[2]
                                        + " and " + splitUser[3]
                                ;
                            }
                            else if (length > 4)
                            {
                                mLikeString = "likes by " + splitUser[0]
                                        + ", " + splitUser[1]
                                        + ", " + splitUser[2]
                                        + " and " + (splitUser.length - 3) + " others"
                                ;
                            }
                            setUpWidgetMethods();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                if (!snapshot.exists())
                {
                    mLikeString = "";
                    mLikeByCurrentUser = false;
                    setUpWidgetMethods();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getCurrentUser()
    {
        DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(currentUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: success");
                for (DataSnapshot singleSnapsot : snapshot.getChildren())
                {
                    mCurrentUser = singleSnapsot.getValue(Users.class);
                }
                getLikeString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = firebaseDatabase.getReference();
            Query query = reference
                    .child(getString(R.string.instagram_clone))
                    .child(getString(R.string.photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    {

                        String keyId = singleSnapshot.getKey();
                        // case 1 : when user already like the photo
                        if (mLikeByCurrentUser &&
                                singleSnapshot.getValue(Like.class)
                                        .getUser_id().equals(currentUser.getUid()))
                        {
                            reference.child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();
                            reference.child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.users_photos))
                                    .child(currentUser.getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikeString();

                        }

                        // case 2 : when user has not like the photo
                        else if (!mLikeByCurrentUser)
                        {
                            // add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!snapshot.exists())
                    {
                        // add new like
                        addNewLike();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return true;
        }
    }

    private void addNewLike()
    {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeId = firebaseDatabase.getReference().push().getKey();
        Like like = new Like();
        like.setUser_id(currentUser.getUid());

        firebaseDatabase.getReference().child(getString(R.string.instagram_clone))
                .child(getString(R.string.photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);
        firebaseDatabase.getReference().child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        mHeart.toggleLike();
        getLikeString();

    }
    private void setUpWidgetMethods()
    {
        Log.d(TAG, "setUpWidgetMethods: ");
        mCaptions.setText(mPhoto.getCaptions().toString());
        mUsername.setText(mUsersSetting.getUsername());
        String timeStampDiff = getTimeStampDiffereance();

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });




//        if (mPhoto.getComments() == null)
//        {
//            String commentId = firebaseDatabase.getReference().push().getKey();
//            Query query = firebaseDatabase.getReference()
//                            .child(getString(R.string.instagram_clone))
//                            .child(getString(R.string.photos))
//                            .child(mPhoto.getPhoto_id());
//
//
//
//
//            query.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    List<Comment> commentList = new ArrayList<Comment>();
//
//                    for (DataSnapshot dSnapshot : snapshot.child(getString(R.string.field_comments)).getChildren())
//                    {
//                        Comment comment = new Comment();
//                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//                        commentList.add(comment);
//                    }
//
//                    mPhoto.setComments(commentList);
//
//
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//
////            Toast.makeText(getActivity(), mPhoto.getComments().size(), Toast.LENGTH_SHORT).show();
//
//
//
////            if (mPhoto.getComments().size() > 0)
////            {
////                Toast.makeText(getActivity(), "asu", Toast.LENGTH_SHORT).show();
////            }
////            else
////            {
////                Toast.makeText(getActivity(), "Singo", Toast.LENGTH_SHORT).show();
////            }
//
//
//
////            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
//        }
//        else
//        {
//            if (mPhoto.getComments().size() > 0)
//            {
//                Toast.makeText(getActivity(), "asu", Toast.LENGTH_SHORT).show();
//            }
//            else
//            {
//                Toast.makeText(getActivity(), "Singo", Toast.LENGTH_SHORT).show();
//            }
//        }

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to commentsthread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);

            }
        });
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });
        if (!timeStampDiff.equals("0"))
        {
            mTimeStamp.setText(timeStampDiff + "DAYS AGO");
        }
        else
        {
            mTimeStamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(mUsersSetting.getProfile_photo(), mProfileImage, null, "");
        mLikes.setText(mLikeString);

        if (mLikeByCurrentUser)
        {
            mHearthWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d(TAG, "onTouch: red heart touch detected");
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }
        else
        {
            mHearthWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHearthWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d(TAG, "onTouch: white heart touch detected");
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }





    }

    private String getTimeStampDiffereance() {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch ( ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

    private void getPhotoDetails()
    {
        Log.d(TAG, "getPhotoDetails: ");
        DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_settings))
                .orderByChild("user_id")
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: success");
                for (DataSnapshot singleSnapsot : snapshot.getChildren())
                {
                    mUsersSetting = singleSnapsot.getValue(UsersSettings.class);
                }
                //setUpWidgetMethods();





            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void initFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

    }

    private void init(View view) {
        postImage = view.findViewById(R.id.layout_view_post_image);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = view.findViewById(R.id.snippet_post_toolbar_back_arrow);
        //mBackLabel = view.findViewById(R.id.layout_view_post_bac);
        mCaptions = view.findViewById(R.id.layout_view_post_image_captions);
        mUsername = view.findViewById(R.id.layout_view_post_username);
        mTimeStamp = view.findViewById(R.id.layout_view_post_image_time_postedk);
        mEllipse = view.findViewById(R.id.layout_view_post_ic_ellipse);
        mHeartRed = view.findViewById(R.id.layout_view_post_iamge_hearth_red);
        mHearthWhite = view.findViewById(R.id.layout_view_post_hearth_image_heart_white);
        mProfileImage = view.findViewById(R.id.layout_view_post_profile_photo);
        mUsername = view.findViewById(R.id.layout_view_post_username);
        mLikes = view.findViewById(R.id.layout_view_post_image_like);
        mComment = view.findViewById(R.id.layout_view_post_speech_buble);
        mComments = view.findViewById(R.id.layout_view_post_image_comments_link);



    }

    private Photo getPhotoFromBundle()
    {
        Log.d(TAG, "getPhotoFromBundle: arguments " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getParcelable(getString(R.string.photo));
        }
        else
        {
            return null;
        }

    }

    private int getActivityNumberFromBundle()
    {
        Log.d(TAG, "getActivityNumberFromBundle: arguments " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getInt(getString(R.string.activity_number));
        }
        else
        {
            return 0;
        }

    }



    private void setUpNavigationView(Context context)
    {
        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

}
