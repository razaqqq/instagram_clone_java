package com.example.instagram_clone_2017.Profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Dialog.ConfirmPasswordDialog;
import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersModified;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Share.NextActivity;
import com.example.instagram_clone_2017.Share.ShareActivity;
import com.example.instagram_clone_2017.Utils.FilePaths;
import com.example.instagram_clone_2017.Utils.ImageManager;
import com.example.instagram_clone_2017.Utils.StringManipulations;
import com.example.instagram_clone_2017.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener
{

    private static final String TAG = "EDIT_PROFILE_FRAGMENTS";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage firebaseStorage;


    private double mPhotoUploadProgress = 0;
    private int imageCount = 0;

    // Widget
    private CircleImageView profilePhoto;
    private ImageView backArrow;
    private ProgressBar progressBar;
    private ImageView imgSave, imgBack;
    private TextView tvChangePhoto;
    private EditText edUsername, edDisplayname, edWebsite, edDescriptions, edEmail, edNoTelp;

    @Override
    public void onConfirmPassword(String password) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentUser.getEmail(), password);
        Users users = new Users();
        // Promt the user to re-provide their sign in credential
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful())
                      {
                          Toast.makeText(getActivity(), "Task Is Succesful Reaunthenticate", Toast.LENGTH_SHORT).show();
                          users.setEmail(edEmail.getText().toString());
                          mAuth.fetchSignInMethodsForEmail(edEmail.getText().toString())
                                  .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                              @Override
                              public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        if (task.getResult().getSignInMethods().equals(edEmail.getText().toString()))
                                        {
                                            Toast.makeText(getActivity(), "That Email Already in Use", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(getActivity(), "That Email is Avaible", Toast.LENGTH_SHORT).show();
                                            mAuth.getCurrentUser().updateEmail(edEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                Toast.makeText(getActivity(), "Email Already Updated", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(getActivity(), "Email Failed to Update", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                            firebaseDatabase.getReference().child("instagram_clone")
                                                    .child("users").child(userId).child("email")
                                                    .setValue(users.getEmail());
                                        }
                                    }
                                    else
                                    {

                                    }
                              }
                          });

                      }
                      else
                      {
                          Toast.makeText(getActivity(), "Task Is Failed to Reauntenticate", Toast.LENGTH_SHORT).show();
                      }
                    }
                });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        initFirebase();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = mAuth.getCurrentUser();
                if (currentUser != null)
                {
                    initView(view);
                    getDataFromDatabase();
                    backArrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                            startActivity(intent);
                        }
                    });
                    tvChangePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // ToDo: Chage Image Photo Edit Profile Fragments
                            Intent intent = new Intent(getActivity(), ShareActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                    saveProfileSettings();
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

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void saveProfileSettings()
    {
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tempUsername = edUsername.getText().toString();
                String temDisplayName = edDisplayname.getText().toString();
                String tempWebsite = edWebsite.getText().toString();
                String tempDesc = edDescriptions.getText().toString();
                String tempEmail = edEmail.getText().toString();
                String tempNoTelp = edNoTelp.getText().toString();
                firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = new Users();
                        for (DataSnapshot data : snapshot.child("instagram_clone").child("users").getChildren())
                        {
                            if (data.getKey().equals(userId))
                            {
                                users.setUsername(data.getValue(Users.class).getUsername());
                                users.setEmail(data.getValue(Users.class).getEmail());
                            }
                        }
                        // case 1 the user di not change username
                        if (!users.getUsername().equals(tempUsername))
                        {
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users").child(userId).child("username")
                                    .setValue(tempUsername);
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users_settings").child(userId).child("username")
                                    .setValue(tempUsername);
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users_settings").child(userId).child("display_name")
                                    .setValue(temDisplayName);
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users_settings").child(userId).child("website")
                                    .setValue(tempWebsite);
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users_settings").child(userId).child("descriptions")
                                    .setValue(tempDesc);
                            firebaseDatabase.getReference().child("instagram_clone")
                                    .child("users").child(userId).child("phone_number")
                                    .setValue(edNoTelp.getText().toString());
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Please Make Different Useername", Toast.LENGTH_SHORT).show();
                        }

                        if(!users.getEmail().equals(tempEmail))
                        {
                            // step 1 reunticate
                            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                            dialog.show(getFragmentManager(), "ConfirmPasswordDialog");
                            dialog.setTargetFragment(EditProfileFragment.this, 1);

                            // step 2chechk if the email already register

                            // step 3 change the email

                        }
                        // case 2 the user changed their username we need to check for uniqueness
                        else
                        {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
    }

    private void initView(View view)
    {
        profilePhoto = view.findViewById(R.id.edit_profile_pic);
        progressBar = view.findViewById(R.id.fragment_edit_profile_progress_bar);
        imgSave = view.findViewById(R.id.snippet_top_edit_profile_toolbar_save);
        tvChangePhoto = view.findViewById(R.id.snippet_center_edit_profile_change_photo);
        edUsername = view.findViewById(R.id.snippet_center_edit_profile_rel2_1_edText_username);
        edDisplayname = view.findViewById(R.id.snippet_center_edit_profile_rel2_2_edText_display_name);
        edWebsite = view.findViewById(R.id.snippet_center_edit_profile_rel2_3_edText_website);
        edDescriptions = view.findViewById(R.id.snippet_center_edit_profile_rel2_4_edText_desc);
        edEmail = view.findViewById(R.id.snippet_center_edit_profile_rel3_web);
        edNoTelp = view.findViewById(R.id.snippet_center_edit_profile_rel3_no_hp);
        backArrow = view.findViewById(R.id.backArrow_edit_profile);
    }

    private void initFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUser = mAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();


    }

    private void getDataFromDatabase()
    {
        userId = currentUser.getUid();
        firebaseDatabase.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // retrieve user information from database
                UsersModified usersModified = getUserSettings(snapshot, userId);
                edUsername.setText(usersModified.getUsersSettings().getUsername());
                edDisplayname.setText(usersModified.getUsersSettings().getDisplay_name());
                edWebsite.setText(usersModified.getUsersSettings().getWebsite());
                edDescriptions.setText(usersModified.getUsersSettings().getDescriptions());
                edEmail.setText(usersModified.getUsers().getEmail());
                edNoTelp.setText(usersModified.getUsers().getPhone_number());
                UniversalImageLoader.setImage(
                        usersModified.getUsersSettings().getProfile_photo(),
                        profilePhoto,
                        progressBar,
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

    private void setProfileImage()
    {
        String imgUrl = "https://techdaily.id/wp-content/uploads/2021/07/Android-main.jpg";
        UniversalImageLoader.setImage(imgUrl, profilePhoto, null, "");
    }

}
