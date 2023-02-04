package com.example.instagram_clone_2017.Utils;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone_2017.Home.ChatPageFragment;
import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final static String TAG = "USERS_ADAPTER";

    ArrayList<String> following;
    ArrayList<UsersSettings> usersList;
    Context mContext;
    UsersSettings usersSettings;
    RecyclerView recView;

    public UsersAdapter(ArrayList<String> following, Context mContext, RecyclerView recView) {
        this.following = following;
        this.mContext = mContext;
        this.recView = recView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        usersSettings = new UsersSettings();
        Query query = FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.instagram_clone))
                        .child(mContext.getString(R.string.users_settings))
                        .orderByChild(mContext.getString(R.string.field_user_id))
                        .equalTo(following.get(position))
                ;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    usersSettings = dataSnapshot.getValue(UsersSettings.class);
                    UniversalImageLoader.setImage(
                            usersSettings.getProfile_photo(),
                            holder.profileImage,
                            null,
                            ""
                    );
                    holder.username.setText(usersSettings.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Linear Layout Clickerd", Toast.LENGTH_SHORT).show();
                ChatPageFragment fragment = new ChatPageFragment();
                Bundle args = new Bundle();
                args.putParcelable(mContext.getString(R.string.users_settings), usersSettings);
                fragment.setArguments(args);

                FragmentTransaction transaction = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.activity_main_rell_layout_parent, fragment);
                transaction.addToBackStack(mContext.getString(R.string.chat_page_fragment));
                transaction.commit();

                recView.setVisibility(View.GONE);
            }
        });





    }

    @Override
    public int getItemCount() {
        return following.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView username;
        TextView lastMessage;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.sample_show_user_circle_image);
            username = itemView.findViewById(R.id.sample_show_user_username);
            lastMessage = itemView.findViewById(R.id.sample_show_user_last_message);
            linearLayout = itemView.findViewById(R.id.sample_show_user_linear_horizontal);
        }
    }

}
