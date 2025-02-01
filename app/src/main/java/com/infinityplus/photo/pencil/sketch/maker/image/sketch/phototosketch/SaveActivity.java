package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SaveActivity extends AppCompatActivity {

    private ImageView finalImageView;
  //  private Bitmap finalBitmap;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        finalImageView = findViewById(R.id.finalImageView);
        MaterialButton buttonShare = findViewById(R.id.buttonShare);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        123 // your request code
                );
            }
        }


        // 1) Get final image from the Intent
        imageUri = getIntent().getStringExtra("FINAL_IMAGE");
      //  byte[] imageBytes = getIntent().getByteArrayExtra("FINAL_IMAGE");
        if (imageUri != null) {
           // finalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Glide.with(SaveActivity.this)
                    .load(Uri.parse(imageUri))
//                    .apply(new RequestOptions()
//                            .override(200,200))
                    .into(finalImageView);
           // finalImageView.setImageBitmap(finalBitmap);
        }

        // 2) Handle Share
        buttonShare.setOnClickListener(v -> shareImage());

        // 3) Handle Save
        buttonSave.setOnClickListener(v -> saveImageFromUri(Uri.parse(imageUri)));

        ImageView back = findViewById(R.id.back_save);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(SaveActivity.this).clear(finalImageView);
                finalImageView.destroyDrawingCache();
                finish();
            }
        });
    }

    /**
     * Share the image using an ACTION_SEND intent
     */
    private void shareImage() {
        if (imageUri == null) return;

//        try {
            // Convert finalBitmap to a file in cache
//            File cachePath = new File(getCacheDir(), "images");
//            cachePath.mkdirs(); // ensure the folder exists
//            File file = new File(cachePath, "share_image.jpg");
//            FileOutputStream fos = new FileOutputStream(file);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.close();
//
//            // Get the Uri for the file using FileProvider
//            Uri contentUri = FileProvider.getUriForFile(
//                    this,
//                    getPackageName() + ".provider", // must match provider in Manifest
//                    file
//            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start share chooser
            startActivity(Intent.createChooser(shareIntent, "Share Image"));

//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
//        }
    }

//    @Override
//    protected void onDestroy() {
//        Glide.with(this).clear(finalImageView);
//        finalImageView.destroyDrawingCache();
//        super.onDestroy();
//    }

    private void saveImageFromUri(Uri sourceUri) {
        if (sourceUri == null) {
            runOnUiThread(() ->
                    Toast.makeText(SaveActivity.this, "Source Uri is null", Toast.LENGTH_SHORT).show()
            );
            return;
        }

        // Android 10 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "temp_image_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/SketchX");

            Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                     InputStream inputStream  = getContentResolver().openInputStream(sourceUri)) {

                    if (inputStream == null) {
                        throw new IOException("Failed to open input stream from sourceUri");
                    }

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    SaveActivity.this,
                                    "Image saved to gallery!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            File cacheDir = new File(getCacheDir(), "images");
                            File[] files = cacheDir.listFiles();
                            for (File f : files) {
                                // maybe if older than X time, delete
                                if(f.getName().startsWith("temp_image_")) {
                                    f.delete();
                                }
                            }

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(
                            SaveActivity.this,
                            "Error saving image: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(
                        SaveActivity.this,
                        "Failed to create media store entry.",
                        Toast.LENGTH_SHORT
                ).show());
            }

        } else {
            // Android 9 (Pie) and below: Save to external storage public directory
            String directoryPath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/SketchX";
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            try (FileOutputStream outputStream = new FileOutputStream(file);
                 InputStream inputStream = getContentResolver().openInputStream(sourceUri)) {

                if (inputStream == null) {
                    throw new IOException("Failed to open input stream from sourceUri");
                }

                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();

                runOnUiThread(() -> {
                    Toast.makeText(SaveActivity.this,
                            "Image saved to gallery!",
                            Toast.LENGTH_SHORT).show();

                    File cacheDir = new File(getCacheDir(), "images");
                    File[] files = cacheDir.listFiles();
                    for (File f : files) {
                        // maybe if older than X time, delete
                        if(f.getName().startsWith("temp_image_")) {
                            f.delete();
                        }
                    }

                });

                // Make the image visible in the gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(SaveActivity.this,
                            "Error saving image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
    }


    //    private void saveBitmap(Bitmap bitmap) {
//        if (bitmap == null) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(SaveActivity.this, "Bitmap is null", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            return;
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Android 10 and above: Use MediaStore
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
//            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SketchX");
//
//            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//            if (uri != null) {
//                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SaveActivity.this, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(SaveActivity.this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                }
//            } else {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(SaveActivity.this, "Failed to create media store entry.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//        } else {
//            // Android 9 and below: Use File system
//            String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SketchX";
//            File directory = new File(directoryPath);
//            if (!directory.exists()) {
//                directory.mkdirs(); // Create the directory if it does not exist
//            }
//
//            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
//            File file = new File(directory, fileName);
//
//            try (FileOutputStream outputStream = new FileOutputStream(file)) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(SaveActivity.this, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//
//                // Make the image visible in the gallery
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(file);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//            } catch (IOException e) {
//                e.printStackTrace();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(SaveActivity.this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//        }
//    }
}

