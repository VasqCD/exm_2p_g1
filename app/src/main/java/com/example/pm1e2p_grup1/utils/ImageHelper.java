package com.example.pm1e2p_grup1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    public static File createImageFile(Context context) throws IOException {
        // Crear un nombre de archivo basado en la fecha y hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", file);
    }

    public static Bitmap loadImageFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Determinar cuánto reducir el tamaño de la imagen
        int targetW = 500;
        int targetH = 500;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decodificar el archivo de imagen en un Bitmap de tamaño reducido
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(path, options);
    }
}