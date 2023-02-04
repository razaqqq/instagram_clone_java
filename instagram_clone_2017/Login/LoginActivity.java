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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.instagram_clone_2017.Home.MainActivity;
import com.example.instagram_clone_2017.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Context mContext;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    AppCompatButton loginBtn;
    TextInputEditText edEmail, edPassword;
    ProgressBar progressBar;
    TextView pleaseWait, signUp, textVerEmail;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpView();
        setUpFirebase();
        checkUser();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edEmail.getText().toString();
                String password = edPassword.getText().toString();
                if (!email.equals("") && !password.equals(""))
                {
                    progressBar.setVisibility(View.VISIBLE);
                    pleaseWait.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        progressBar.setVisibility(View.GONE);
                                        pleaseWait.setVisibility(View.GONE);
                                        if (task.getResult().getUser().isEmailVerified())
                                        {
                                            textVerEmail.setVisibility(View.GONE);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                        else
                                        {
                                            Toast.makeText(mContext, "Your Email Is Not Verivied", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(mContext, "Send Email Verivication", Toast.LENGTH_SHORT).show();
                                            task.getResult().getUser().sendEmailVerification()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(mContext, "Success Send Email Verivication", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(mContext, "Failed to Send Email Verivication ", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                    else
                                    {
                                        progressBar.setVisibility(View.GONE);
                                        pleaseWait.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(mContext, "Please Input Your Email and Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkUser()
    {
        if (currentUser != null)
        {
            if (currentUser.isEmailVerified())
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
            else
            {
                textVerEmail.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUpView()
    {
        loginBtn = findViewById(R.id.activity_login_button);
        edEmail = findViewById(R.id.activity_login_email);
        edPassword = findViewById(R.id.activity_login_password);
        progressBar = findViewById(R.id.activity_login_progess_bar);
        pleaseWait = findViewById(R.id.activity_login_please_wait);
        signUp = findViewById(R.id.activity_login_signUp);
        mContext = LoginActivity.this;
        textVerEmail = findViewById(R.id.activity_login_ver_email);
    }
    private void setUpFirebase()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }
}
