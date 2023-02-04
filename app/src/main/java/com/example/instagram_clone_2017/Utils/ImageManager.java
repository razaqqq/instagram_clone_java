package com.example.instagram_clone_2017.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {
    private static final String TAG = "ImageManager";
    public static Bitmap getBitMap(String imageUrl)
    {
        File imageFile = new File(imageUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "getBitMAp: fileNotFoundExceptions = " + e);
        }
        finally {
            try {
                fis.close();
            }
            catch (IOException e)
            {
                Log.d(TAG, "getBitMAp: IOExceptions = " + e);
            }
        }
        return bitmap;
    }

    public static byte[] getByteFromBitmap(Bitmap bm, int quality)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

}
