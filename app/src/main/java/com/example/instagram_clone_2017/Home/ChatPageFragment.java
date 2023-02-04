package com.example.instagram_clone_2017.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.Model.MessageModel;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.ChatAdapter;
import com.example.instagram_clone_2017.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatPageFragment extends Fragment {

    private static final String TAG = "CHAT_PAGE_FRAGMENT";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser currentUser;
    private String userId;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //var
    UsersSettings usersSettings;

    // widget
    private ImageView backArrow, checkMark;
    private CircleImageView profilePic;
    private TextView username;
    private EditText edMessage;
    private RecyclerView chatRecView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_page, container, false);

        backArrow = view.findViewById(R.id.snippet_fragment_chat_page_toolbar_back_arrow);
        profilePic = view.findViewById(R.id.snippet_fragment_chat_page_toolbar_profile_pic);
        username = view.findViewById(R.id.snippet_fragment_chat_page_tollbar_username);
        checkMark = view.findViewById(R.id.fragment_chat_page_check_mark);
        edMessage = view.findViewById(R.id.fragment_chat_pages_edit_text);
        chatRecView = view.findViewById(R.id.fragment_chat_page_rec_view_showing_chat);

        initFirebase();

        usersSettings = getUserSettingFromBundle();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (currentUser != null)
                {
                    backArrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().getSupportFragmentManager().popBackStack();

                        }
                    });

                    final String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String receiveId = usersSettings.getUser_id();

                    UniversalImageLoader.setImage(
                            usersSettings.getProfile_photo(),
                            profilePic,
                            null,
                            ""
                            );
                    username.setText(usersSettings.getUsername().toString());

                    final ArrayList<MessageModel> messageModels = new ArrayList<>();
                    final ChatAdapter chatAdapter = new ChatAdapter(messageModels, getActivity(), receiveId);

                    chatRecView.setAdapter(chatAdapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    chatRecView.setLayoutManager(layoutManager);

                    final String senderRoom = senderId + receiveId;
                    final String receiverRoom = receiveId + senderId;


                    firebaseDatabase.getReference().child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.chat_message))
                                    .child(senderRoom)
                                    .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    messageModels.clear();
                                                    for (DataSnapshot dataSnapshot: snapshot.getChildren())
                                                    {
                                                        MessageModel model = dataSnapshot.getValue(MessageModel.class);
                                                        model.setMessageId(dataSnapshot.getKey());
                                                        messageModels.add(model);
                                                    }
                                                    chatAdapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });


                    checkMark.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String message = edMessage.getText().toString();
                            final MessageModel model = new MessageModel(senderId, message);
                            model.setTimeStamp(new Date().getTime());
                            edMessage.setText("");

                            firebaseDatabase.getReference()
                                    .child(getString(R.string.instagram_clone))
                                    .child(getString(R.string.chat_message))
                                    .child(senderRoom)
                                    .push()
                                    .setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            firebaseDatabase.getReference()
                                                    .child(getString(R.string.instagram_clone))
                                                    .child(getString(R.string.chat_message))
                                                    .child(receiverRoom)
                                                    .push()
                                                    .setValue(model)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    });
                                        }
                                    });



                        }
                    });
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

    private UsersSettings getUserSettingFromBundle()
    {
        Log.d(TAG, "getUserSettingFromBundle: gettIngUserSettingsFromBundle");
        Bundle bundle = this.getArguments();
        if (bundle != null)
        {
            return bundle.getParcelable(getString(R.string.users_settings));
        }
        else
        {
            return  null;
        }
    }

}
