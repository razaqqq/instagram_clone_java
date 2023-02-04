package com.example.instagram_clone_2017.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.ViewCommentFragment;
import com.example.instagram_clone_2017.Utils.ViewProfileFragment;
import com.example.instagram_clone_2017.ViewPostFragment;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener
        , ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener

{
    private static final String TAG = "ProvileActivityActivity";

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected a comment thread");
        ViewCommentFragment fragment = new ViewCommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_profile_container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    @Override
    public void OnGridImageSelectedListener(Photo photo, int activityNumber) {
        Log.d(TAG, "OnGridImageSelectedListener: selected image" + photo.toString() + " ,ActivityNum = " + activityNumber);
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_profile_container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private ProgressBar progressBar;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
//        setUpActivityWidget();
//        setUpNavigationView(ProfileActivity.this);
//        setUpToolbar();
//        setUpProfileImage();
//        tempGridSetUp();
    }

    private void init()
    {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "init: searching for user_object atached as intentextra");
            if (intent.hasExtra(getString(R.string.intent_user)))
            {
                Users users = intent.getParcelableExtra(getString(R.string.intent_user));
                if (!users.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    Log.d(TAG, "init: inflating View Profile");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.activity_profile_container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }
                else
                {
                    Log.d(TAG, "init: inflating Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.activity_profile_container, profileFragment);
                    transaction.addToBackStack("ProfileFragment");
                    transaction.commit();
                }

            }
            else
            {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.d(TAG, "init: inflating Profile");
            ProfileFragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activity_profile_container, profileFragment);
            transaction.addToBackStack("ProfileFragment");
            transaction.commit();
        }
    }

//
//    private void setUpProfileImage()
//    {
//        UniversalImageLoader.setImage("https://techdaily.id/wp-content/uploads/2021/07/Android-main.jpg",
//                imageView,
//                progressBar,
//                ""
//                );
//    }
//
//    private void setUpActivityWidget()
//    {
//        progressBar = findViewById(R.id.profileProgessbar);
//        progressBar.setVisibility(View.GONE);
//        imageView = findViewById(R.id.profileImage);
//    }
//
//    private void setUpNavigationView(Context context)
//    {
//        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavViewBar);
//        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationView);
//        Menu menu = bottomNavigationView.getMenu();
//        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//        menuItem.setChecked(true);
//    }
//
//    private void tempGridSetUp()
//    {
//        ArrayList<String> imgUrls = new ArrayList<>();
//        imgUrls.add("https://nudes18.fun/wp-content/uploads/sites/11/2022/02/56-48.jpg");
//        imgUrls.add("https://nudes18.fun/wp-content/uploads/sites/11/2022/02/10/73-47.jpg");
//        imgUrls.add("https://nudes18.fun/wp-content/uploads/sites/11/2022/02/10/9-42.jpg");
//        imgUrls.add("https://nudes18.fun/wp-content/uploads/sites/11/2022/02/10/39-42.jpg");
//        imgUrls.add("http://www.xnightflight.com/pn/image/320509.jpg");
//        imgUrls.add("http://www.xnightflight.com/pn/image/16ce4ad19ceb9651a0440e3050bfd31d.jpg");
//        imgUrls.add("http://www.xnightflight.com/pn/image/8901ef376a77abc1c89dbe7a682b19c7.jpg");
//
//        setUpImageGrid(imgUrls);
//
//    }
//
//    private void setUpImageGrid(ArrayList<String> imgUrls)
//    {
//        GridView gridView = findViewById(R.id.gridView);
//
//        int gridWidth = getResources().getDisplayMetrics().widthPixels;
//        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
//        gridView.setColumnWidth(imageWidth);
//
//        GridImageAdapter adapter = new GridImageAdapter(ProfileActivity.this, R.layout.layout_grid_image_view, "", imgUrls);
//        gridView.setAdapter(adapter);
//    }
//
//    private void setUpToolbar()
//    {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
//        setSupportActionBar(toolbar);
//
//        ImageView profileMenu = (ImageView) findViewById(R.id.snippet_top_profile_bar_menu);
//        profileMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ProfileActivity.this, AccountSettingActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP );
//                startActivity(intent);
//            }
//        });
//
//    }
}
