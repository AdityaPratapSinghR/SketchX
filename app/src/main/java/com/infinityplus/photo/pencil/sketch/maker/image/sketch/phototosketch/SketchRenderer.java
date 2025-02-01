package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Draws the main image in a "Sobel sketch" style,
 * plus hatch pattern in darker areas.
 */
public class SketchRenderer implements GLSurfaceView.Renderer {

    private final Bitmap mainBitmap;
    private final Bitmap hatchBitmap;

    private int programId;

    // We'll have 2 texture IDs: main image, hatch pattern
    private int mainTextureId;
    private int hatchTextureId;

    // Dimensions
    private int imgWidth, imgHeight;

    // Vertex buffer for quad
    private FloatBuffer vertexBuffer;
    private float[] mvpMatrix = new float[16];

    // locations
    private int aPositionLoc;
    private int aTexCoordLoc;
    private int uMVPMatrixLoc;
    private int uMainTexLoc;
    private int uHatchTexLoc;
    private int uTexSizeLoc;

    // We'll do a "fit" approach in onSurfaceChanged
    public SketchRenderer(Bitmap main, Bitmap hatch) {
        this.mainBitmap = main;
        this.hatchBitmap= hatch;
        imgWidth = main.getWidth();
        imgHeight= main.getHeight();
    }

    // Vertex shader: we pass MVP, plus flip Y
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTexCoord;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "void main(){\n" +
                    "   gl_Position = uMVPMatrix * aPosition;\n" +
                    // flip Y so it's not upside-down
                    "   vTexCoord   = vec2(aTexCoord.x, 1.0 - aTexCoord.y);\n" +
                    "}\n";

    // Fragment: we do sobel edge detection + invert => "sketch lines".
    // Then we measure brightness of the original color => if it's dark => blend in hatch texture.
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "uniform sampler2D uMainTexture;\n" +
                    "uniform sampler2D uHatchTexture;\n" +
                    "uniform vec2 uTexSize;\n" +  // for sobel offsets
                    "void main(){\n" +
                    "  // sample original color for brightness measure\n" +
                    "  vec4 orgColor = texture2D(uMainTexture, vTexCoord);\n" +
                    "  float brightness = dot(orgColor.rgb, vec3(0.299, 0.587, 0.114));\n" +

                    "  // do sobel (like we did before)\n" +
                    "  vec2 onePixel = 1.0 / uTexSize;\n" +
                    "  float sobelX[9];\n" +
                    "  sobelX[0] = -1.0; sobelX[1] =  0.0; sobelX[2] = 1.0;\n" +
                    "  sobelX[3] = -2.0; sobelX[4] =  0.0; sobelX[5] = 2.0;\n" +
                    "  sobelX[6] = -1.0; sobelX[7] =  0.0; sobelX[8] = 1.0;\n" +

                    "  float sobelY[9];\n" +
                    "  sobelY[0] = -1.0; sobelY[1] = -2.0; sobelY[2] = -1.0;\n" +
                    "  sobelY[3] =  0.0; sobelY[4] =  0.0; sobelY[5] =  0.0;\n" +
                    "  sobelY[6] =  1.0; sobelY[7] =  2.0; sobelY[8] =  1.0;\n" +

                    "  vec3 sample[9];\n" +
                    "  int idx=0;\n" +
                    "  for(int row=-1; row<=1; row++){\n" +
                    "    for(int col=-1; col<=1; col++){\n" +
                    "      vec2 coord = vTexCoord + onePixel*vec2(float(col), float(row));\n" +
                    "      vec4 c = texture2D(uMainTexture, coord);\n" +
                    "      sample[idx++] = c.rgb;\n" +
                    "    }\n" +
                    "  }\n" +
                    "  float gx=0.0;\n" +
                    "  float gy=0.0;\n" +
                    "  for(int i=0; i<9; i++){\n" +
                    "    float gray = dot(sample[i], vec3(0.299,0.587,0.114));\n" +
                    "    gx += gray*sobelX[i];\n" +
                    "    gy += gray*sobelY[i];\n" +
                    "  }\n" +
                    "  float mag = length(vec2(gx,gy));\n" +
                    // scale it up for "darker" lines
                    "  mag = clamp(mag/3.0, 0.0, 1.0);\n" +
                    // invert => black lines => final linesVal
                    "  float linesVal = 1.0 - mag;\n" +

                    // now let's sample the hatch texture => repeated 8 times
                    "  vec4 hatchSample = texture2D(uHatchTexture, vTexCoord * 8.0);\n" +
                    // assume hatch is black lines on white => we can interpret 'hatchSample.r' as intensity
                    "  float hatchVal = hatchSample.r;\n" +

                    // compute factor for "darker area" => if brightness < 0.5 => factor=1, else 0
                    "  float factor = clamp((0.5 - brightness)*2.0, 0.0, 1.0);\n" +

                    // "mix" linesVal with hatchVal => if factor=1 => full hatchVal, else linesVal
                    "  float finalVal = mix(linesVal, hatchVal, factor);\n" +
                    "  gl_FragColor = vec4(vec3(finalVal), 1.0);\n" +
                    "}\n";

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // compile & link
        programId = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        // create textures
        mainTextureId  = createTexture(mainBitmap);
        hatchTextureId = createTexture(hatchBitmap);

        GLES20.glUseProgram(programId);

        // get locations
        aPositionLoc  = GLES20.glGetAttribLocation(programId, "aPosition");
        aTexCoordLoc  = GLES20.glGetAttribLocation(programId, "aTexCoord");
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
        uMainTexLoc   = GLES20.glGetUniformLocation(programId, "uMainTexture");
        uHatchTexLoc  = GLES20.glGetUniformLocation(programId, "uHatchTexture");
        uTexSizeLoc   = GLES20.glGetUniformLocation(programId, "uTexSize");

        GLES20.glClearColor(0f,0f,0f,1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // viewport
        GLES20.glViewport(0,0,width,height);

        // orthographic
        float[] ortho = new float[16];
        Matrix.orthoM(ortho,0, 0f,width, 0f,height, -1f,1f);
        System.arraycopy(ortho,0,mvpMatrix,0,16);

        // scale to fit
        float ratioX = (float)width/(float)imgWidth;
        float ratioY = (float)height/(float)imgHeight;
        float ratio  = Math.min(ratioX, ratioY);

        float drawW = imgWidth* ratio;
        float drawH = imgHeight* ratio;
        float offsetX = (width - drawW)*0.5f;
        float offsetY = (height- drawH)*0.5f;

        // define the quad data
        float[] quadData = {
                //  X,        Y,         U,   V
                offsetX,      offsetY,       0f, 0f,
                offsetX+drawW,offsetY,       1f, 0f,
                offsetX,      offsetY+drawH, 0f, 1f,
                offsetX+drawW,offsetY+drawH, 1f, 1f
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(quadData.length*4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(quadData);
        vertexBuffer.position(0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programId);

        // pass MVP
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc,1,false,mvpMatrix,0);

        // pass texture size => needed for sobel
        GLES20.glUniform2f(uTexSizeLoc,(float)imgWidth,(float)imgHeight);

        // bind main texture => unit0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainTextureId);
        GLES20.glUniform1i(uMainTexLoc,0);

        // bind hatch texture => unit1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, hatchTextureId);
        GLES20.glUniform1i(uHatchTexLoc,1);

        // set up attribute pointers
        int stride = 4*4; // 4 floats (X,Y,U,V)
        vertexBuffer.position(0); // x,y
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GLES20.glVertexAttribPointer(aPositionLoc,2,GLES20.GL_FLOAT,false,stride,vertexBuffer);

        vertexBuffer.position(2); // u,v
        GLES20.glEnableVertexAttribArray(aTexCoordLoc);
        GLES20.glVertexAttribPointer(aTexCoordLoc,2,GLES20.GL_FLOAT,false,stride,vertexBuffer);

        // draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

        // disable
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTexCoordLoc);
    }

    // helper: compile + link
    private int createProgram(String vs, String fs){
        int vsId = loadShader(GLES20.GL_VERTEX_SHADER, vs);
        int fsId = loadShader(GLES20.GL_FRAGMENT_SHADER, fs);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vsId);
        GLES20.glAttachShader(program, fsId);
        GLES20.glLinkProgram(program);

        int[] linkStatus= new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus,0);
        if(linkStatus[0]==0){
            String err = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Program link error: "+err);
        }
        return program;
    }

    private int loadShader(int type, String source){
        int shader=GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,source);
        GLES20.glCompileShader(shader);

        int[] compiled=new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compiled,0);
        if(compiled[0]==0){
            String err=GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile error: "+err);
        }
        return shader;
    }

    private int createTexture(Bitmap bmp){
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex,0);
        if(tex[0]==0){
            throw new RuntimeException("Error gen texture");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bmp,0);
        return tex[0];
    }
}