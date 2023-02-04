package com.example.instagram_clone_2017.Profile;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersModified;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Share.NextActivity;
import com.example.instagram_clone_2017.Utils.BottomNavigationViewHelper;
import com.example.instagram_clone_2017.Utils.FilePaths;
import com.example.instagram_clone_2017.Utils.ImageManager;
import com.example.instagram_clone_2017.Utils.SectionsStatePagerAdapter;
import com.example.instagram_clone_2017.Utils.StringManipulations;
import com.example.instagram_clone_2017.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class AccountSettingActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingActivity";
    private static final int ACTIVITY_NUM = 4;

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ImageView backArrow;
    private Context mContext;
    private SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout relativeLayout;
    private BottomNavigationView mBottomNavigationView;

    private double mPhotoUploadProgress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        initFirebase();
        initView();
        setUpNavigationView(AccountSettingActivity.this);
        setUpSettingList(mContext);
        setUpFragment();
        getIncomingIntent();
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initFirebase()
    {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void initView()
    {
        mContext = AccountSettingActivity.this;
        backArrow = findViewById(R.id.backArrow);
        mViewPager = findViewById(R.id.container_viewpager);
        relativeLayout = findViewById(R.id.activity_account_setting_rell_layout_1);
        mBottomNavigationView = findViewById(R.id.bottomNavViewBar);
    }

    private void setUpNavigationView(Context context)
    {
        BottomNavigationViewHelper.enableNavigation(context, mBottomNavigationView);
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        Log.d(TAG, "getIncomingIntent: selected image = " + intent.getStringExtra(getString(R.string.selected_image)));
        Log.d(TAG, "getIncomingIntent: return_to_fragment = " + intent.getStringExtra(getString(R.string.return_to_fragment)));
        if (intent.hasExtra(getString(R.string.selected_image))
            || intent.hasExtra(getString(R.string.selected_uri))
        )
        {
            // if there is an imageUrl attched as an extra, then it was chosen from the gallery/photo fragment
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment)))
            {
                if (intent.hasExtra(getString(R.string.selected_image)))
                {
                    // set the new profile picture
                    Log.d(TAG, "getIncomingIntent: selected_image = true" );
                    uploadNewPhotos(getString(R.string.profile_photo),
                            null,
                            0,
                            intent.getStringExtra(getString(R.string.selected_image)),
                            null
                    );
                }
                else if (intent.hasExtra(getString(R.string.selected_uri)))
                {
                    Log.d(TAG, "getIncomingIntent: selected_bitmap = true" );

                    Uri imageUri = intent.getParcelableExtra(getString(R.string.selected_uri));
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        uploadNewPhotos(getString(R.string.profile_photo),
                                null,
                                0,
                                null,
                                bitmap
                        );
                    }
                    catch (IOException e)
                    {
                        Toast.makeText(mContext, "There Some Problem", Toast.LENGTH_SHORT).show();
                    }



                }
            }
            else
            {
                Log.d(TAG, "there are problem with slected_image or selected_bitmap" );
            }
        }
        if(intent.hasExtra(getString(R.string.calling_activity)))
        {
            //setmViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
            setmViewPager(0);
        }
    }

    private void setUpFragment()
    {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }

    private void setmViewPager(int fragmentNumber)
    {
        relativeLayout.setVisibility(View.GONE);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setUpSettingList(Context context)
    {
        ListView listView = findViewById(R.id.lvAccountSetting);
        ArrayList<String> option = new ArrayList<>();
        option.add(getString(R.string.edit_profile_fragment));
        option.add(getString(R.string.sign_out_fragment));
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, option);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setmViewPager(i);
            }
        });
    }

    private void uploadNewPhotos(String photoTypes,
                                 String captions,
                                 int imageCounts,
                                 String imageUrls,
                                 Bitmap bm
                                 )
    {
        FilePaths filePaths = new FilePaths();
        Uri firebaseUrl = null;
        Log.d(TAG, "uploadNewPhotos: ");
        // case 1 new_photo
        if (photoTypes.equals(getString(R.string.new_photo)))
        {
            Log.d(TAG, "uploadNewPhotos: uploading new photos = " + getString(R.string.new_photo) + ". ");
            String user_id = currentUser.getUid();
            StorageReference storageReference = mStorage.getReference()
                    .child(filePaths.FIREBASE_IMAGE_STORAGE
                            + "/" + user_id + "/photo" + (imageCounts + 1));
            // convert url into bitmap
            if (bm == null)
            {
                bm = ImageManager.getBitMap(imageUrls);
            }
            byte[] bytes = ImageManager.getByteFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    taskSnapshot.getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "IMAGE_URL = " + uri.toString());
                                    // add the new photo to photo node and user photos
                                    addPhotoToDatabase(captions, uri.toString());
                                    // navigate to the main feed so the user can see their photo
                                    Intent intent = new Intent(AccountSettingActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    AccountSettingActivity.this.setmViewPager(0);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AccountSettingActivity.this, "ASU", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AccountSettingActivity.this, "Photo Upload Failed " + e.toString() , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(AccountSettingActivity.this, "photo upload progress = "
                                + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });
        }
        // case 2 profile_photo
        if(photoTypes == getString(R.string.profile_photo))
        {
            Log.d(TAG, "uploadNewPhotos: " + "Phototypes = " + getString(R.string.profile_photo));
            String user_id = currentUser.getUid();
            StorageReference storageReference = mStorage.getReference()
                    .child(filePaths.FIREBASE_IMAGE_STORAGE
                            + "/" + user_id + "/profile_photo");
            // convert url into bitmap
            if (bm == null)
            {
                bm = ImageManager.getBitMap(imageUrls);
            }
            byte[] bytes = ImageManager.getByteFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "IMAGE_URL = " + uri.toString());
                                    // insert into user_settings node
                                    setProfilePhoto(uri.toString());
                                    AccountSettingActivity.this.setmViewPager(0);

                                    //Todo: after succesing changing the profile image go to Edit_profile_fragment

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AccountSettingActivity.this, "ASU", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AccountSettingActivity.this, "Photo Upload Failed " + e.toString() , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(AccountSettingActivity.this, "photo upload progress = "
                                + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });
        }
        else
        {
            Log.d(TAG, "uploadNewPhotos: " + "ASUUU");
            Log.d(TAG, "uploadNewPhotos: " + "Phototypes = " + photoTypes);
        }
    }

    private void setProfilePhoto(String imageUrl)
    {
        Log.d(TAG, "setProfilePhoto: " + imageUrl);
        mDatabase.getReference().child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_settings))
                .child(currentUser.getUid())
                .child(getString(R.string.profile_photo))
                .setValue(imageUrl);
    }

    private String getTimeStamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String captions, String firebaseUrl)
    {
        Log.d(TAG, "addPhotoToDatabase ");
        String newPhotoKey = mDatabase.getReference().child(getString(R.string.instagram_clone)).push().getKey();
        String tags = StringManipulations.getTags(captions);
        Photo photo = new Photo();
        photo.setCaptions(captions);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(firebaseUrl);
        photo.setTags(tags);
        photo.setUser_id(currentUser.getUid());
        photo.setPhoto_id(newPhotoKey);
        Log.d(TAG, "addPhotoToDatabase " + "newPhotoKey = " + newPhotoKey + ", firebaseUrl = " + firebaseUrl);
        // insert to database
        mDatabase.getReference().child(getString(R.string.instagram_clone))
                .child(getString(R.string.users_photos))
                .child(currentUser.getUid())
                .child(newPhotoKey)
                .setValue(photo);
        mDatabase.getReference().child(getString(R.string.instagram_clone))
                .child(getString(R.string.photos))
                .child(newPhotoKey)
                .setValue(photo);
    }


}
