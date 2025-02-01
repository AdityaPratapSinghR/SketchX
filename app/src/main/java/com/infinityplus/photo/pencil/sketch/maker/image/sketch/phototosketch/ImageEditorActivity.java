package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;
import com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.adapter.ConversionAdapter;
import com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.model.ConversionItem;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageEditorActivity extends AppCompatActivity implements
        ConversionAdapter.OnConversionClickListener {

    private ImageView imagePreview;
    private RecyclerView conversionsRecycler, filtersRecycler;

    // Keep an original bitmap in memory to reset or re-apply filters
    private Bitmap originalBitmap;
    // Keep a “working” bitmap that we display on screen
    private Bitmap workingBitmap;
    // This will store the current ConversionItem that the user clicked
    private ConversionItem currentConversionItem;
    Slider conversionSlider;
    SharedPreferences sharedApp;
    SharedPreferences.Editor editor;
    byte[] imageData;
    boolean doneImage = false;
    int sliderValue = 100;
    Bitmap scaledBitmap;

    //private List<ModelConfig> modelConfigs;
    ConversionAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        sharedApp = getSharedPreferences(Constant.APP_NAME, MODE_PRIVATE);
        editor = sharedApp.edit();
        //System.loadLibrary("tensorflow_inference");

       // loadModelConfigs();

        imagePreview = findViewById(R.id.imagePreview);
        // Check if we have a ByteArray (camera) or a Uri (gallery)
        //  Toast.makeText(this, getIntent().getStringExtra(Constant.GALLERY), Toast.LENGTH_SHORT).show();
        if (getIntent().hasExtra(Constant.CAMERA)) {
            // Camera thumbnail
            byte[] imageBytes = getIntent().getByteArrayExtra(Constant.CAMERA);
            if (imageBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                // Set this as the original (for filter processing)
                originalBitmap = rotateBitmap(bmp, 90);
                ;
                // Create a working copy
                workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                // Display the working bitmap
                // workingBitmap =
                imagePreview.setImageBitmap(workingBitmap);
                Toast.makeText(this, "Loaded camera image!", Toast.LENGTH_SHORT).show();
            }
        } else if (getIntent().hasExtra(Constant.GALLERY)) {
            // Gallery image
            String uriString = getIntent().getStringExtra(Constant.GALLERY);
            if (uriString != null) {
                try {
                    imageData = uriToBytes(this, Uri.parse(uriString));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Uri imageUri = Uri.parse(uriString);
                // Use your custom method to load the Uri into a Bitmap
                Bitmap bmp = loadImageFromUri(imageUri);
                if (bmp != null) {
                    originalBitmap = bmp;
                    workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    // 1) Downsample or get the original bitmap
                    scaledBitmap = decodeSampledBitmap(imageData, 900, 600);
                    // workingBitmap = scaledBitmap;
                    imagePreview.setImageBitmap(workingBitmap);

                    Toast.makeText(this, "Loaded gallery image!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to load gallery image!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No image received!", Toast.LENGTH_SHORT).show();
        }

        conversionsRecycler = findViewById(R.id.conversionsRecycler);
        filtersRecycler = findViewById(R.id.filtersRecycler);

        ImageView back = findViewById(R.id.back_preview);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

//        // Load a sample image from drawable
//        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lady);
//        workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        imagePreview.setImageBitmap(workingBitmap);

        setupConversionsRecycler();
        //setupFiltersRecycler();


        conversionSlider = findViewById(R.id.conversionSeekBar);
        conversionSlider.setValue(100f);

//         conversionSlider.addOnChangeListener((slider, v, b) -> {
//             if (currentConversionItem != null && b) {
//                 applyConversion(currentConversionItem, (int) v);
//             }
//         });
        conversionSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // Called when the user first touches the slider (finger down).
                // You can do nothing here if you only want to apply the effect on release.
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Called when the user lifts their finger (finger up).
                // Now we apply the conversion:
                if (currentConversionItem != null) {
                    int value = (int) slider.getValue();
                    sliderValue = value;

                        // Use your normal CPU pipeline
                        applyConversion(currentConversionItem, value);
                   // applyMLConversion(currentConversionItem.getType());
                   /// applyConversion(currentConversionItem, value);
                }
            }
        });

        conversionSlider.setVisibility(View.GONE);

        ImageView done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doneImage = true;

                ProgressDialog progressDialog = new ProgressDialog(ImageEditorActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Completing Final Result");
                progressDialog.show();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {

//                    if (currentConversionItem.getType() >= SketchImage.ML_SKETCH_BASE+1
//                            && currentConversionItem.getType() <= SketchImage.ML_SKETCH_BASE+11) {
//                        // Use ML approach
//                       // applyMLConversion(currentConversionItem.getType(),position);
//                    } else {
                        // Use your normal CPU pipeline
                      //  applyConversion(currentConversionItem, sliderValue);
                        // Recreate the SketchImage each time, or keep it as a field if you want
                        SketchImage sketchImage = new SketchImage.Builder(ImageEditorActivity.this, originalBitmap).build();
                        // Generate the new bitmap
                        Bitmap converted = sketchImage.getImageAs(currentConversionItem.getType(), sliderValue);
                        workingBitmap = converted;
                  //  }

                });
                if (workingBitmap != null) {

                    try {
                        // Convert finalBitmap to a file in cache
                        File cachePath = new File(getCacheDir(), "images");
                        cachePath.mkdirs(); // ensure the folder exists
                        File file = new File(cachePath, "temp_image_"+ System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(file);
                        workingBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                        // Get the Uri for the file using FileProvider
                        Uri  contentUri = FileProvider.getUriForFile(
                                ImageEditorActivity.this,
                                getPackageName() + ".provider", // must match provider in Manifest
                                file
                        );
                       // Toast.makeText(ImageEditorActivity.this, contentUri.toString(), Toast.LENGTH_SHORT).show();

                        progressDialog.hide();

                        Intent intent = new Intent(ImageEditorActivity.this, SaveActivity.class);
                        intent.putExtra("FINAL_IMAGE", contentUri.toString());
                        startActivity(intent);

                    }catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ImageEditorActivity.this, "Error sharing image", Toast.LENGTH_SHORT).show();
                    }
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        workingBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
//                        byte[] imageBytes = baos.toByteArray();

                    // Start SaveActivity and pass the final image

                }

            }
        });

        ImageView more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ImageEditorActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottom_sheet);
                bottomSheetDialog.setCancelable(true);
                Switch originalEdit = bottomSheetDialog.findViewById(R.id.originalSwitch);
                originalEdit.setChecked(sharedApp.getBoolean(Constant.ORIGINAL, true));
                originalEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        editor.putBoolean(Constant.ORIGINAL, b);
                        editor.apply();
                        originalEdit.setChecked(b);
                    }
                });
                bottomSheetDialog.show();
            }
        });

        ImageView undo = findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePreview.setImageBitmap(originalBitmap);
            }
        });


    }

    private Bitmap loadImageFromUri(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupConversionsRecycler() {
        conversionsRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<ConversionItem> conversions = new ArrayList<>();

        // Define conversion types
    //    for (int i = 0; i < modelConfigs.size(); i++) {
            //conversions.add(new ConversionItem(SketchImage.ML_SKETCH_1, modelConfigs.get(0).displayName, R.drawable.lady));
       // }

//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_1, modelConfigs.get(0).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_2, modelConfigs.get(1).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_3, modelConfigs.get(2).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_4, modelConfigs.get(3).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_5, modelConfigs.get(4).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_6, modelConfigs.get(5).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_7, modelConfigs.get(6).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_8, modelConfigs.get(7).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_9, modelConfigs.get(8).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_10, modelConfigs.get(9).displayName, R.drawable.lady));
//        conversions.add(new ConversionItem(SketchImage.ML_SKETCH_11, modelConfigs.get(10).displayName, R.drawable.lady));






        conversions.add(new ConversionItem(SketchImage.ORIGINAL_TO_GRAY, "Gray", R.drawable.img_1));
        conversions.add(new ConversionItem(SketchImage.ORIGINAL_TO_SKETCH, "Sketch", R.drawable.img_2));
        conversions.add(new ConversionItem(SketchImage.ORIGINAL_TO_COLORED_SKETCH, "Color Sketch", R.drawable.img_3));
        conversions.add(new ConversionItem(SketchImage.POSTERIZE_SKETCH, "Posterize Sketch", R.drawable.img_4));
        conversions.add(new ConversionItem(SketchImage.ORIGINAL_TO_SOFT_SKETCH, "Soft Sketch", R.drawable.img_5));
        conversions.add(new ConversionItem(SketchImage.STYLE_TECHNICAL_DRAWING, "Technical Drawing", R.drawable.img_23));
        conversions.add(new ConversionItem(SketchImage.ORIGINAL_TO_SOFT_COLOR_SKETCH, "Soft Color", R.drawable.img_7));
        conversions.add(new ConversionItem(SketchImage.GRAY_TO_SKETCH, "No Color Sketch", R.drawable.img_6));
        conversions.add(new ConversionItem(SketchImage.GRAY_TO_COLORED_SKETCH, "No Color Sketch", R.drawable.img_9));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_1, "Custom 1", R.drawable.img_30));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_2, "Custom 2", R.drawable.img_31));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_3, "Custom 3", R.drawable.img_32));
        conversions.add(new ConversionItem(SketchImage.GRAY_TO_SOFT_COLOR_SKETCH, "No Color Sketch", R.drawable.img_8));
        conversions.add(new ConversionItem(SketchImage.SKETCH_TO_COLOR_SKETCH, "No Color Sketch", R.drawable.img_11));
       // conversions.add(new ConversionItem(SketchImage.NEON_OUTLINE, "Neon Outline", R.drawable.img_10));
        conversions.add(new ConversionItem(SketchImage.NEGATIVE_COLOR_SKETCH, "Negative Color Sketch", R.drawable.img_12));

        conversions.add(new ConversionItem(SketchImage.HALFTONE_SKETCH, "Halftone Sketch", R.drawable.img_14));
        conversions.add(new ConversionItem(SketchImage.SKETCH_CRISP_INK, "Crisp Ink", R.drawable.img_13));
        conversions.add(new ConversionItem(SketchImage.SKETCH_POSTERIZED, "Posterized Sketch", R.drawable.img_16));
        conversions.add(new ConversionItem(SketchImage.POSTERIZE_GRAY, "Posterized Gray", R.drawable.img_17));
        conversions.add(new ConversionItem(SketchImage.SKETCH_LINE_ART, "Line Art", R.drawable.img_15));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_15, "Custom 15", R.drawable.img_44));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_16, "Custom 16", R.drawable.img_45));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_17, "Custom 17", R.drawable.img_46));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_18, "Custom 18", R.drawable.img_47));
        conversions.add(new ConversionItem(SketchImage.CARTOONIFY, "Cartoonify", R.drawable.img_19));
        conversions.add(new ConversionItem(SketchImage.CARTOONIFY_GRAY, "Cartoonify Gray", R.drawable.img_18));
        conversions.add(new ConversionItem(SketchImage.WATERCOLOR, "Watercolor Sketch", R.drawable.img_21));

        conversions.add(new ConversionItem(SketchImage.SKETCH_CHARCOAL_SMUDGE, "Charcoal Smudge", R.drawable.img_20));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_4, "Custom 4", R.drawable.img_33));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_5, "Custom 5", R.drawable.img_34));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_6, "Custom 6", R.drawable.img_35));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_7, "Custom 7", R.drawable.img_36));

        conversions.add(new ConversionItem(SketchImage.SKETCH_HATCHED_DETAIL, "Hatched Detail", R.drawable.img_22));
        conversions.add(new ConversionItem(SketchImage.SKETCH_SHADED_PENCIL, "Shaded Pencil", R.drawable.img_24));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_11, "Custom 11", R.drawable.img_40));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_12, "Custom 12", R.drawable.img_41));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_13, "Custom 13", R.drawable.img_42));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_14, "Custom 14", R.drawable.img_43));
        conversions.add(new ConversionItem(SketchImage.STYLE_GRAPHITE_SHADE, "Graphite Shade", R.drawable.img_25));

        conversions.add(new ConversionItem(SketchImage.STYLE_VINTAGE_PENCIL, "Vintage Pencil", R.drawable.img_26));



        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_8, "Custom 8", R.drawable.img_37));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_9, "Custom 9", R.drawable.img_38));
        conversions.add(new ConversionItem(SketchImage.CUSTOM_SKETCH_10, "Custom 10", R.drawable.img_39));







         adapter = new ConversionAdapter(conversions, this);
        conversionsRecycler.setAdapter(adapter);

    }


    @Override
    public void onConversionSelected(ConversionItem item, int position) {
        // Store the current conversion
        currentConversionItem = item;
        conversionSlider.setVisibility(View.VISIBLE);
        // Apply conversion wi  th the current SeekBar value
        int currentValue = (int) conversionSlider.getValue();

//        // Check if it's an ML-based type or a normal type
//        if (item.getType() >= SketchImage.ML_SKETCH_BASE+1
//                && item.getType() <= SketchImage.ML_SKETCH_BASE+11) {
//            // Use ML approach
//            applyMLConversion(item.getType(),position);
//            conversionSlider.setVisibility(View.GONE);
//        } else {
            // Use your normal CPU pipeline
            applyConversion(item, currentValue);
      //  }


//        applyConversion(item, currentValue);
    }

    /**
     * Utility to apply the chosen conversion with the specified intensity
     */
    private void applyConversion(ConversionItem item, int value) {
        if (originalBitmap == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(ImageEditorActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Applying!");
        progressDialog.show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {

            if (currentConversionItem.getType() >= SketchImage.ML_SKETCH_BASE+1
                    && currentConversionItem.getType() <= SketchImage.ML_SKETCH_BASE+11) {
                // Use ML approach
               // applyMLConversion(currentConversionItem.getType(),position);
                SketchImage sketchImage = new SketchImage.Builder(this, workingBitmap).build();
                // Generate the new bitmap
                Bitmap converted = sketchImage.getImageAs(SketchImage.ORIGINAL_TO_SKETCH, value);
                workingBitmap = converted;
            } else {
                // Recreate the SketchImage each time, or keep it as a field if you want
                SketchImage sketchImage = new SketchImage.Builder(this, scaledBitmap).build();
                // Generate the new bitmap
                Bitmap converted = sketchImage.getImageAs(item.getType(), value);
                workingBitmap = converted;
            }



            // 3) Post result back to the main thread
            runOnUiThread(() -> {
                progressDialog.hide();
                imagePreview.setImageBitmap(workingBitmap);
            });
        });


        //  Bitmap scaledBitmap = decodeSampledBitmap(data, 1200, 900);
        // Recreate the SketchImage each time, or keep it as a field if you want
//            SketchImage sketchImage = new SketchImage.Builder(this, originalBitmap).build();
//            // Generate the new bitmap
//            Bitmap converted = sketchImage.getImageAs(item.getType(), value);
//
//            // Update the working bitmap
//            workingBitmap = converted;
//            imagePreview.setImageBitmap(workingBitmap);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true
        );
    }

    /**
     * Decode and downsample an image from a Uri (or file path).
     */
    public static Bitmap decodeSampledBitmap(byte[] data, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize power of 2 that keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static byte[] uriToBytes(Context context, Uri uri) throws IOException {
        if (uri == null) return null;

        // Read the data from the Uri into a byte array
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if (inputStream == null) {
            return null;
        }

        byte[] data = new byte[1024];
        int len;
        while ((len = inputStream.read(data)) != -1) {
            buffer.write(data, 0, len);
        }
        inputStream.close();
        buffer.flush();
        return buffer.toByteArray();
    }


//    private boolean isSquareImage(Bitmap bitmap) {
//        return Math.abs(bitmap.getWidth() - bitmap.getHeight()) < 50;
//    }
//
//    private String getModelFileName(int effectType, boolean isSquare) {
//        ModelConfig config = modelConfigs.get(effectType);
//        String shape = isSquare ? "square" : "rect";
//        return "file:///android_asset/" + config.name + "-v" + config.version + "-" + shape + ".pb";
//    }

//    private void applyMLConversion(int effectType, int currentIndex) {
//        if (originalBitmap == null) return;
//
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Applying Effect");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        new Thread(() -> {
//            try {
//                Log.d("ApplyEffectDebug", "Selected Effect Type: " + effectType);
//               // String modelFile = getModelFileName(effectType, isSquareImage(originalBitmap));
//               // Bitmap resultBitmap = runMLInference(originalBitmap,false,true, modelFile);
//              //  Bitmap resultBitmap = runMLInference(originalBitmap, modelConfigs, sliderValue);
//                // Suppose user chooses a model from your modelConfigs List
//                ModelConfig chosen = modelConfigs.get(currentIndex);
//
//// Run inference
//                Bitmap resultBitmap = runMLInference(originalBitmap, chosen);
//                workingBitmap = resultBitmap;
//                runOnUiThread(() -> {
//                    progressDialog.dismiss();
//                    imagePreview.setImageBitmap(workingBitmap);
//                });
//            } catch (Exception e) {
//                Log.e("ApplyEffectError", "Error during effect application: " + e.getMessage());
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    progressDialog.dismiss();
//                    Toast.makeText(this, "Error during effect application.", Toast.LENGTH_SHORT).show();
//                });
//            }
//        }).start();
//    }



//    private Bitmap runMLInference(Bitmap original,
//                                  boolean isColorModel,
//                                  boolean isRectModel,
//                                  String pbFile) {
//        // 1) Initialize the inference interface from assets & the chosen .pb
//        TensorFlowInferenceInterface inference =
//                new TensorFlowInferenceInterface(getAssets(), pbFile);
//
//        // 2) Decide input dimensions.
//        //    For example, competitor: 960x540 (rect) or 960x960 (square).
//        int inputW, inputH;
//        if (isRectModel) {
//            inputW = 960;
//            inputH = 540;
//        } else {
//            inputW = 960;
//            inputH = 960;
//        }
//
//        // 3) Decide the number of channels
//        int channels = isColorModel ? 3 : 1;
//
//        // 4) Scale the original to match the model’s expected width/height
//        //    (We’re ignoring rotations/portrait logic here; adapt if needed.)
//        Bitmap scaled = Bitmap.createScaledBitmap(original, inputW, inputH, false);
//
//        // 5) Prepare the input float[] in [0..255], shape [1, inputH, inputW, channels]
//        float[] inputFloats = new float[inputW * inputH * channels];
//        int index = 0;
//        for (int y = 0; y < inputH; y++) {
//            for (int x = 0; x < inputW; x++) {
//                int pixel = scaled.getPixel(x, y);
//                int R = (pixel >> 16) & 0xFF;
//                int G = (pixel >>  8) & 0xFF;
//                int B =  pixel       & 0xFF;
//
//                if (isColorModel) {
//                    // 3 channels: feed raw R, G, B (0..255)
//                    inputFloats[index++] = R;
//                    inputFloats[index++] = G;
//                    inputFloats[index++] = B;
//                } else {
//                    // 1 channel: grayscale
//                    float gray = 0.299f * R + 0.587f * G + 0.114f * B;
//                    inputFloats[index++] = gray;
//                }
//            }
//        }
//
//        // 6) Feed the input to "deepsketch/input"
//        inference.feed("deepsketch/input", inputFloats,
//                1, inputH, inputW, channels);
//
//        // 7) Run the session with output = "deepsketch/output"
//        String[] outputNames = {"deepsketch/output"};
//        inference.run(outputNames, false);
//
//        // 8) Fetch the output from "deepsketch/output"
//        float[] outputFloats = new float[inputW * inputH * channels];
//        inference.fetch("deepsketch/output", outputFloats);
//
//        // 9) Convert output back to an ARGB Bitmap
//        int[] outPixels = new int[inputW * inputH];
//        index = 0;
//        if (isColorModel) {
//            // Color model => 3 channels per pixel
//            for (int i = 0; i < outPixels.length; i++) {
//                int rr = (int) outputFloats[index++];
//                int gg = (int) outputFloats[index++];
//                int bb = (int) outputFloats[index++];
//                // clamp if needed
//                if (rr < 0) rr = 0; if (rr > 255) rr = 255;
//                if (gg < 0) gg = 0; if (gg > 255) gg = 255;
//                if (bb < 0) bb = 0; if (bb > 255) bb = 255;
//                outPixels[i] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
//            }
//        } else {
//            // Gray model => 1 channel per pixel
//            for (int i = 0; i < outPixels.length; i++) {
//                int gray = (int) outputFloats[i];
//                if (gray < 0) gray = 0;
//                if (gray > 255) gray = 255;
//                outPixels[i] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
//            }
//        }
//
//        Bitmap outBmp = Bitmap.createBitmap(inputW, inputH, Bitmap.Config.ARGB_8888);
//        outBmp.setPixels(outPixels, 0, inputW, 0, 0, inputW, inputH);
//
//        // 10) Optionally re-rotate or scale back up to the original size/orientation.
//        //     For simplicity, we just return outBmp as is:
//        return outBmp;
//    }

//    private Bitmap runMLInference(
//            Bitmap original,
//            ModelConfig config // which has .name, .version, .colorType
//    ) {
//        // -------------------------------------------------------
//        // 1) Construct the .pb filename
//        //    e.g. "best0-v1-rect.pb" or "best0-v1-square.pb"
//        // -------------------------------------------------------
//        String baseName = config.name + "-v" + config.version;
//
//        // Decide if we use rect or square
//        // (example: if original is wider than tall => rect, else square)
//        boolean useRect = (original.getWidth() > original.getHeight());
//        String shapeSuffix = useRect ? "rect" : "square";
//
//        // e.g. "file:///android_asset/best0-v1-rect.pb"
//        String pbFile = "file:///android_asset/" + baseName + "-" + shapeSuffix + ".pb";
//
//        // -------------------------------------------------------
//        // 2) Initialize TFInferenceInterface
//        // -------------------------------------------------------
//        TensorFlowInferenceInterface inference =
//                new TensorFlowInferenceInterface(getAssets(), pbFile);
//
//        // -------------------------------------------------------
//        // 3) Decide model input dimension
//        //    Competitor approach: rect => 960x540, square => 960x960
//        // -------------------------------------------------------
//        int inputW, inputH;
//       // useRect = false;
//        if (useRect) {
//            inputW = 960;
//            inputH = 540;
//        } else {
//            inputW = 960;
//            inputH = 960;
//        }
//
//        // -------------------------------------------------------
//        // 4) Check color vs. gray
//        // -------------------------------------------------------
//        boolean isColorModel = config.colorType.equalsIgnoreCase("color");
//        int channels = isColorModel ? 3 : 1;
//
//        // -------------------------------------------------------
//        // 5) Scale input to match the model’s shape
//        // -------------------------------------------------------
//        Bitmap scaled = Bitmap.createScaledBitmap(original, inputW, inputH, false);
//
//        // -------------------------------------------------------
//        // 6) Prepare input array in [0..255]
//        // -------------------------------------------------------
//        float[] inputFloats = new float[inputW * inputH * channels];
//        int index = 0;
//        for (int y = 0; y < inputH; y++) {
//            for (int x = 0; x < inputW; x++) {
//                int pixel = scaled.getPixel(x, y);
//
//                int R = (pixel >> 16) & 0xFF;
//                int G = (pixel >> 8) & 0xFF;
//                int B =  pixel       & 0xFF;
//
//                if (isColorModel) {
//                    // 3 channels
//                    inputFloats[index++] = R;
//                    inputFloats[index++] = G;
//                    inputFloats[index++] = B;
//                } else {
//                    // 1 channel => Gray
//                    float gray = (0.299f * R + 0.587f * G + 0.114f * B);
//                    inputFloats[index++] = gray;
//                }
//            }
//        }
//
//        // -------------------------------------------------------
//        // 7) Feed to "deepsketch/input"
//        // -------------------------------------------------------
//        inference.feed("deepsketch/input", inputFloats,
//                1, inputH, inputW, channels);
//
//        // -------------------------------------------------------
//        // 8) Run the session => "deepsketch/output"
//        // -------------------------------------------------------
//        inference.run(new String[] { "deepsketch/output" }, false);
//
//        // -------------------------------------------------------
//        // 9) Fetch the output
//        // -------------------------------------------------------
//        float[] outputFloats = new float[inputW * inputH * channels];
//        inference.fetch("deepsketch/output", outputFloats);
//
//        // -------------------------------------------------------
//        // 10) Convert output => ARGB
//        // -------------------------------------------------------
//        int[] outPixels = new int[inputW * inputH];
//        index = 0;
//        if (isColorModel) {
//            for (int i = 0; i < outPixels.length; i++) {
//                int rr = (int) outputFloats[index++];
//                int gg = (int) outputFloats[index++];
//                int bb = (int) outputFloats[index++];
//                if (rr < 0) rr = 0; if (rr > 255) rr = 255;
//                if (gg < 0) gg = 0; if (gg > 255) gg = 255;
//                if (bb < 0) bb = 0; if (bb > 255) bb = 255;
//                outPixels[i] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
//            }
//        } else {
//            // grayscale => single channel
//            for (int i = 0; i < outPixels.length; i++) {
//                int gray = (int) outputFloats[i];
//                if (gray < 0) gray = 0;
//                if (gray > 255) gray = 255;
//                outPixels[i] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
//            }
//        }
//
//        Bitmap outBmp = Bitmap.createBitmap(inputW, inputH, Bitmap.Config.ARGB_8888);
//        outBmp.setPixels(outPixels, 0, inputW, 0, 0, inputW, inputH);
//
//        // -------------------------------------------------------
//        // 11) Scale the result back to original size
//        // -------------------------------------------------------
//        Bitmap finalBmp = Bitmap.createScaledBitmap(
//                outBmp, original.getWidth(), original.getHeight(), true);
//
//        // Return the final image
//        return finalBmp;
//    }



//    private void loadModelConfigs() {
//        try {
//            InputStream is = getAssets().open("filters.json");
//            byte[] buffer = new byte[is.available()];
//            is.read(buffer);
//            is.close();
//
//            String json = new String(buffer, "UTF-8");
//            JSONArray jsonArray = new JSONArray(json);
//
//            modelConfigs = new ArrayList<>();
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject obj = jsonArray.getJSONObject(i);
//                String name = obj.getString("name");
//                String version = obj.getString("version");
//                String displayName = obj.getString("display");
//                String colorType = obj.has("color") ? obj.getString("color") : "color";
//
//                modelConfigs.add(new ModelConfig(name, version, displayName, colorType));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to load model configurations.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private static class ModelConfig {
//        String name;
//        String version;
//        String displayName;
//        String colorType; // either "color" or "gray"
//
//        ModelConfig(String name, String version, String displayName, String colorType) {
//            this.name = name;
//            this.version = version;
//            this.displayName = displayName;
//            this.colorType = colorType;
//        }
//    }


}

