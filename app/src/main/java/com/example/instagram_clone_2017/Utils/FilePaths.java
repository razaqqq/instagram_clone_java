package com.example.instagram_clone_2017.Utils;

import android.os.Environment;

public class FilePaths {
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String CAMERA = ROOT_DIR + "/DCIM/Camera";
    public String PICTURES = ROOT_DIR + "/Pictures";
    public String DOWNLOADS = ROOT_DIR + "/Downloads";

    public String FIREBASE_IMAGE_STORAGE = "instagram_clone/photos/users";

}
