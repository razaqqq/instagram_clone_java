package com.example.instagram_clone_2017.Share;

import static android.app.Activity.RESULT_OK;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.instagram_clone_2017.Profile.AccountSettingActivity;
import com.example.instagram_clone_2017.R;
import com.example.instagram_clone_2017.Utils.Permissions;

import java.io.File;
import java.io.IOException;
import java.nio.file.spi.FileSystemProvider;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final int PHOTO_FRAGMENT_NUMBER = 1;
    private static final int GALLERY_FRAGMENT_NUMBER = 0;
    private static final int CAMERA_REQUEST_CODE = 5;

    private String currentPhotoPath;

    private Uri image_uri;

    private ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Button btnlaunchCamera = (Button) view.findViewById(R.id.fragment_photo_btn_launch_camera);
        imageView = view.findViewById(R.id.imageBitmap);
        btnlaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUMBER)
                {
                    if (((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSIONS[0]))
                    {
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

//                        File photoFile = null;
//                        try {
//                            photoFile = createImageFile();
//                        } catch (IOException ex)
//                        {
//                            Toast.makeText(getActivity(), "Exception While Creating FIle " + ex.toString(), Toast.LENGTH_SHORT).show();
//                        }
//
//                        if (photoFile != null)
//                        {
//                            Uri photoUri =
//                            Intent takePictureIntent = new Intent();
//                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
//                        }

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "new Image");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
                        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);


                    }
                    else
                    {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
        return view;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,
                ".jpg"
        );

        return  image;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Toast.makeText(getActivity(), "onActivityResulyt is called", Toast.LENGTH_SHORT).show();
            // Navigate to the final share screen to publish potho

//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image_uri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }



            if (isRootTask())
                {
                    Toast.makeText(getActivity(), "Is A Root Task", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_uri), image_uri);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getActivity(), "Is Not Root TAsk", Toast.LENGTH_SHORT).show();
                    try {
                        Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                        intent.putExtra(getString(R.string.selected_uri), image_uri);
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        getActivity().finish();
                    }catch (NullPointerException e)
                    {
                        Log.d(TAG, "onActivityResult = NullPointerException" + e.getMessage());
                    }
                }

            }
            else
            {
                Toast.makeText(getActivity(), "Data Is Null", Toast.LENGTH_SHORT).show();
            }






        }


}

