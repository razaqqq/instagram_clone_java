package com.example.instagram_clone_2017.Utils;



import android.content.Context;
import android.media.Image;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<Users> {

    private static final String TAG = "UserListAdapter";

    private LayoutInflater mInflater;
    private List<Users> mUser = null;
    private int layoutResource;
    private Context mContext;

    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<Users> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUser = objects;
    }

    private static class ViewHolder
    {
        TextView username, email;
        CircleImageView profileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView =  mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.layout_user_list_item_username);
            holder.email = convertView.findViewById(R.id.layout_user_list_item_email);
            holder.profileImage = convertView.findViewById(R.id.layout_user_list_item_profile_image);

            convertView.setTag(holder);

        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.username.setText(getItem(position).getUsername().toString());
        holder.email.setText(getItem(position).getEmail().toString());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(mContext.getString(R.string.instagram_clone))
                .child(mContext.getString(R.string.users_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: found user : " +
                            singleSnapshot.getValue(UsersSettings.class).toString());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UsersSettings.class).getProfile_photo(),
                            holder.profileImage
                            );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return convertView;

    }
}
