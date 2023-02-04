package com.example.instagram_clone_2017.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Model.UsersSettings;
import com.example.instagram_clone_2017.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    // Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseUser user;

    // Widget
    Context mContext;
    TextInputEditText email, fullname, password, username;
    AppCompatButton register;
    TextView login;
    ProgressBar progressBar;
    TextView pleasewait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setUpView();
        setUpFirebase();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emails = email.getText().toString();
                String fullnames = fullname.getText().toString();
                String passwords = password.getText().toString();
                String usernames = username.getText().toString();
                if (!emails.equals("") && !fullnames.equals("") && !passwords.equals("") && !usernames.equals(""))
                {
                    progressBar.setVisibility(View.VISIBLE);
                    pleasewait.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(emails, passwords)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        progressBar.setVisibility(View.GONE);
                                        pleasewait.setVisibility(View.GONE);
                                        Toast.makeText(mContext, "SuccesFull Create, Send Verivication Email", Toast.LENGTH_SHORT).show();
                                        task.getResult().getUser().sendEmailVerification()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(mContext, "Succesfull Send Verivication", Toast.LENGTH_SHORT).show();
                                                        Users users = new Users(
                                                                task.getResult().getUser().getUid(),
                                                                "087737873747",
                                                                emails,
                                                                usernames,
                                                                passwords
                                                        );
                                                        UsersSettings usersSettings = new UsersSettings(
                                                                usernames,
                                                                fullnames,
                                                                0,
                                                                0,
                                                                0,
                                                                "default photo",
                                                                "www.google.com",
                                                                "This Is My First Apps",
                                                                task.getResult().getUser().getUid()
                                                        );
                                                        database.getReference()
                                                                .child(getString(R.string.instagram_clone))
                                                                .child(getString(R.string.users))
                                                                .child(task.getResult().getUser().getUid())
                                                                .setValue(users);
                                                        database.getReference()
                                                                .child(getString(R.string.instagram_clone))
                                                                .child(getString(R.string.users_settings))
                                                                .child(task.getResult().getUser().getUid())
                                                                .setValue(usersSettings);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(mContext, "Failed to send verivication emails", Toast.LENGTH_SHORT).show();
                                                        Toast.makeText(mContext, "Error : " + e.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                    else
                                    {
                                        progressBar.setVisibility(View.GONE);
                                        pleasewait.setVisibility(View.GONE);
                                        Toast.makeText(mContext, "Register Failed, \n" + task.getException().toString() , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(mContext, "Please Input Your Email, Fullnames, Passwords", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    public void setUpView()
    {
        mContext = RegisterActivity.this;
        email = findViewById(R.id.activity_sign_in_email);
        fullname = findViewById(R.id.activity_sign_in_full_name);
        password = findViewById(R.id.activity_sign_in_password);
        register = findViewById(R.id.activity_sign_in_register);
        login = findViewById(R.id.activity_sign_in_login);
        progressBar = findViewById(R.id.activity_sign_in_progess_bar);
        pleasewait = findViewById(R.id.activity_sign_in_loading_data);
        username = findViewById(R.id.activity_sign_in_username);
    }

    public void setUpFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = mAuth.getCurrentUser();
    }

}
