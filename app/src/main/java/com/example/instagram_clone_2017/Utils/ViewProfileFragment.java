package com.example.instagram_clone_2017.Utils;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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
import com.example.instagram_clone_2017.Profile.AccountSettingActivity;
import com.example.instagram_clone_2017.Profile.GridImageAdapter;
import com.example.instagram_clone_2017.Profile.ProfileActivity;
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

public class ViewProfileFragment extends Fragment {

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
            mDisplayNames, mUsernames, mWebsite, mDescriptions, mEditProfile
            , mFollow, mUnfollow;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView mGridView;
    private Toolbar mToolbar;
    private ImageView profileMenu;
    private BottomNavigationView mBottomNavigationView;

    private Users mUsers;

    private int mFollowrsCount = 0;
    private int mFollowingCount = 0;
    private int mPostCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);


        initFirebase();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = mAuth.getCurrentUser();
                if (currentUser != null)
                {
                    initView(view);
                    setUpToolbar();

                    try
                    {
                        mUsers = getUserFromBundle();
                        init();
                        
                    }catch (NullPointerException e)
                    {
                        Log.e(TAG, "onAuthStateChanged: NullPointerExceptions " + e.getMessage() );
                        Toast.makeText(getActivity(), "Something Goes Wrong", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }

                    setUpNavigationView(getContext());

                    isFollowing();
                    getFollowerCount();
                    getFollowingCount();
                    getPostCount();

                    mFollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: now Following: " + mUsers.getUsername());
                            firebaseDatabase.getReference()
                                    .child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.following))
                                    .child(currentUser.getUid())
                                    .child(mUsers.getUser_id())
                                    .child(getString(R.string.field_user_id))
                                    .setValue(mUsers.getUser_id());
                            firebaseDatabase.getReference()
                                    .child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.followers))
                                    .child(mUsers.getUser_id())
                                    .child(currentUser.getUid())
                                    .child(getString(R.string.field_user_id))
                                    .setValue(currentUser.getUid());
                            setFollowing();
                        }
                    });

                    mUnfollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: bowUnfollowing: " + mUsers.getUsername());
                            firebaseDatabase.getReference()
                                    .child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.following))
                                    .child(currentUser.getUid())
                                    .child(mUsers.getUser_id())
                                    .child(getString(R.string.field_user_id))
                                    .removeValue();
                            firebaseDatabase.getReference()
                                    .child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.followers))
                                    .child(mUsers.getUser_id())
                                    .child(currentUser.getUid())
                                    .child(getString(R.string.field_user_id))
                                    .removeValue();
                            setUnFollowing();
                        }
                    });




                    gettingDataFromFirebase();
//                    setUpGridView();


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
                .child(mUsers.getUser_id());
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
                .child(mUsers.getUser_id());
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
                .child(mUsers.getUser_id());
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

    private void isFollowing()
    {
        Log.d(TAG, "isFollowing: chechking if following this users.");
        setUnFollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.following))
                .child(currentUser.getUid())
                .orderByChild(mUsers.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found the user "
                            + singleSnapshot.getValue(UsersSettings.class).toString());
                    setFollowing();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setFollowing()
    {
        Log.d(TAG, "setFollowing: updateing UIO for following users");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        mEditProfile.setVisibility(View.GONE);
    }

    private void setUnFollowing()
    {
        Log.d(TAG, "setFollowing: updateing UIO for unfollowing users");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        mEditProfile.setVisibility(View.GONE);
    }

    private void setCurrentUserProfile()
    {
        Log.d(TAG, "setFollowing: updateing UIO for showing this user their own profile");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        mEditProfile.setVisibility(View.VISIBLE);
    }

    private void init()
    {
        // Set Profile Widget
        Toast.makeText(getActivity(), "Anjay", Toast.LENGTH_SHORT).show();
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mUsers.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found the user "
                            + singleSnapshot.getValue(UsersSettings.class).toString());
                    UsersModified modified = new UsersModified();
                    modified.setUsers(mUsers);
                    modified.setUsersSettings(singleSnapshot.getValue(UsersSettings.class));
                    setProfileWidget(modified);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Get the user profile photos

        DatabaseReference reference2 = firebaseDatabase.getReference();
        Query query2 = reference2
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_photos))
                .child(mUsers.getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: success");
                ArrayList<Photo> photos = new ArrayList<Photo>();
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
                        Log.d(TAG, "onItemClick: On Item Click ViewProfileFragment" + "Position = Photo" + i + "Activity_NUm = " + ACTIVITY_NUM);
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

    private void setProfileWidget(UsersModified modified)
    {
        Log.d(TAG, "setProfileWidget: setProfileWidget");
        UsersSettings settings = modified.getUsersSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayNames.setText(settings.getDisplay_name());
        mUsernames.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescriptions.setText(settings.getDescriptions());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowings.setText(String.valueOf(settings.getFollowings()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);

    }

    private Users getUserFromBundle()
    {
        Log.d(TAG, "getUserFromBundle: arguments + " + getArguments());
        Bundle bundle = this.getArguments();

        if (bundle != null)
        {
            return bundle.getParcelable(getString(R.string.intent_user));
        }
        else
        {
            return  null;
        }

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
        mFollow = view.findViewById(R.id.snippet_top_view_profile_text_follow);
        mUnfollow = view.findViewById(R.id.snippet_top_view_profile_text_unfollow);
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

}
