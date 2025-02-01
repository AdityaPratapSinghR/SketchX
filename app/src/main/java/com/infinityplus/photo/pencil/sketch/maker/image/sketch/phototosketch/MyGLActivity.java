package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MyGLActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private SketchRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Create the GLSurfaceView
        glSurfaceView = new GLSurfaceView(this);
        // We want OpenGL ES 2.0
        glSurfaceView.setEGLContextClientVersion(2);

        // 2) Load a sample image from resources
        Bitmap sampleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lady);

        // 3) Create our custom renderer, passing the bitmap
        glSurfaceView = new GLSurfaceView(this);
        // We want ES 2.0
        glSurfaceView.setEGLContextClientVersion(2);

        // Load the user’s main image (someimage.jpg in res/drawable)
        Bitmap mainBmp = BitmapFactory.decodeResource(getResources(), R.drawable.lady);
        // Load the hatch pattern (hatch_texture.png in res/drawable)
        Bitmap hatchBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cross_overlay);

        // Create our custom renderer
        renderer = new SketchRenderer(mainBmp, hatchBmp);
        glSurfaceView.setRenderer(renderer);

        // optional: glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}
