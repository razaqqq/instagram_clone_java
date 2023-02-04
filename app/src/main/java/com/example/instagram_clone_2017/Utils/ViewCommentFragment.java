package com.example.instagram_clone_2017.Utils;

import android.content.Context;
import android.content.Intent;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Like;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.ViewPostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

public class ViewCommentFragment extends Fragment {

    private static final String TAG = "ViewCommentFragment";

    public ViewCommentFragment()
    {
        super();
        setArguments(new Bundle());
    }

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseAuth.AuthStateListener mAuthListener;


    // widgets
    private ImageView mBackArrow, mCheckmark;
    private EditText mComment;

    // vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private ListView mListView;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comment, container, false);

        mBackArrow = view.findViewById(R.id.snippet_comment_fragment_ic_back_arrow);
        mCheckmark = view.findViewById(R.id.fragment_view_comment_iv_post_comment);
        mComment = view.findViewById(R.id.fragment_view_comment_ed_comment);
        mComments = new ArrayList<>();
        mListView = (ListView) view.findViewById(R.id.fragment_view_comment_list_view);
        mContext = getActivity();

        initFirebase();



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (currentUser != null)
                {
                    try {

                        mPhoto = getPhotoFromBundle();


                        creatingListOfComment();

                    }
                    catch (NullPointerException e)
                    {
                        Log.e(TAG, "onCreateView: NullPointerExceptions, photo was null from bundle" + e );
                    }


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


    private void creatingListOfComment()
    {
        Log.d(TAG, "creatingListOfComment: ");

        if (mPhoto.getComments().size() == 0)
        {
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaptions());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setUpWidgets();
        }

        firebaseDatabase.getReference()
                .child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener () {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Query query = firebaseDatabase.getReference()
                                .child(mContext.getString(R.string.instagram_clone))
                                .child(mContext.getString(R.string.photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot singleSnapsot : snapshot.getChildren())
                                {

                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapsot.getValue();
                                    photo.setCaptions(objectMap.get(mContext.getString(R.string.field_captions)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaptions());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());
                                    mComments.add(firstComment);

                                    for (DataSnapshot dSnapshot : singleSnapsot.child(mContext.getString(R.string.field_comments)).getChildren())
                                    {

                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }
                                    photo.setComments(mComments);
                                    mPhoto = photo;
                                    setUpWidgets();
//                    for (DataSnapshot dSnapshot : singleSnapsot.child(getString(R.string.field_likes)).getChildren())
//                    {
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: cancelled error = " + error.toString());
                            }
                        });
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Log.d(TAG, "onDataChange: changed");
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onChildRemoved: removed");
                    }
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Log.d(TAG, "onChildMoved: moved");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: canceled");
                    }
                });
    }

    private void setUpWidgets()
    {


        CommentListenerAdapter adapter = new CommentListenerAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back");
                if (getCallingActivityFromBundle().equals(getString(R.string.main_activity)))
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((MainActivity)getActivity()).showLayout();
                }
                else
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mComment.getText().toString().equals(""))
                {
                    Log.d(TAG, "onClick: attempting to submit new comment");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyBoard();
                }
                else
                {
                    Toast.makeText(getActivity(), "you cant post blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void closeKeyBoard()
    {
        View view = getActivity().getCurrentFocus();
        if (view != null)
        {
            InputMethodManager mm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment)
    {
        Log.d(TAG, "addNewComment: added newComment: " + newComment);

        String commentId = firebaseDatabase.getReference().push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(currentUser.getUid());



        // insert into photos node
        firebaseDatabase.getReference().child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);

        // insert into user_photos node
        firebaseDatabase.getReference().child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);

    }

    private String getTimeStamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return  sdf.format(new Date());
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

    private String getCallingActivityFromBundle()
    {
        Log.d(TAG, "getPhotoFromBundle: arguments " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getString(mContext.getString(R.string.main_activity));
        }
        else
        {
            return null;
        }

    }

    private Photo getPhotoFromBundle()
    {
        Log.d(TAG, "getPhotoFromBundle: arguments " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getParcelable(mContext.getString(R.string.photo));
        }
        else
        {
            return null;
        }

    }

}
