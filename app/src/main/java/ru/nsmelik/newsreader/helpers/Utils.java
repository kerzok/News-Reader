package ru.nsmelik.newsreader.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by Nick.
 */
public class Utils {
    public static byte[] convertImageToByteArray(Bitmap image) {
        if (image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.toByteArray().length);
            return stream.toByteArray();
        } else {
            return null;
        }
    }

    public static Bitmap convertByteArrayToImage(byte[] blob) {
        if (blob != null) {
            return BitmapFactory.decodeByteArray(blob, 0, blob.length);
        } else {
            return null;
        }
    }
}
