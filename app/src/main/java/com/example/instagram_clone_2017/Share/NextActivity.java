package com.example.instagram_clone_2017.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Profile.AccountSettingActivity;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.FilePaths;
import com.example.instagram_clone_2017.Utils.ImageManager;
import com.example.instagram_clone_2017.Utils.StringManipulations;
import com.example.instagram_clone_2017.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;
    private static FirebaseUser currentUser;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    private static FirebaseStorage firebaseStorage;

    private ImageView backArrow, shareImage;
    private EditText mCaption;
    private TextView share;
    private String imageUrl;
    private Bitmap bitmap;

    // var
    private String mAppend = "file:/";
    private int imageCount = 0;
    private double mPhotoUploadProgress = 0;
    private Intent intent;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
//        Log.d(TAG, getIntent().getStringExtra(getString(R.string.selected_image)));
        initFirebase();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (currentUser != null)
                {
                    init();
                    setImage();
                    Log.d(TAG, "onDatachangeImageCount = " + imageCount);
                    mDatabase.getReference().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            imageCount = getImageCount(snapshot);
                            Log.d(TAG, "onDatachangeImageCount = " + imageCount);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    backArrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Upload the Image Into Firebase
                            Toast.makeText(NextActivity.this, "Attempting Upload New Photos", Toast.LENGTH_SHORT).show();
                            String caption = mCaption.getText().toString();
                            if (intent.hasExtra(getString(R.string.selected_image)))
                            {
                                imageUrl = getIntent().getStringExtra(getString(R.string.selected_image));
                                uploadNewPhotos(getString(R.string.new_photo), caption, imageCount, imageUrl, null);
                            }
                            else if (intent.hasExtra(getString(R.string.selected_bitmap)))
                            {
                                bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                                uploadNewPhotos(getString(R.string.new_photo), caption, imageCount, null, bitmap);
                            }
                            else if (intent.hasExtra(getString(R.string.selected_uri)))
                            {
                                imageUri = intent.getParcelableExtra(getString(R.string.selected_uri));
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                    uploadNewPhotos(getString(R.string.new_photo), caption, imageCount, null, bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                else
                {
                    mAuth.signOut();
                    Intent intent = new Intent(NextActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
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
            StorageReference storageReference = firebaseStorage.getReference()
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
                                    Intent intent = new Intent(NextActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    //NextActivity.this.setmViewPager(0);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NextActivity.this, "ASU", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NextActivity.this, "Photo Upload Failed " + e.toString() , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(NextActivity.this, "photo upload progress = "
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
            StorageReference storageReference = firebaseStorage.getReference()
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
                                    //NextActivity.this.setmViewPager(0);
                                    //Todo: after succesing changing the profile image go to Edit_profile_fragment
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NextActivity.this, "ASU", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NextActivity.this, "Photo Upload Failed " + e.toString() , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(NextActivity.this, "photo upload progress = "
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
//        addNewComment(photo.getTags().toString(), photo);
    }

//    private void addNewComment(String newComment, Photo photo)
//    {
//        Log.d(TAG, "addNewComment: added newComment: " + newComment);
//        String commentId = mDatabase.getReference().push().getKey();
//
//        Comment comment = new Comment();
//        comment.setComment(newComment);
//        comment.setDate_created(getTimeStamp());
//        comment.setUser_id(currentUser.getUid());
//
//        // insert into photos node
//        mDatabase.getReference().child(getString(R.string.instagram_clone))
//                .child(getString(R.string.photos))
//                .child(photo.getPhoto_id())
//                .child(getString(R.string.field_comments))
//                .child(commentId)
//                .setValue(comment);
//        // insert into user_photos node
//        mDatabase.getReference().child(getString(R.string.instagram_clone))
//                .child(getString(R.string.users_photos))
//                .child(currentUser.getUid())
//                .child(photo.getPhoto_id())
//                .child(getString(R.string.field_comments))
//                .child(commentId)
//                .setValue(comment);
//
//    }

    private int getImageCount(DataSnapshot snapshot)
    {
        int count = 0;
        for (DataSnapshot ds : snapshot
                .child("instagram_clone")
                .child("users_photos")
                .child(currentUser.getUid())
                .getChildren())
        {
            count++;
        }
        return count;
    }

    private void someMethod()
    {
        /***
         *
         *      step 1)
         *      Create Data Model for Photo
         *
         *      step 2)
         *      Add Properties to Object (caption, data, imageUrl, photo_id, tags, user_id)
         *
         *      step 3)
         *      Count the Number of Photo That the User Already Has
         *
         *      step 4)
         *      a)  Upload the Photo to Firebase Storage and Insert Two New Nodes in Firebase Database
         *      b) 'photos' node
         *      c) 'user_photos' node
         *
         * **/

    }
    private void setImage()
    {
       intent = getIntent();
       if (intent.hasExtra(getString(R.string.selected_image)))
       {
           imageUrl = getIntent().getStringExtra(getString(R.string.selected_image));
           Log.d(TAG, "setImage: got new email url " + imageUrl);
           UniversalImageLoader.setImage(imageUrl,
                   shareImage, null, mAppend);
       }
       else if (intent.hasExtra(getString(R.string.selected_bitmap)))
       {
           bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
           Log.d(TAG, "setImage: got new bitmap");
           shareImage.setImageBitmap(bitmap);
       }
       else if (intent.hasExtra(getString(R.string.selected_uri)))
       {
           imageUri = getIntent().getParcelableExtra(getString(R.string.selected_uri));
           shareImage.setImageURI(imageUri);
       }
    }

    private void initFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
    }
    private void init()
    {
        backArrow = findViewById(R.id.snippet_top_next_toolbar_back);
        shareImage = findViewById(R.id.activity_next_image_share);
        mCaption = findViewById(R.id.activity_next_caption);
        share = findViewById(R.id.snippet_top_next_toolbar_share);
    }

}
