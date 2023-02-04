package com.example.instagram_clone_2017.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.instagram_clone_2017.Profile.AccountSettingActivity;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.FilePaths;
import com.example.instagram_clone_2017.Utils.FileSearch;
import com.example.instagram_clone_2017.Profile.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;


import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLUMN = 3;

    // Widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar progressBar;
    private Spinner directorySpinner;

    //vars
    private ArrayList<String> directories;
    private String mAppend = "file:/";

    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryImage = (ImageView) view.findViewById(R.id.fragment_gallery_gallery_image_view);
        gridView = (GridView) view.findViewById(R.id.fragment_gallery_grid_view);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_gallery_proges_bar);
        directorySpinner = (Spinner) view.findViewById(R.id.snippet_top_gallery_toolbar_spinner_directory);
        directories = new ArrayList<>();
        progressBar.setVisibility(View.GONE);
        ImageView shareClose = (ImageView) view.findViewById(R.id.snippet_top_gallery_toolbar_iv_close_share);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        TextView nextScreen = (TextView) view.findViewById(R.id.snippet_top_gallery_toolbar_tv_next);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRootTask())
                {
                    Log.d(TAG, "onClick: " + "isRoootTask = true" );
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(intent);
                }
                else
                {
                    Log.d(TAG, "onClick: " + "isRoootTask = false" );
                    Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        init();
        return view;
    }

    private boolean isRootTask()
    {
        if (((ShareActivity)getActivity()).getTask() == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void init()
    {
        FilePaths filePaths = new FilePaths();
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null)
        {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        ArrayList<String> directoriesName = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++)
        {
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index).replace("/", "");
            directoriesName.add(string);
        }
        directories.add(filePaths.CAMERA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, directoriesName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), i + " " + directories.get(i), Toast.LENGTH_SHORT).show();
                // setUp for our image grid for the directory chosen
                setUpGrid(directories.get(i));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpGrid(String selectedDirectories)
    {
        final ArrayList<String> imgUrls = FileSearch.getFilePaths(selectedDirectories);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMN;
        gridView.setColumnWidth(imageWidth);
        // Use the grid adapter to add images to GridView
        GridImageAdapter imageAdapter = new GridImageAdapter(
                getActivity(),
                R.layout.layout_grid_image_view,
                mAppend,
                imgUrls
                );
        gridView.setAdapter(imageAdapter);
         // Set the first image to be displayed when activity fragment view is infloated
        //:Todo: cannot set image without setOnItemClick Listener


//        if (imgUrls == null)
//        {
//
//        }
//        else
//        {
//            setImage(imgUrls.get(0), galleryImage, mAppend);
//            mSelectedImage = imgUrls.get(0);
//        }

        try
        {

        }catch(ArrayIndexOutOfBoundsException e)
        {
            Log.e(TAG, "setUpGrid: ArrayIndexOutOfBoundsException" + e.getMessage());
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setImage(imgUrls.get(i), galleryImage, mAppend);
                //setImageGlide(imgUrls.get(i), galleryImage, mAppend);
                mSelectedImage = imgUrls.get(i);
            }
        });
    }

    private void setImageGlide(String imgUrls, ImageView imageView, String append)
    {
        Glide.with(getActivity())
                .load(Uri.parse(append + imgUrls))
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(imageView);
    }

    private void setImage(String imgUrls, ImageView imageView, String append)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgUrls, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}
