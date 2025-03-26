package com.example.pm1e2p_grup1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    public static File createImageFile(Context context) throws IOException {
        // Crear un nombre de archivo basado en la fecha y hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Usar getFilesDir() en lugar de getExternalFilesDir()
        // Esto guardará las imágenes en el almacenamiento interno de la app
        File storageDir = new File(context.getFilesDir(), "Pictures");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getUriForFile(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            // Intenta una ruta alternativa para el archivo
            try {
                // Log para depuración
                Log.d("ImageHelper", "Ruta original falló, intentando alternativa");

                // Intenta usar un directorio diferente
                File newFile = new File(context.getFilesDir(), "temp_" + file.getName());
                if (!file.equals(newFile)) {
                    if (file.exists()) {
                        // Copia el archivo usando FileInputStream/FileOutputStream (compatible con API 24)
                        copyFile(file, newFile);
                        return FileProvider.getUriForFile(context,
                                context.getPackageName() + ".fileprovider", newFile);
                    }
                }
            } catch (Exception copyEx) {
                Log.e("ImageHelper", "Error en ruta alternativa", copyEx);
            }

            return null;
        }
    }

    // Método de copia de archivos compatible con API 24
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignorar
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignorar
                }
            }
        }
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