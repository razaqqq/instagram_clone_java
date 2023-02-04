package com.example.instagram_clone_2017.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instagram_clone_2017.Model.Users;
import com.example.instagram_clone_2017.Profile.ProfileActivity;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.BottomNavigationViewHelper;
import com.example.instagram_clone_2017.Utils.UserListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 1;

    //widgets
    private EditText searchParam;
    private ListView mListView;

    //var
    private List<Users> mUserList;
    private UserListAdapter mUserListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchParam = (EditText) findViewById(R.id.snippet_search_bar_search);
        mListView = (ListView) findViewById(R.id.activity_search_list_view);
        hideSoftKeyboard();
        setUpNavigationView(SearchActivity.this);
        initTextListener();
    }

    private void initTextListener()
    {
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();
//        searchParam.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//            @Override
//            public void afterTextChanged(Editable editable) {
//                String text = searchParam.getText().toString().toLowerCase(Locale.getDefault());
//                searchForMatch(text);
//            }
//        });
        searchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = searchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword)
    {
        Log.d(TAG, "searchForMatch: searching for match");
        mUserList.clear();
        // update the users lis
        if (keyword.length() == 0)
        {

        }
        else
        {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.instagram_clone))
                    .child(getString(R.string.users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    {
                        Log.d(TAG, "onDataChange: found the user "
                                + singleSnapshot.getValue(Users.class).toString());
                        mUserList.add(singleSnapshot.getValue(Users.class));
                        // update the user list view
                        updateUserList();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateUserList()
    {
        Log.d(TAG, "updateUserList: updating users list");
        mUserListAdapter = new UserListAdapter(SearchActivity.this,
                R.layout.layout_user_list_item,
                mUserList
                );
        mListView.setAdapter(mUserListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected user " + mUserList.get(i).toString());
                //Navigate to profileActivity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(i));
                startActivity(intent);
            }
        });
    }

    private void hideSoftKeyboard()
    {
      if (getCurrentFocus() != null)
      {
          InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
      }
    }

    private void setUpNavigationView(Context context)
    {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
