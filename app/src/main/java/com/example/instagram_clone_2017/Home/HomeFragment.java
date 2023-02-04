package com.example.instagram_clone_2017.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Model.Comment;
import com.example.instagram_clone_2017.Model.Like;
import com.example.instagram_clone_2017.Model.Photo;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.MainFeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";


    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowings;
    private ArrayList<Photo> mPaginatedPhotos;
    private ListView mListView;
    private MainFeedListAdapter mAdapter;
    private DatabaseReference reference;
    private ImageView backArrow;
    private int mResults;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.fragment_home_list_view);
        mFollowings = new ArrayList<>();
        mPhotos = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();


        getFollowing();
        return view;
    }



    private void getFollowing()
    {
        Log.d(TAG, "getFollowing: searching for following");
        Query query = reference
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user  " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue() );
                    mFollowings.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowings.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                // get the photoss
                getPhotos();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getPhotos()
    {
        Log.d(TAG, "getPhotos: getting photos");

        for (int i = 0; i < mFollowings.size(); i++)
        {
            final int count = i;
            Query query = reference
                    .child(getString(R.string.instagram_clone))
                    .child(getString(R.string.users_photos))
                    .child(mFollowings.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowings.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

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
                        mPhotos.add(photo);

                    }
                    if (count >= mFollowings.size() - 1)
                    {
                        //displays our photos
                        displayPhoto();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }
    private void displayPhoto()
    {
        mPaginatedPhotos = new ArrayList<>();

        if (mPhotos != null)
        {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo photo, Photo t1) {
                        return t1.getDate_created().compareTo(photo.getDate_created());
                    }
                });

                int iteration = mPhotos.size();

                if (iteration > 10)
                {
                    iteration = 10;
                }
                mResults = 10;
                for (int i = 0; i < iteration; i++)
                {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mAdapter = new MainFeedListAdapter(getActivity(), R.layout.layout_mainfeed_list_item,
                        mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            }catch (NullPointerException e)
            {
                Log.e(TAG, "displayPhoto: NullPointerException " + e.getMessage() );
            }catch (IndexOutOfBoundsException e)
            {
                Log.e(TAG, "displayPhoto: IndexOutOfBoundsException " + e.getMessage() );
            }

        }
    }

    public void displayMorePhotos()
    {
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try {

            if (mPhotos.size() > mResults && mPhotos.size() > 0)
            {
                int iterations;
                if (mPhotos.size() > (mResults + 10))
                {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }
                else
                {
                    Log.d(TAG, "displayMorePhotos: there less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photo to the paginated results
                for (int i = 0; i < mResults + iterations; i++)
                {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();

            }

        }catch (NullPointerException e)
        {
            Log.e(TAG, "displayPhoto: NullPointerException " + e.getMessage() );
        }catch (IndexOutOfBoundsException e)
        {
            Log.e(TAG, "displayPhoto: IndexOutOfBoundsException " + e.getMessage() );
        }
    }

}
