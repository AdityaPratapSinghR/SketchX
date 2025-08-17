package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1001;
    private static final int PICK_IMAGE_GALLERY = 1002;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialButton camButton = findViewById(R.id.camera);
        MaterialButton galleryButton = findViewById(R.id.gallery);
//        Intent intent2 = new Intent(MainActivity.this,MyGLActivity.class);
//        startActivity(intent2);

        // Camera button
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestStoragePermission();
        }
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        //String imagePath = getPathFromUri(uri);
                        Intent intent = new Intent(MainActivity.this,ImageEditorActivity.class);
                        intent.putExtra(Constant.GALLERY,String.valueOf(uri));
                       // intent.putExtra("imageType", Constant.GALLERY);
                        startActivity(intent);
                    } else {
                        //Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        // Gallery button
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Launch the photo picker and let the user choose only images.
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }else {
                    openGallery();
                }
            }
        });
        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    /**
     * Launch the system gallery (image picker) to select an image.
     */
    private void openGallery() {
        // ACTION_PICK is a simple approach to open the system Gallery or other registered apps
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, PICK_IMAGE_GALLERY);
    }

    /**
     * Handle the result from the camera or gallery.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is OK (user didn't cancel)
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    // If using ACTION_IMAGE_CAPTURE without extra output, a thumbnail is in data.getExtras()
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        if (imageBitmap != null) {
                            // Convert the bitmap to byte array for sending to the editor
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();

                            // Send to ImageEditorActivity
                            Intent editorIntent = new Intent(MainActivity.this, ImageEditorActivity.class);
                            editorIntent.putExtra(Constant.CAMERA, imageBytes);
                            startActivity(editorIntent);
                        } else {
                            Toast.makeText(this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                        }
                        // Do something with the bitmap, e.g. display it or pass it to another Activity
                        Toast.makeText(this, "Camera image captured!", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case PICK_IMAGE_GALLERY:
                    // The user picked an image from the Gallery
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        // Do something with the Uri (e.g. display in an ImageView)
                        if (selectedImageUri != null) {
                            // Send Uri to ImageEditorActivity
                            Intent editorIntent = new Intent(MainActivity.this, ImageEditorActivity.class);
                            editorIntent.putExtra(Constant.GALLERY, selectedImageUri.toString());
                            startActivity(editorIntent);
                        } else {
                            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(this, "Gallery image selected!", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }
}
