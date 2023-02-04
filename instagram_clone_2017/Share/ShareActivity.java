package com.example.instagram_clone_2017.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.BottomNavigationViewHelper;
import com.example.instagram_clone_2017.Utils.Permissions;
import com.example.instagram_clone_2017.Utils.SectionPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.security.Permission;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIVY_PERMISSIONS_REQUEST = 1;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        if (checkPermissionArray(Permissions.PERMISSIONS))
        {
            //setUpNavigationView(ShareActivity.this);
            setUpViewPager();
        }
        else
        {
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }
    /*
    * return the current tab number
    * 0 = GalleryFragment
    * 1 = PhotoFragment
    * */
    public int getCurrentTabNumber()
    {
        return mViewPager.getCurrentItem();
    }
    /*
    * SetUp ViewPager for manage Tabs
    * */
    private void setUpViewPager()
    {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());
        mViewPager = (ViewPager) findViewById(R.id.container_viewpager);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.layout_bottom_tabs_table_layout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setText("GALLERY");
        tabLayout.getTabAt(1).setText("PHOTO");
    }

    public int getTask()
    {
        Log.d(TAG, "getTask: TASK " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    private void verifyPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIVY_PERMISSIONS_REQUEST
                );
    }

    private boolean checkPermissionArray(String[] permissions) {

        for (int i = 0; i < permissions.length; i++)
        {
            String check = permissions[i];
            if (!checkPermissions(check))
            {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission) {
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permissions not granted " + permission , Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            Toast.makeText(this, "Permissions granted " + permission, Toast.LENGTH_SHORT).show();
            return true;
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
