package com.example.instagram_clone_2017.Profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Login.LoginActivity;
import com.example.instagram_clone_2017.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignOutFragment extends Fragment {
    private FirebaseAuth mAuth;
    private Button yes, no;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_out, container, false);
        setUpFirebase();
        initView(view);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "SuccesFull To Sign Out", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent intent = new Intent(getContext(), ProfileActivity.class);
                 startActivity(intent);
            }
        });
        return view;
    }
    private void setUpFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
    }
    private void initView(View view)
    {
        yes = view.findViewById(R.id.fragment_sign_out_yes);
        no = view.findViewById(R.id.fragment_sign_out_no);
    }
}
