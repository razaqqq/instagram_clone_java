package com.example.instagram_clone_2017.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessageFragment extends Fragment {
    private static final String TAG = "MessageFragment";



    ArrayList<String> following = new ArrayList<>();
    FirebaseDatabase database;
    RecyclerView recview;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        database = FirebaseDatabase.getInstance();
        recview = view.findViewById(R.id.fragment_message_rec_view);





        Query query = database.getReference()
                .child(getString(R.string.instagram_clone))
                .child(getString(R.string.following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                following.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: MessageFragment OnDatachange " + dataSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                    String idFollowing = dataSnapshot.child(getString(R.string.field_user_id)).getValue().toString();
                    following.add(idFollowing);
                }
                recview.setVisibility(View.VISIBLE);
                UsersAdapter adapter = new UsersAdapter(following, getContext(), recview);
                recview.setAdapter(adapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                recview.setLayoutManager(layoutManager);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return view;
    }

    @Override
    public void onStart() {

        super.onStart();
    }


    public void showRecView()
    {
        recview.setVisibility(View.VISIBLE);
    }


}
