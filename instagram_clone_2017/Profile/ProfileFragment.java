package com.example.instagram_clone_2017.Profile;

import android.content.Context;
import android.content.Intent;
// import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Like;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersModified;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.BottomNavigationViewHelper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

// import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_FRAGMENT";

    public interface OnGridImageSelectedListener{
        void OnGridImageSelectedListener(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener onGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView mPosts, mFollowers, mFollowings,
            mDisplayNames, mUsernames, mWebsite, mDescriptions, mEditProfile;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView mGridView;
    private Toolbar mToolbar;
    private ImageView profileMenu;
    private BottomNavigationView mBottomNavigationView;

    private int mFollowrsCount = 0;
    private int mFollowingCount = 0;
    private int mPostCount = 0;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initFirebase();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = mAuth.getCurrentUser();
                if (currentUser != null)
                {

                    initView(view);
                    setUpToolbar();
                    setUpNavigationView(getContext());
                    gettingDataFromFirebase();
                    setUpGridView();

                    getFollowerCount();
                    getFollowingCount();
                    getPostCount();

                    //ArrayList<Photo> tempPhoto = new ArrayList<Photo>();
                    mEditProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), AccountSettingActivity.class);
                            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                            startActivity(intent);
                        }
                    });
                }
                else
                {
//                    Intent intent = new Intent(getContext(), LoginActivity.class);
//                    startActivity(intent);
                }
            }
        };
        return view;
    }

    private void getFollowerCount()
    {
        mFollowrsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.followers))
                .child(currentUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found followerr "
                            + singleSnapshot.getValue());
                    mFollowrsCount++;
                }
                mFollowers.setText(String.valueOf(mFollowrsCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowingCount()
    {
        mFollowingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.following))
                .child(currentUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found followerr "
                            + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowings.setText(String.valueOf(mFollowingCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostCount()
    {
        mPostCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_photos))
                .child(currentUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found followerr "
                            + singleSnapshot.getValue());
                    mPostCount++;
                }
                mPosts.setText(String.valueOf(mPostCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onAttach(@NonNull Context context) {
        try{
            Log.d(TAG, "onAttach: TryOnAttch");
            onGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e)
        {
            Log.e(TAG, "onAttach: ClassExceptions: " + e.getMessage());
        }
        super.onAttach(context);
    }

    private void setUpGridView()
    {
        Log.d(TAG, "setUpGridView: ");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_photos))
                .child(currentUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: success");
                for (DataSnapshot singleSnapsot : snapshot.getChildren())
                {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapsot.getValue();
                    photo.setCaptions(objectMap.get(getString(R.string.field_captions)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                    List<Comment> comments = new ArrayList<Comment>();
                    for (DataSnapshot snapshotComment : singleSnapsot.child(getString(R.string.field_comments)).getChildren())
                    {
                        Comment comment = new Comment();
                        comment.setComment(snapshotComment.getValue(Comment.class).getComment());
                        comment.setDate_created(snapshotComment.getValue(Comment.class).getDate_created());
                        comment.setUser_id(snapshotComment.getValue(Comment.class).getUser_id());
                        comments.add(comment);
                    }
                    photo.setComments(comments);
                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapsot.child(getString(R.string.field_likes)).getChildren())
                    {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);
                }
                // Set Up Image Grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                int imageHeight = gridWidth * 2;
                mGridView.setColumnWidth(imageWidth);
                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++)
                {
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(getActivity(),
                        R.layout.layout_grid_image_view
                        , ""
                        , imgUrls
                );
                mGridView.setAdapter(adapter);
                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        onGridImageSelectedListener.OnGridImageSelectedListener(photos.get(i), ACTIVITY_NUM);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: cancelled error = " + error.toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void initFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void gettingDataFromFirebase()
    {
        userId = currentUser.getUid();
        firebaseDatabase.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // retrieve user information from database
                UsersModified usersModified = getUserSettings(snapshot, userId);

                String posts = String.valueOf(usersModified.getUsersSettings().getPosts());
                String followers = String.valueOf(usersModified.getUsersSettings().getFollowers());
                String followings = String.valueOf(usersModified.getUsersSettings().getFollowings());
                mUsernames.setText(usersModified.getUsersSettings().getUsername());
                mWebsite.setText(usersModified.getUsersSettings().getWebsite());
                mDescriptions.setText(usersModified.getUsersSettings().getDescriptions());
                mDisplayNames.setText(usersModified.getUsersSettings().getDisplay_name());
                UniversalImageLoader.setImage(
                        usersModified.getUsersSettings().getProfile_photo(),
                        mProfilePhoto,
                        mProgressBar,
                        "");
                // retrieve image for the user in questions
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private UsersModified getUserSettings(DataSnapshot dataSnapshot, String userId)
    {
        UsersSettings usersSettings = new UsersSettings();
        Users users = new Users();
        for (DataSnapshot data: dataSnapshot.child("instagram_clone").getChildren())
        {
            // handle users_settings node
            if (data.getKey().equals("users_settings"))
            {
                usersSettings.setDisplay_name(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getDisplay_name()
                );
                usersSettings.setUsername(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getUsername()
                );
                usersSettings.setWebsite(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getWebsite()
                );
                usersSettings.setDescriptions(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getDescriptions()
                );
                usersSettings.setProfile_photo(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getProfile_photo()
                );
                usersSettings.setPosts(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getPosts()
                );
                usersSettings.setFollowings(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getFollowings()
                );
                usersSettings.setFollowers(
                        data.child(userId)
                                .getValue(UsersSettings.class)
                                .getFollowers()
                );
            }
            // handle users node
            if (data.getKey().equals("users"))
            {
                users.setUsername(
                        data.child(userId)
                                .getValue(Users.class)
                                .getUsername()
                );
                users.setEmail(
                        data.child(userId)
                                .getValue(Users.class)
                                .getEmail()
                );
                users.setPhone_number(
                        data.child(userId)
                                .getValue(Users.class)
                                .getPhone_number()
                );
                users.setUser_id(
                        data.child(userId)
                                .getValue(Users.class)
                                .getUser_id()
                );
            }
        }
        return new UsersModified(usersSettings, users);
    }

    private void initView(View view) {
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowings = view.findViewById(R.id.tvFollowing);
        mDisplayNames = view.findViewById(R.id.displayName);
        mUsernames = view.findViewById(R.id.profileName);
        mWebsite = view.findViewById(R.id.website);
        mDescriptions = view.findViewById(R.id.description);
        mEditProfile = view.findViewById(R.id.snippet_top_profile_edit_rofile);
        mProgressBar = view.findViewById(R.id.profileProgessbar);
        mProfilePhoto = view.findViewById(R.id.profileImage);
        mGridView = view.findViewById(R.id.gridView);
        mToolbar = view.findViewById(R.id.snippet_top_profile_bar_toolbar);
        profileMenu = view.findViewById(R.id.snippet_top_profile_bar_menu);
        mBottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
    }

    private void setUpNavigationView(Context context)
    {
        BottomNavigationViewHelper.enableNavigation(context, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void setUpToolbar()
    {

        ((ProfileActivity)getActivity()).setSupportActionBar(mToolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AccountSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity(intent);
            }
        });
    }

    private void setUpImageGrid(ArrayList<String> imgUrls)
    {
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        int imageHeight = imageWidth * 2;
//        mGridView.setColumnWidth(imageWidth);
        mGridView.setLayoutParams(new ViewGroup.LayoutParams(imageWidth, imageHeight));
        GridImageAdapter adapter = new GridImageAdapter(getContext(), R.layout.layout_grid_image_view, "", imgUrls);
        mGridView.setAdapter(adapter);
    }

}
