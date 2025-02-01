package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicConvolve3x3;

import java.nio.IntBuffer;

public class SketchImage {

    public static final int ORIGINAL_TO_GRAY = 0;
    public static final int ORIGINAL_TO_SKETCH = 1;
    public static final int ORIGINAL_TO_COLORED_SKETCH = 2;
    public static final int ORIGINAL_TO_SOFT_SKETCH = 3;
    public static final int ORIGINAL_TO_SOFT_COLOR_SKETCH = 4;
    public static final int GRAY_TO_SKETCH = 5;
    public static final int GRAY_TO_COLORED_SKETCH = 6;
    public static final int GRAY_TO_SOFT_SKETCH = 7;
    public static final int GRAY_TO_SOFT_COLOR_SKETCH = 8;
    public static final int SKETCH_TO_COLOR_SKETCH = 9;
    // Add more if you want (10, 11, etc.)
    public static final int VIBRANT_SKETCH = 10;
    public static final int OIL_PAINTING = 11;
    public static final int NEON_OUTLINE = 12;
    public static final int CARTOONIFY = 13;
    public static final int WATERCOLOR = 14;
    public static final int SHARP_SKETCH = 15;
    public static final int WARM_SKETCH = 16;
    public static final int NEGATIVE_COLOR_SKETCH = 17;
    public static final int POSTERIZE_SKETCH = 18;
    public static final int HALFTONE_SKETCH = 19;

    // Additional filters
    public static final int FILTER_BRIGHTNESS = 99;
    public static final int FILTER_CONTRAST = 98;
    public static final int FILTER_SEPIA = 97;
    public static final int FILTER_GRAYSCALE = 96;
    public static final int FILTER_HUE = 95;
    public static final int FILTER_SATURATION = 94;
    public static final int FILTER_POSTERIZE = 93;
    public static final int FILTER_GAMMA = 92;
    public static final int FILTER_INVERT_COLORS = 91;

    // NEW enumerations
    public static final int DARK_PENCIL = 100;
    public static final int LIGHT_PENCIL = 101;
    public static final int INK_STYLE = 102;
    public static final int CHARCOAL_SKETCH = 103;
    public static final int ETCHED_HATCH = 104;
    public static final int STYLE_DARK_PENCIL_1       = 200;
    public static final int STYLE_DARK_PENCIL_2       = 201;
    public static final int STYLE_COLORED_PENCIL_1    = 202;
    public static final int STYLE_COLORED_PENCIL_2    = 203;
    public static final int STYLE_HARD_STROKE         = 204;
    public static final int STYLE_SOFT_STROKE         = 205;
    public static final int STYLE_INK_PEN             = 206;
    public static final int STYLE_CHARCOAL            = 207;
    public static final int STYLE_CHALK               = 208;
    public static final int STYLE_GRAPHITE_SHADE      = 209;
    public static final int STYLE_CROSS_HATCH         = 210;
    public static final int STYLE_TECHNICAL_DRAWING   = 211;
    public static final int STYLE_VINTAGE_PENCIL      = 212;
    public static final int STYLE_HIGH_CONTRAST_PENCIL= 213;
    public static final int STYLE_STIPPLE             = 214;

    public static final int SKETCH_CRISP_INK        = 300;
    public static final int SKETCH_SHADED_PENCIL    = 301;
    public static final int SKETCH_SOFT_SHADE       = 302;
    public static final int SKETCH_HALFTONE         = 303;
    public static final int SKETCH_POSTERIZED       = 304;
    public static final int SKETCH_LINE_ART         = 305;
    public static final int SKETCH_CHARCOAL_SMUDGE  = 306;
    public static final int SKETCH_HATCHED_DETAIL   = 307;
    public static final int POSTERIZE_GRAY = 308;
    public static final int CARTOONIFY_GRAY = 309;

    public static final int ML_SKETCH_BASE = 1000; // offset so it won't clash

    public static final int ML_SKETCH_1  = ML_SKETCH_BASE + 1;
    public static final int ML_SKETCH_2  = ML_SKETCH_BASE + 2;
    public static final int ML_SKETCH_3  = ML_SKETCH_BASE + 3;
    public static final int ML_SKETCH_4  = ML_SKETCH_BASE + 4;
    public static final int ML_SKETCH_5  = ML_SKETCH_BASE + 5;
    public static final int ML_SKETCH_6  = ML_SKETCH_BASE + 6;
    public static final int ML_SKETCH_7  = ML_SKETCH_BASE + 7;
    public static final int ML_SKETCH_8  = ML_SKETCH_BASE + 8;
    public static final int ML_SKETCH_9  = ML_SKETCH_BASE + 9;
    public static final int ML_SKETCH_10 = ML_SKETCH_BASE + 10;
    public static final int ML_SKETCH_11 = ML_SKETCH_BASE + 11;

    public static final int CUSTOM_SKETCH_1 = 901;
    public static final int CUSTOM_SKETCH_2 = 902;
    public static final int CUSTOM_SKETCH_3 = 903;
    public static final int CUSTOM_SKETCH_4 = 904;
    public static final int CUSTOM_SKETCH_5 = 905;
    public static final int CUSTOM_SKETCH_6 = 906;
    public static final int CUSTOM_SKETCH_7 = 907;
    public static final int CUSTOM_SKETCH_8 = 908;
    public static final int CUSTOM_SKETCH_9 = 909;
    public static final int CUSTOM_SKETCH_10 = 910;
    public static final int CUSTOM_SKETCH_11 = 911;
    public static final int CUSTOM_SKETCH_12 = 912;
    public static final int CUSTOM_SKETCH_13 = 913;
    public static final int CUSTOM_SKETCH_14 = 914;
    public static final int CUSTOM_SKETCH_15 = 915;
    public static final int CUSTOM_SKETCH_16 = 916;
    public static final int CUSTOM_SKETCH_17 = 917;
    public static final int CUSTOM_SKETCH_18 = 918;
    public static final int CUSTOM_SKETCH_19 = 919;





    private Context context;
    private Bitmap bitmap;

    private Bitmap bmGray, bmInvert, bmBlur, bmBlend;

    private SketchImage(Builder builder){
        this.context = builder.context;
        this.bitmap = builder.bitmap;
    }

    /**
     * Apply a specific sketch conversion type with a certain intensity/value
     * @param type  e.g. SketchImage.ORIGINAL_TO_GRAY
     * @param value range 0 to 100
     * @return      processed Bitmap
     */
    public Bitmap getImageAs(int type, int value) {
        switch (type) {
            case ORIGINAL_TO_GRAY:
                bmGray = toGrayscale(bitmap, 101 - value);
                bmInvert = toInverted(bmGray, 1);
                bmBlur = toBlur(bmInvert, 1);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case ORIGINAL_TO_SKETCH:
                bmGray = toGrayscale(bitmap, 101 - value);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);

                return bmBlend;

            case ORIGINAL_TO_COLORED_SKETCH:
                bmGray = toGrayscale(bitmap, 100);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, value);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case ORIGINAL_TO_SOFT_SKETCH:
                bmGray = toGrayscale(bitmap, 101 - value);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 1);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case ORIGINAL_TO_SOFT_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, 100);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 101 - value);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SKETCH:
                bmGray = toGrayscale(bitmap, 1);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_COLORED_SKETCH:
                bmGray = toGrayscale(bitmap, value);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, value);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SOFT_SKETCH:
                bmGray = toGrayscale(bitmap, 100);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 1);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case GRAY_TO_SOFT_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, value);
                bmInvert = toInverted(bmGray, value);
                bmBlur = toBlur(bmInvert, 1);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case SKETCH_TO_COLOR_SKETCH:
                bmGray = toGrayscale(bitmap, value);
                bmInvert = toInverted(bmGray, 100);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, bmGray, 100);
                return bmBlend;

            case CUSTOM_SKETCH_1:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_1);
                Bitmap combined = overlayTexture(bmGray, overlay, 0.3f);
                bmInvert = toInverted(combined, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined, 100);
                return bmBlend;
            case CUSTOM_SKETCH_2:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_2);
                Bitmap combined2 = overlayTexture(bmGray, overlay2, 0.3f);
                bmInvert = toInverted(combined2, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined2, 100);
                Bitmap combined2New = overlayTexture(bmBlend, overlay2, 0.1f);
                return combined2New;
            case CUSTOM_SKETCH_3:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_3);
                Bitmap combined3 = overlayTexture(bmGray, overlay3, 0.3f);
                bmInvert = toInverted(combined3, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined3, 100);
                Bitmap combined3New = overlayTexture(bmBlend, overlay3, 0.1f);
                return combined3New;
            case CUSTOM_SKETCH_4:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_4);
                Bitmap combined4 = overlayTexture(bmGray, overlay4, 0.3f);
                bmInvert = toInverted(combined4, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined4, 100);
                Bitmap combined4New = overlayTexture(bmBlend, overlay4, 0.1f);
                return combined4New;
            case CUSTOM_SKETCH_5:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_5);
                Bitmap combined5 = overlayTexture(bmGray, overlay5, 0.3f);
                bmInvert = toInverted(combined5, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined5, 100);
                Bitmap combined5New = overlayTexture(bmBlend, overlay5, 0.2f);
                return combined5New;
              //  return bmBlend;
            case CUSTOM_SKETCH_6:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay6 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_6);
                Bitmap combined6 = overlayTexture(bmGray, overlay6, 0.5f);
                bmInvert = toInverted(combined6, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined6, 100);
                Bitmap combined6New = overlayTexture(bmBlend, overlay6, 0.2f);
                return combined6New;
            case CUSTOM_SKETCH_7:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay7 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_7);
                Bitmap combined7 = overlayTexture(bmGray, overlay7, 0.3f);
                bmInvert = toInverted(combined7, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined7, 100);
                Bitmap combined7New = overlayTexture(bmBlend, overlay7, 0.2f);
                return combined7New;
                //return bmBlend;
            case CUSTOM_SKETCH_8:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay8 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_8);
                Bitmap combined8 = overlayTexture(bmGray, overlay8, 0.3f);
                bmInvert = toInverted(combined8, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined8, 100);
                return bmBlend;
            case CUSTOM_SKETCH_9:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay9 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_9);
                Bitmap combined9 = overlayTexture(bmGray, overlay9, 0.3f);
                bmInvert = toInverted(combined9, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined9, 100);
                Bitmap combined9New = overlayTexture(bmBlend, overlay9, 0.2f);
                return combined9New;
                //return bmBlend;
               // return bmBlend;
            case CUSTOM_SKETCH_10:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay10 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_10);
                Bitmap combined10 = overlayTexture(bmGray, overlay10, 0.2f);
                bmInvert = toInverted(combined10, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined10, 100);
                return bmBlend;
            case CUSTOM_SKETCH_11:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay11 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_11);
                Bitmap combined11 = overlayTexture(bmGray, overlay11, 0.3f);
                bmInvert = toInverted(combined11, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined11, 100);
                return bmBlend;
            case CUSTOM_SKETCH_12:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay12 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_12);
                Bitmap combined12 = overlayTexture(bmGray, overlay12, 0.3f);
                bmInvert = toInverted(combined12, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined12, 100);
                return bmBlend;
            case CUSTOM_SKETCH_13:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay13 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_13);
                Bitmap combined13 = overlayTexture(bmGray, overlay13, 0.1f);
                bmInvert = toInverted(combined13, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined13, 100);
                return bmBlend;
            case CUSTOM_SKETCH_14:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay14 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_14);
                Bitmap combined14 = overlayTexture(bmGray, overlay14, 0.3f);
                bmInvert = toInverted(combined14, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined14, 100);
                //Bitmap overlay14 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_14);
                Bitmap combined14New = overlayTexture(bmBlend, overlay14, 0.2f);
                return combined14New;
               // return bmBlend;
            case CUSTOM_SKETCH_15:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay15 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_15);
                Bitmap combined15 = overlayTexture(bmGray, overlay15, 0.5f);
                bmInvert = toInverted(combined15, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined15, 100);
               // Bitmap overlay15 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_15);
                Bitmap combined15New = overlayTexture(bmBlend, overlay15, 0.2f);
                return combined15New;
            case CUSTOM_SKETCH_16:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay16 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_16);
                Bitmap combined16 = overlayTexture(bmGray, overlay16, 0.3f);
                bmInvert = toInverted(combined16, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined16, 100);
                Bitmap combined16New = overlayTexture(bmBlend, overlay16, 0.2f);
                return combined16New;
               // return bmBlend;
            case CUSTOM_SKETCH_17:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay17 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_17);
                Bitmap combined17 = overlayTexture(bmGray, overlay17, 0.3f);
                bmInvert = toInverted(combined17, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined17, 100);
                return bmBlend;
            case CUSTOM_SKETCH_18:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay18 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_18);
                Bitmap combined18 = overlayTexture(bmGray, overlay18, 0.65f);
                bmInvert = toInverted(combined18, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined18, 100);
                return bmBlend;
            case CUSTOM_SKETCH_19:
                bmGray = toGrayscale(bitmap, 1);
                Bitmap overlay19 = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_19);
                Bitmap combined19 = overlayTexture(bmGray, overlay19, 0.2f);
                bmInvert = toInverted(combined19, value);
                bmBlur = toBlur(bmInvert, 100);
                bmBlend = colorDodgeBlend(bmBlur, combined19, 100);
                return bmBlend;

            case VIBRANT_SKETCH:
                return vibrantSketch(bitmap, value);

            case OIL_PAINTING:
                return oilPaintingEffect(bitmap, value);

            case NEON_OUTLINE:
                return neonOutlineSketch(bitmap, value);

            case CARTOONIFY:
                return cartoonifySketch(bitmap, value);

            case WATERCOLOR:
                return watercolorSketch(bitmap, value);

            case SHARP_SKETCH:
                return sharpSketch(bitmap, value);

            case WARM_SKETCH:
                return warmSketch(bitmap, value);

            case NEGATIVE_COLOR_SKETCH:
                return negativeColorSketch(bitmap, value);

            case POSTERIZE_SKETCH:
                return posterizeSketch(bitmap, value);

            case POSTERIZE_GRAY:
                return doPosterizedGraySketch(value);

            case HALFTONE_SKETCH:
                return halftoneSketch(bitmap, value);

            case FILTER_BRIGHTNESS:
                return adjustBrightness(bitmap, value);
            case FILTER_CONTRAST:
                return adjustContrast(bitmap, 0.5f + (value / 100f));
            case FILTER_SEPIA:
                return toSepia(bitmap, value);
            case FILTER_GRAYSCALE:
                return toGrayScale(bitmap);
            case FILTER_HUE:
                return adjustHue(bitmap, value);
            case FILTER_SATURATION:
                return adjustSaturation(bitmap, value);
            case FILTER_POSTERIZE:
                return posterizeFilter(bitmap, value);
            case FILTER_GAMMA:
                return adjustGamma(bitmap, 0.5f + (value / 100f));
            case FILTER_INVERT_COLORS:
                return invertColors(bitmap);
            case CARTOONIFY_GRAY:
                return cartoonifySkechGray(bitmap, value);
            // ------------------------------------------------------
            // NEW ones below
            // ------------------------------------------------------
            case DARK_PENCIL:
                return doDarkPencil(value);

            case LIGHT_PENCIL:
                return doLightPencil(value);

            case INK_STYLE:
                return doInkStyle(value);

            case CHARCOAL_SKETCH:
                return doCharcoalSketch(value);

            case ETCHED_HATCH:
                return doEtchedHatch(value);
            // ------------------------------------------
            // 15 NEW STYLES
            // ------------------------------------------
            case STYLE_DARK_PENCIL_1:
                return doDarkPencil1(value);

            case STYLE_DARK_PENCIL_2:
                return doDarkPencil2(value);

            case STYLE_COLORED_PENCIL_1:
                return doColoredPencil1(value);

            case STYLE_COLORED_PENCIL_2:
                return doColoredPencil2(value);

            case STYLE_HARD_STROKE:
                return doHardStroke(value);

            case STYLE_SOFT_STROKE:
                return doSoftStroke(value);

            case STYLE_INK_PEN:
                return doInkPen(value);

            case STYLE_CHARCOAL:
                return doCharcoal(value);

            case STYLE_CHALK:
                return doChalk(value);

            case STYLE_GRAPHITE_SHADE:
                return doGraphiteShade(value);

            case STYLE_CROSS_HATCH:
                return doCrossHatch(value);

            case STYLE_TECHNICAL_DRAWING:
                return doTechnicalDrawing(value);

            case STYLE_VINTAGE_PENCIL:
                return doVintagePencil(value);

            case STYLE_HIGH_CONTRAST_PENCIL:
                return doHighContrastPencil(value);

            case STYLE_STIPPLE:
                return doStipple(value);
            case SKETCH_CRISP_INK:
                return doCrispInk(value);

            case SKETCH_SHADED_PENCIL:
                return doShadedPencil(value);

            case SKETCH_SOFT_SHADE:
                return doSoftShade(value);

            case SKETCH_HALFTONE:
                return doHalftone(value);

            case SKETCH_POSTERIZED:
                return doPosterizedSketch(value);

            case SKETCH_LINE_ART:
                return doLineArt(value);

            case SKETCH_CHARCOAL_SMUDGE:
                return doCharcoalSmudge(value);

            case SKETCH_HATCHED_DETAIL:
                return doHatchedDetail(value);


            default:
                return bitmap;
        }
    }


    public static class Builder {
        private final Context context;
        private final Bitmap bitmap;

        public Builder(Context context, Bitmap bitmap){
            this.context = context;
            this.bitmap = bitmap;
        }

        public SketchImage build(){
            return new SketchImage(this);
        }
    }

    private Bitmap toGrayscale(Bitmap bmpOriginal, float saturation) {
        int width = bmpOriginal.getWidth();
        int height = bmpOriginal.getHeight();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(saturation / 100f);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private Bitmap toInverted(Bitmap src, float i) {
        ColorMatrix colorMatrix_Inverted =
                new ColorMatrix(new float[]{
                        -1, 0,  0,  0, 255,
                        0,  -1, 0,  0, 255,
                        0,   0, -1, 0, 255,
                        0,   0,  0,  i/100f, 0
                });

        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix_Inverted);

        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(src, 0, 0, paint);

        return bitmap;
    }

    private Bitmap toBlur(Bitmap input, float i) {
        try {
            RenderScript rsScript = RenderScript.create(context);
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius((i * 25f) / 100f);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);
            rsScript.destroy();

            return result;
        } catch (Exception e) {
            return input;
        }
    }

    private Bitmap colorDodgeBlend(Bitmap source, Bitmap layer, float i) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc, i);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc, i);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc, i);

            int alpha = (int)((i * 255f) / 100f);
            int pixel = Color.argb(alpha, redValueFinal, greenValueFinal, blueValueFinal);
            buffOut.put(pixel);
        }

        buffOut.rewind();
        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        return base;
    }

    private int colordodge(int in1, int in2, float i) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << (int) (i * 8) / 100) / (255 - image)))));
    }

    private static Bitmap adjustBrightness(Bitmap bmp, float brightness) {
        Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return result;
    }

    // Contrast: typical range is [0..2], 1 = normal
    private static Bitmap adjustContrast(Bitmap bmp, float contrast) {
        Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(contrast, contrast, contrast, 1);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return result;
    }

    private static Bitmap toGrayScale(Bitmap bmp) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return result;
    }

    // Sepia filter
    private static Bitmap toSepia(Bitmap bmp, float level) {
        // level can be used to intensify the effect
        // Basic formula, tweak as you like
        ColorMatrix sepiaMatrix = new ColorMatrix();
        sepiaMatrix.setScale(
                1 + (level / 100f),
                1,
                0.8f,
                1
        );
        Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(sepiaMatrix));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return result;
    }

    /**
     * Vibrant Sketch:
     * 1) Convert to grayscale partially (like 50% saturation).
     * 2) Invert lightly, blur, blend.
     * 3) Then apply a Hue shift to reintroduce color.
     */
    private Bitmap vibrantSketch(Bitmap src, int value) {
        Bitmap gray = toGrayscale(src, 0);                 // partial grayscale
        Bitmap inverted = toInverted(gray, value );     // mild invert
        Bitmap blurred = toBlur(inverted, value );      // mild blur
        Bitmap blended = colorDodgeBlend(blurred, gray, 100);
        // reintroduce some vibrance/hue
        return adjustHue(blended, value);
    }

    /**
     * Oil Painting Effect (simplified):
     * Could do a real “oil paint” algorithm or a simplified blur + posterize approach.
     */
    private Bitmap oilPaintingEffect(Bitmap src, int value) {
        // Step 1: Soft blur
        Bitmap blurred = toBlur(src, Math.min(20, value));
        // Step 2: Posterize slightly to mimic paint strokes
        Bitmap result = posterizeFilter(blurred, Math.max(20, value));
        return result;
    }

    /**
     * Neon Outline Sketch:
     * 1) Convert to edges (like an inverted + blend).
     * 2) Shift color towards neon by applying hue or invert colors.
     */
    private Bitmap neonOutlineSketch(Bitmap src, int value) {
        // Basic edge-like approach: grayscale -> invert -> blur -> colorDodgeBlend
        Bitmap gray = toGrayscale(src, 100 - value);
        Bitmap inv  = toInverted(gray, 100);
        Bitmap blur = toBlur(inv, 100);
        Bitmap blend= colorDodgeBlend(blur, gray, 100);
        // Then invert colors again for a neon vibe
        return invertColors(blend);
    }

    /**
     * Cartoonify:
     * 1) Reduce color palette (posterize).
     * 2) Possibly detect edges and darken them.
     */
    private Bitmap cartoonifySketch(Bitmap src, int value) {
        // Posterize color
        Bitmap poster = posterizeFilter(src, value);
        // Outline edges: approach - grayscale + invert + blend
        Bitmap gray = toGrayscale(src, 100);
        Bitmap inv  = toInverted(gray, 100);
        Bitmap edge = colorDodgeBlend(toBlur(inv, 100), gray, 20);
        // Combine the edges with the poster
        // e.g., multiply or overlay
        Bitmap combined = overlayBlend(poster, edge);
      //  Bitmap newGray = toGray(combined);
        return combined;
    }
    private Bitmap cartoonifySkechGray(Bitmap src, int value) {
        Bitmap poster = posterizeFilter(src, value);
        // Outline edges: approach - grayscale + invert + blend
        Bitmap gray = toGrayscale(src, 100);
        Bitmap inv  = toInverted(gray, 100);
        Bitmap edge = colorDodgeBlend(toBlur(inv, 100), gray, 20);
        // Combine the edges with the poster
        // e.g., multiply or overlay
        Bitmap combined = overlayBlend(poster, edge);
        Bitmap newGray = toGray(combined);
        return newGray;
    }

    /**
     * Watercolor:
     * 1) Light blur
     * 2) Increase brightness + reduce saturation
     */
    private Bitmap watercolorSketch(Bitmap src, int value) {
        Bitmap blurred = toBlur(src, Math.min(value, 5));
        // make it bright
        Bitmap bright = adjustBrightness(blurred, value /2f);
        // reduce saturation
        Bitmap water = adjustSaturation(bright, 0);
        return water;
    }

    /**
     * Sharp Sketch:
     * 1) Grayscale + partial invert
     * 2) Slight sharpen filter
     */
    private Bitmap sharpSketch(Bitmap src, int value) {
        // Basic sketch
        Bitmap gray = toGrayscale(src, 0);
        Bitmap inv  = toInverted(gray, 100);
        Bitmap blurred = toBlur(inv, 100);
        Bitmap blend = colorDodgeBlend(blurred, gray, 100);
        // Now sharpen
        return sharpenFilter(blend, value / 50f);
    }

    /**
     * Warm Sketch:
     * 1) Original to partial grayscale or soft color
     * 2) Increase red/yellow tones
     */
    private Bitmap warmSketch(Bitmap src, int value) {
        Bitmap gray = toGrayscale(src, 70);  // partial
        Bitmap inv  = toInverted(gray, 80);
        Bitmap blur = toBlur(inv, 20);
        Bitmap blend= colorDodgeBlend(blur, gray, 100);
        // Shift color to warm (increase red/yellow)
        return warmFilter(blend, value);
    }

    /**
     * Negative Color Sketch:
     * 1) Basic sketch
     * 2) Invert final colors
     */
    private Bitmap negativeColorSketch(Bitmap src, int value) {
        Bitmap gray = toGrayscale(src, 100);
        Bitmap inv  = toInverted(gray, 100);
        Bitmap blur = toBlur(inv, 100);
        Bitmap blend= colorDodgeBlend(blur, gray, 100);
        // Now invert everything
        return invertColors(blend);
    }

    /**
     * Posterize Sketch:
     * 1) Basic sketch
     * 2) Posterize the result
     */
    private Bitmap posterizeSketch(Bitmap src, int value) {
        Bitmap basic = ORIGINAL_TO_SKETCH_LOGIC(src, value);
        // Then posterize
        return posterizeFilter(basic, value);
    }

    // Example method if you want to reuse original logic
    private Bitmap ORIGINAL_TO_SKETCH_LOGIC(Bitmap src, int value) {
        Bitmap gray = toGrayscale(src, 101 - value);
        Bitmap inverted = toInverted(gray, value);
        Bitmap blurred = toBlur(inverted, 100);
        return colorDodgeBlend(blurred, gray, 100);
    }

    /**
     * Halftone Sketch:
     * 1) Convert to sketch
     * 2) Then apply a “halftone” style effect (dot pattern).
     */
    private Bitmap halftoneSketch(Bitmap src, int value) {
        Bitmap basicSketch = ORIGINAL_TO_SKETCH_LOGIC(src, Math.min(value, 50));
        // Halftone effect
        return halftoneFilter(basicSketch, value);
    }

    private Bitmap adjustHue(Bitmap src, float level) {
        // level range: 0..100 => convert to degrees 0..360 or your preference
        float degrees = (level / 100f) * 360f;

        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        ColorMatrix hueMatrix = new ColorMatrix();
        // Shift hue by 'degrees'
        setHue(hueMatrix, degrees);

        paint.setColorFilter(new ColorMatrixColorFilter(hueMatrix));
        canvas.drawBitmap(src, 0, 0, paint);

        return result;
    }

    // Utility to modify hue
    private void setHue(ColorMatrix cm, float degrees) {
        // This method modifies the color matrix to shift hue.
        // One known approach: first convert to HSV or do advanced math. For brevity:
        float cosVal = (float) Math.cos(Math.toRadians(degrees));
        float sinVal = (float) Math.sin(Math.toRadians(degrees));

        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;

        ColorMatrix mat = new ColorMatrix(new float[]{
                lumR + cosVal*(1-lumR) + sinVal*(-lumR),   lumG + cosVal*(-lumG) + sinVal*(-lumG),  lumB + cosVal*(-lumB) + sinVal*(1 - lumB), 0, 0,
                lumR + cosVal*(-lumR) + sinVal*(0.143f),   lumG + cosVal*(1-lumG) + sinVal*(0.140f), lumB + cosVal*(-lumB) + sinVal*(-0.283f),  0, 0,
                lumR + cosVal*(-lumR) + sinVal*(-(1-lumR)),lumG + cosVal*(-lumG) + sinVal*(lumG),   lumB + cosVal*(1-lumB) + sinVal*(lumB),    0, 0,
                0,                                         0,                                       0,                                       1, 0
        });
        cm.postConcat(mat);
    }

    private Bitmap adjustSaturation(Bitmap src, float level) {
        // level from 0..100
        float satValue = level / 100f;

        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(satValue);

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);

        return result;
    }
    private Bitmap posterizeFilter(Bitmap src, int level) {
        // Basic approach: reduce color steps, e.g. 8 to 16 levels
        // level range 0..100 => colorSteps from, say, 2..32
        int colorSteps = Math.max(2, level / 4);
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        int A, R, G, B;
        int pixelColor;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = src.getPixel(x, y);

                A = Color.alpha(pixelColor);
                R = Color.red(pixelColor);
                G = Color.green(pixelColor);
                B = Color.blue(pixelColor);

                R = (R / colorSteps) * colorSteps;
                G = (G / colorSteps) * colorSteps;
                B = (B / colorSteps) * colorSteps;

                result.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return result;
    }
    private Bitmap adjustGamma(Bitmap src, float gamma) {
        // typical gamma range: 0.1..5
        // We'll interpret value=50 => gamma=1, value=100 => gamma=1.5, etc.
        // but you can do direct mapping if you like
        if (gamma <= 0) gamma = 0.1f;

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());

        // build a lookup table
        int[] gammaLUT = new int[256];
        for (int i = 0; i < 256; i++) {
            gammaLUT[i] = (int) Math.min(255, (255.0 * Math.pow(i / 255.0, 1.0 / gamma) + 0.5));
        }

        int A, R, G, B, pixel;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = gammaLUT[Color.red(pixel)];
                G = gammaLUT[Color.green(pixel)];
                B = gammaLUT[Color.blue(pixel)];

                result.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return result;
    }
    private Bitmap invertColors(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        int A, R, G, B;
        int pixelColor;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = src.getPixel(x, y);

                A = Color.alpha(pixelColor);
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);

                result.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return result;
    }
    private Bitmap overlayBlend(Bitmap base, Bitmap blend) {
        // Both must be same size
        Bitmap out = base.copy(Bitmap.Config.ARGB_8888, true);
        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        out.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(out.getWidth() * out.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int colorBase = buffBase.get();
            int colorBlend= buffBlend.get();

            // ARGB
            int alphaBase = Color.alpha(colorBase);
            int redBase   = Color.red(colorBase);
            int greenBase = Color.green(colorBase);
            int blueBase  = Color.blue(colorBase);

            int alphaBlend= Color.alpha(colorBlend);
            int redBlend  = Color.red(colorBlend);
            int greenBlend= Color.green(colorBlend);
            int blueBlend = Color.blue(colorBlend);

            // Simple overlay formula
            int rOut = overlayFormula(redBase, redBlend);
            int gOut = overlayFormula(greenBase, greenBlend);
            int bOut = overlayFormula(blueBase, blueBlend);
            int aOut = Math.max(alphaBase, alphaBlend);

            buffOut.put(Color.argb(aOut, rOut, gOut, bOut));
        }

        buffOut.rewind();
        out.copyPixelsFromBuffer(buffOut);
        return out;
    }

    // Overlay blend formula for each color channel
    private int overlayFormula(int base, int blend) {
        // base and blend in [0..255]
        float b = base / 255f;
        float f = blend / 255f;
        // Overlay mode
        float result = (b < 0.5f) ? (2f * b * f) : (1f - 2f * (1f - b) * (1f - f));
        return (int) (result * 255 + 0.5f);
    }
    private Bitmap warmFilter(Bitmap src, int value) {
        // Increase red, decrease blue slightly
        float warmth = 1 + (value / 100f);

        ColorMatrix cm = new ColorMatrix(new float[]{
                warmth, 0,      0,      0, 0,
                0,      1,      0,      0, 0,
                0,      0, 1 - (value/200f), 0, 0,
                0,      0,      0,      1, 0
        });

        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return out;
    }
    private Bitmap sharpenFilter(Bitmap src, float intensity) {
        // Simple 3x3 sharpen kernel
        //    0  -1   0
        //   -1   5  -1
        //    0  -1   0
        // The center weight (5) can be adjusted by 'intensity'.
        float centerWeight = 5f + intensity;

        float[] kernel = {
                0,         -1,          0,
                -1, centerWeight, -1,
                0,         -1,          0
        };
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, src);
        Allocation allocOut= Allocation.createFromBitmap(rs, dest);

        // Create script for convolution
        ScriptIntrinsicConvolve3x3 convolve = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolve.setCoefficients(kernel);
        convolve.setInput(allocIn);
        convolve.forEach(allocOut);

        allocOut.copyTo(dest);
        rs.destroy();
        return dest;
    }

    /**
     * Applies a simple "dot halftone" effect:
     * 1) Convert the image to grayscale (optional, or do color halftone).
     * 2) Divide the image into cells of size cellSize x cellSize.
     * 3) For each cell, find the average brightness (0..255).
     * 4) Draw a circle in the output image with radius proportional to (255 - averageBrightness).
     *
     * @param src       The source Bitmap (ARGB_8888).
     * @param intensity Typically the cell size or a scale factor.
     *                  The bigger the cell size, the more pronounced the halftone pattern.
     * @return A new Bitmap with halftone effect.
     */
    private Bitmap halftoneFilter(Bitmap src, int intensity) {
        // cellSize controls how big each "dot" cell is
        // We'll map intensity (0..100) to something like 4..20
        int cellSize = Math.max(4, intensity / 5);
        if (cellSize < 4) cellSize = 4;
        if (cellSize > 50) cellSize = 50; // safeguard for extremely large cells

        // Optionally, convert the source to grayscale first:
        Bitmap gray = toGrayScale(src);

        int width = gray.getWidth();
        int height = gray.getHeight();

        // We'll create a white background canvas the same size
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);

        // We read pixels from 'gray' in blocks of cellSize
        int[] pixels = new int[cellSize * cellSize];
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        // For each cell, compute average brightness and draw a circle
        for (int y = 0; y < height; y += cellSize) {
            for (int x = 0; x < width; x += cellSize) {

                // cellWidth/cellHeight might be smaller if near the boundary
                int cellWidth = Math.min(cellSize, width - x);
                int cellHeight = Math.min(cellSize, height - y);

                // Get all pixels in this cell
                gray.getPixels(pixels, 0, cellWidth, x, y, cellWidth, cellHeight);

                // Compute average brightness
                long sum = 0;
                for (int i = 0; i < cellWidth * cellHeight; i++) {
                    int c = pixels[i];
                    int brightness = Color.red(c); // since it's grayscale, R=G=B
                    sum += brightness;
                }
                float avg = (float) sum / (cellWidth * cellHeight); // 0..255

                // Dot size: the darker the cell, the larger the dot
                // e.g. radius goes from 0..(cellSize/2)
                // We'll invert brightness so darker => bigger radius
                float darkness = 255f - avg;  // 0..255
                float radius = (darkness / 255f) * (cellSize / 2f);

                // Draw the circle in black
                float centerX = x + cellWidth / 2f;
                float centerY = y + cellHeight / 2f;
                paint.setColor(Color.BLACK);
                canvas.drawCircle(centerX, centerY, radius, paint);
            }
        }

        return result;
    }

    // ----------- Example "Dark Pencil" Pipeline -------------
    // A pipeline that intensifies edges, does a deeper color dodge,
    // and possibly lowers brightness for a heavier/darker pencil look.
    private Bitmap doDarkPencil(int intensity) {
        // 1) Convert to grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Invert with partial alpha
        Bitmap invert = toInverted(gray, 100); // full invert
        // 3) Blur heavily
        Bitmap blurred = toBlur(invert, Math.min(100, intensity + 20));
        // 4) colorDodge blend with the original grayscale
        Bitmap blended = colorDodgeBlend(blurred, gray, 100);
        // 5) optionally darken
        Bitmap darkened = adjustBrightness2(blended, -30); // shift brightness
        // 6) maybe adjust contrast slightly
        Bitmap finalImg = adjustContrast2(darkened, 1.2f); // raise contrast

        return finalImg;
    }

    // ----------- Example "Light Pencil" Pipeline -------------
    // A softer version with less intense edges
    private Bitmap doLightPencil(int intensity) {
        // 1) partial grayscale (the lower the intensity, the more color we keep)
        Bitmap partialGray = toGrayscale(bitmap, 50 + (intensity/2));
        // 2) invert lightly
        Bitmap invert = toInverted(partialGray, 50 + intensity/2);
        // 3) blur less
        Bitmap blurred = toBlur(invert, Math.max(1, intensity/2));
        // 4) color dodge
        Bitmap blended = colorDodgeBlend(blurred, partialGray, 80);
        // 5) brighten a bit
        Bitmap brightened = adjustBrightness2(blended, 30);
        // 6) reduce contrast slightly
        Bitmap finalImg = adjustContrast2(brightened, 1f);

        return finalImg;
    }

    // ----------- Example "Ink Style" -------------
    // A pipeline focusing on strong black lines with thresholding
    private Bitmap doInkStyle(int intensity) {
        // 1) Convert to grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Apply some edge detection or invert-blur-dodge
        Bitmap invert = toInverted(gray, 100);
        Bitmap blurred = toBlur(invert, 100);
        Bitmap basicSketch = colorDodgeBlend(blurred, gray, 100);

        // 3) Convert the result to pure black & white with threshold
        // if intensity is high => more black
        int threshold = 128 + (intensity - 50); // range [78..178]
        Bitmap thresholded = applyThreshold(basicSketch, threshold);

        // 4) Optionally do posterize to 2-3 levels for an "ink" feel
        Bitmap finalImg = posterize(thresholded, 2);

        return finalImg;
    }

    // ---------- Example "Charcoal Sketch" ----------
    // Combining partial posterization, heavier blur, partial invert
    private Bitmap doCharcoalSketch(int intensity) {
        // 1) Gray
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Slight invert
        Bitmap invert = toInverted(gray, 50);
        // 3) Blur moderately
        Bitmap blurred = toBlur(invert, 30 + intensity/3);
        // 4) color dodge
        Bitmap blended = colorDodgeBlend(blurred, gray, 100);
        // 5) posterize to ~4-5 levels
        Bitmap posterized = posterize(blended, 4 + intensity/25);
        // 6) adjust contrast (charcoal is typically high contrast)
        Bitmap finalImg = adjustContrast2(posterized, 1.5f);

        return finalImg;
    }

    // ---------- Example "Etched Hatch" ----------
    // A pipeline that uses partial edge detection plus cross-hatching pattern
    // We'll simulate cross-hatch by repeated blend of "Sobel edges" or multiple thresholds
    private Bitmap doEtchedHatch(int intensity) {
        // 1) Gray
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Sobel edges (or your custom method)
        Bitmap edges = doSobel(gray);
        // 3) Possibly invert edges so lines are black on white
        // We'll keep edges black => no invert
        // 4) Combine edges with partial color or partial gray
        Bitmap combined = multiplyBlend(gray, edges);

        // 5) If we want "hatching," we can do repeated threshold overlays
        // For demonstration, let's just do a posterize
        Bitmap finalImg = posterize(combined, 3 + (intensity/30));

        return finalImg;
    }

    private Bitmap posterize(Bitmap src, int levels) {
        // levels = 2,3,4,...
        // Each channel is mapped to discrete steps
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int step = 256 / levels;  // e.g. if levels=4 => step=64

        int w = src.getWidth();
        int h = src.getHeight();
        int[] pixels = new int[w*h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i=0; i<w*h; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int g = (c >>  8) & 0xFF;
            int b = (c      ) & 0xFF;

            r = (r/step)*step;  // quantize
            g = (g/step)*step;
            b = (b/step)*step;

            // clamp
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);

            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    private Bitmap applyThreshold(Bitmap src, int thresh) {
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int w = src.getWidth();
        int h = src.getHeight();
        int[] pixels = new int[w*h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i=0; i<w*h; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int g = (c >>  8) & 0xFF;
            int b = (c      ) & 0xFF;

            // If not grayscale, we do average
            int avg = (r+g+b)/3;
            if (avg >= thresh) {
                // white
                r=255; g=255; b=255;
            } else {
                // black
                r=0; g=0; b=0;
            }
            pixels[i] = (a << 24)|(r<<16)|(g<<8)|b;
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

//    /**
//     *  applyThreshold:
//     *  For each pixel, we compute an intensity (average of R,G,B).
//     *  If intensity >= threshold => turn white, else black.
//     *
//     *  @param src the source Bitmap (ARGB_8888 recommended)
//     *  @param threshold value in [0..255]
//     *  @return a new thresholded Bitmap
//     */
//    public static Bitmap applyThreshold(Bitmap src, int threshold) {
//        // Create an output bitmap
//        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
//        int w = src.getWidth();
//        int h = src.getHeight();
//
//        int[] pixels = new int[w * h];
//        src.getPixels(pixels, 0, w, 0, 0, w, h);
//
//        for (int i = 0; i < w * h; i++) {
//            int c = pixels[i];
//            // Extract channels
//            int a = (c >> 24) & 0xFF;
//            int r = (c >> 16) & 0xFF;
//            int g = (c >> 8)  & 0xFF;
//            int b = (c)       & 0xFF;
//
//            // Compute intensity
//            int intensity = (r + g + b) / 3;
//            if (intensity >= threshold) {
//                // White
//                r = g = b = 255;
//            } else {
//                // Black
//                r = g = b = 0;
//            }
//            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
//        }
//
//        out.setPixels(pixels, 0, w, 0, 0, w, h);
//        return out;
//    }
//
//    /**
//     * posterize:
//     * Reduce each color channel to "levels" discrete steps.
//     * If levels=2 => each channel is either 0 or 255
//     * If levels=4 => possible channel values are 0,85,170,255
//     *
//     * @param src the source Bitmap
//     * @param levels how many discrete steps per channel
//     * @return new posterized Bitmap
//     */
//    public static Bitmap posterize(Bitmap src, int levels) {
//        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
//        int w = src.getWidth();
//        int h = src.getHeight();
//
//        // e.g. if levels=4, step=256/4=64 => channel becomes 0,64,128,192
//        // but we might want to map them to 0,85,170,255. We'll do an approach
//        // that quantizes to multiples of step, then clamp at 255.
//        if (levels < 2) levels = 2; // at least 2
//        int step = 256 / levels;    // e.g. 64 if levels=4
//
//        int[] pixels = new int[w * h];
//        src.getPixels(pixels, 0, w, 0, 0, w, h);
//
//        for (int i = 0; i < w * h; i++) {
//            int c = pixels[i];
//            int a = (c >> 24) & 0xFF;
//            int r = (c >> 16) & 0xFF;
//            int g = (c >> 8)  & 0xFF;
//            int b = (c)       & 0xFF;
//
//            r = (r / step) * step;
//            g = (g / step) * step;
//            b = (b / step) * step;
//
//            // ensure no overflow
//            if (r > 255) r = 255;
//            if (g > 255) g = 255;
//            if (b > 255) b = 255;
//
//            pixels[i] = (a << 24)|(r<<16)|(g<<8)|b;
//        }
//
//        out.setPixels(pixels, 0, w, 0, 0, w, h);
//        return out;
//    }

    /**
     * adjustBrightness:
     * Add a delta to each channel. delta in [-255..255].
     * Negative => darker, positive => brighter.
     */
    public static Bitmap adjustBrightness2(Bitmap src, float delta) {
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int w = src.getWidth();
        int h = src.getHeight();

        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < w * h; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8)  & 0xFF;
            int b = (c)       & 0xFF;

            // add delta
            int nr = (int)(r + delta);
            int ng = (int)(g + delta);
            int nb = (int)(b + delta);

            // clamp [0..255]
            nr = Math.max(0, Math.min(255, nr));
            ng = Math.max(0, Math.min(255, ng));
            nb = Math.max(0, Math.min(255, nb));

            pixels[i] = (a << 24)|(nr<<16)|(ng<<8)|nb;
        }

        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    /**
     * adjustContrast:
     * scale in [0..2], 1 = no change
     * 0 => pure gray, 2 => high contrast
     * formula: out = ((pixel-128)*scale + 128)
     */
    public static Bitmap adjustContrast2(Bitmap src, float scale) {
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int w = src.getWidth();
        int h = src.getHeight();

        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        float midpoint = 128f;
        for (int i = 0; i < w * h; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8)  & 0xFF;
            int b = (c)       & 0xFF;

            // shift relative to 128
            float nr = (r - midpoint) * scale + midpoint;
            float ng = (g - midpoint) * scale + midpoint;
            float nb = (b - midpoint) * scale + midpoint;

            int ir = clamp((int)nr);
            int ig = clamp((int)ng);
            int ib = clamp((int)nb);

            pixels[i] = (a << 24)|(ir<<16)|(ig<<8)|ib;
        }

        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    /**
     * doSobel:
     * A simple CPU-based sobel edge detection on a GRAYSCALE image.
     * We'll do:
     *   Gray => convolve with Gx, Gy => magnitude => return black/white edges
     *
     * For best results, pass in a grayscale image first. We'll skip that here and just convert on the fly.
     */
    public static Bitmap doSobel(Bitmap src) {
        // 1) Convert to grayscale
        Bitmap gray = toGray(src);

        // 2) We'll create an output for edges
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int w = src.getWidth();
        int h = src.getHeight();

        int[] pixels = new int[w * h];
        gray.getPixels(pixels, 0, w, 0, 0, w, h);

        // Sobel kernels
        int[] gxKernel = {-1, 0, 1,
                -2, 0, 2,
                -1, 0, 1};
        int[] gyKernel = {-1,-2,-1,
                0, 0, 0,
                1, 2, 1};

        int[] outPixels = new int[w * h];

        // skip border
        for (int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                int sumX = 0;
                int sumY = 0;
                // convolve 3x3
                int idxK = 0;
                for (int ky=-1; ky<=1; ky++) {
                    for (int kx=-1; kx<=1; kx++) {
                        int px = x + kx;
                        int py = y + ky;
                        int pixel = pixels[py*w + px];
                        int intensity = (pixel >> 16) & 0xFF; // since grayscale => R=G=B
                        sumX += intensity * gxKernel[idxK];
                        sumY += intensity * gyKernel[idxK];
                        idxK++;
                    }
                }
                // magnitude
                int mag = (int) Math.sqrt(sumX * sumX + sumY * sumY);
                if (mag > 255) mag = 255;
                // produce white edges => invert if you want
                int c = Color.rgb(mag, mag, mag);
                outPixels[y*w + x] = c;
            }
        }
        // fill borders black
        for (int x=0; x<w; x++){
            outPixels[x] = Color.BLACK;
            outPixels[(h-1)*w + x] = Color.BLACK;
        }
        for (int y=0; y<h; y++){
            outPixels[y*w] = Color.BLACK;
            outPixels[y*w + (w-1)] = Color.BLACK;
        }

        out.setPixels(outPixels, 0, w, 0, 0, w, h);
        return out;
    }

    /**
     * multiplyBlend:
     * out = (base * blend) / 255 for each channel
     */
    public static Bitmap multiplyBlend(Bitmap base, Bitmap blend) {
        int w = Math.min(base.getWidth(), blend.getWidth());
        int h = Math.min(base.getHeight(), blend.getHeight());
        // create an output
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        int[] basePix = new int[w*h];
        int[] blendPix = new int[w*h];
        base.getPixels(basePix, 0, w, 0, 0, w, h);
        blend.getPixels(blendPix, 0, w, 0, 0, w, h);

        int[] outPix = new int[w*h];
        for (int i=0; i<w*h; i++){
            int c1 = basePix[i];
            int c2 = blendPix[i];

            int a1 = (c1 >> 24) & 0xFF;
            int r1 = (c1 >> 16) & 0xFF;
            int g1 = (c1 >> 8)  & 0xFF;
            int b1 =  c1        & 0xFF;

            int a2 = (c2 >> 24) & 0xFF;
            int r2 = (c2 >> 16) & 0xFF;
            int g2 = (c2 >> 8)  & 0xFF;
            int b2 =  c2        & 0xFF;

            // Multiply each channel
            int outA = (a1 * a2)/255;
            int outR = (r1 * r2)/255;
            int outG = (g1 * g2)/255;
            int outB = (b1 * b2)/255;

            outPix[i] = (outA<<24)|(outR<<16)|(outG<<8)|outB;
        }

        out.setPixels(outPix, 0, w, 0, 0, w, h);
        return out;
    }

    // ------------------- Helpers -------------------

    // Convert a color image to grayscale by average or standard formula
    private static Bitmap toGray(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, src.getConfig());
        int[] pixels = new int[w*h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i=0; i<w*h; i++){
            int c = pixels[i];
            int a = (c>>24)&0xFF;
            int r = (c>>16)&0xFF;
            int g = (c>>8)&0xFF;
            int b = c&0xFF;

            // simple average
            int gray = (r+g+b)/3;
            pixels[i] = (a<<24)|(gray<<16)|(gray<<8)|gray;
        }
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    // clamp helper
    private static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    //Completely New
    private Bitmap doDarkPencil1(int intensity) {
        // 1) Full grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Invert strongly
        Bitmap invert = toInverted(gray, 100);
        // 3) Blur with moderate or high radius
        Bitmap blurred = toBlur(invert, Math.min(100, intensity + 20));
        // 4) colorDodge with original gray
        Bitmap blended = colorDodgeBlend(blurred, gray, 100);
        // 5) Darken a bit
        Bitmap darkened = adjustBrightness(blended, -30);
        // 6) Slight contrast increase
        Bitmap finalImg = adjustContrast(darkened, 1.2f);

        return finalImg;
    }
    private Bitmap doDarkPencil2(int intensity) {
        // Similar to Dark Pencil 1, but heavier shading
        Bitmap gray = toGrayscale(bitmap, 0);
        Bitmap invert = toInverted(gray, 100);
        Bitmap blurred = toBlur(invert, 100); // always max blur
        Bitmap blended = colorDodgeBlend(blurred, gray, 100);

        // Now apply a threshold to emphasize darkest lines
        Bitmap thresholded = applyThreshold(blended, 180 - intensity);
        // Then slightly posterize
        Bitmap poster = posterize(thresholded, 2 + intensity/25);
        // Maybe lower brightness further
        Bitmap finalImg = adjustBrightness(poster, -20);

        return finalImg;
    }
    private Bitmap doColoredPencil1(int intensity) {
        // 1) Partially grayscale => keep some color
        Bitmap partialGray = toGrayscale(bitmap, 70); // 70 => partial
        // 2) Invert lightly
        Bitmap invert = toInverted(partialGray, 50 + intensity/2);
        // 3) Blur lightly
        Bitmap blurred = toBlur(invert, 30 + intensity/3);
        // 4) colorDodge
        Bitmap blended = colorDodgeBlend(blurred, partialGray, 90);
        // 5) Posterize to 4 or 5 levels for a colored pencil feel
        Bitmap poster = posterize(blended, 4 + (intensity/25));
        // 6) Slight brightness up
        Bitmap finalImg = adjustBrightness(poster, 10);

        return finalImg;
    }
    private Bitmap doColoredPencil2(int intensity) {
        // More saturated color approach
        // 1) saturate or partial color => skip grayscale
        // 2) invert + blur
        Bitmap invert = toInverted(bitmap, 60 + intensity/2);
        Bitmap blur = toBlur(invert, 20 + intensity/4);
        // 3) color dodge with original
        Bitmap blend = colorDodgeBlend(blur, bitmap, 100);
        // 4) slight posterize
        Bitmap post = posterize(blend, 3 + intensity/30);
        // 5) brightness + contrast
        Bitmap bright = adjustBrightness(post, 15);
        Bitmap finalImg = adjustContrast(bright, 1.1f);

        return finalImg;
    }
    private Bitmap doHardStroke(int intensity) {
        // "Hard stroke" => strong edges & threshold
        Bitmap gray = toGrayscale(bitmap, 100);
        Bitmap invert = toInverted(gray, 100);
        Bitmap blurred = toBlur(invert, 8 + intensity/10);
        Bitmap blend = colorDodgeBlend(blurred, gray, 100);

        // Then do a threshold to create “hard” lines
        Bitmap thresholded = applyThreshold(blend, 128 + intensity/2);
        return thresholded;
    }
    private Bitmap doSoftStroke(int intensity) {
        // softer version => partial color, weaker threshold
        Bitmap partialGray = toGrayscale(bitmap, 50);
        Bitmap invert = toInverted(partialGray, 50);
        Bitmap blurred = toBlur(invert, 5 + intensity/5);
        Bitmap blend = colorDodgeBlend(blurred, partialGray, 80);

        // Raise brightness, lower contrast for softness
        Bitmap bright = adjustBrightness(blend, 20);
        Bitmap finalImg = adjustContrast(bright, 0.8f);

        return finalImg;
    }
    private Bitmap doInkPen(int intensity) {
        // We'll do a strong edge extraction, then threshold for black/white lines

        // 1) grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) invert + blur => basic sketch
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 100);
        Bitmap sketch = colorDodgeBlend(blur, gray, 100);

        // 3) threshold to pure black lines
        int thresh = 128 + (intensity - 50);
        Bitmap thresholded = applyThreshold(sketch, thresh);

        // 4) optional small posterize for a smoother ink fill
        Bitmap finalImg = posterize(thresholded, 2);
        return finalImg;
    }
    private Bitmap doCharcoal(int intensity) {
        // Charcoal => partial posterize, heavier blur, partial invert
        Bitmap gray = toGrayscale(bitmap, 100);
        Bitmap invert = toInverted(gray, 50);
        Bitmap blur = toBlur(invert, 10 + intensity/3);
        Bitmap blend = colorDodgeBlend(blur, gray, 100);

        // Posterize ~4 levels for a smudged charcoal effect
        Bitmap poster = posterize(blend, 4 + intensity/30);
        // Increase contrast
        Bitmap finalImg = adjustContrast(poster, 1.4f);

        return finalImg;
    }
    private Bitmap doChalk(int intensity) {
        // Chalk => more "dusty" edges, so let's do partial invert + color dodge, then threshold
        Bitmap gray = toGrayscale(bitmap, 100);
        Bitmap invert = toInverted(gray, 80);
        Bitmap blurred = toBlur(invert, 15 + intensity/4);
        Bitmap blend = colorDodgeBlend(blurred, gray, 100);

        // Light threshold
        Bitmap thresholded = applyThreshold(blend, 200 - intensity);
        // reduce contrast => chalk
        Bitmap finalImg = adjustContrast(thresholded, 0.7f);
        return finalImg;
    }
    private Bitmap doGraphiteShade(int intensity) {
        // Graphite => partial colorDodge, partial multiply for shading
        Bitmap gray = toGrayscale(bitmap, 0);
        Bitmap invert = toInverted(gray, 10);
        Bitmap blur = toBlur(invert,  intensity);
        Bitmap dodge = colorDodgeBlend(blur, gray, 100);

        // Then multiply-blend the result with the original grayscale
        Bitmap finalImg = multiplyBlend(dodge, gray);
        // maybe a mild contrast
        finalImg = adjustContrast(finalImg, 1.1f);

        return finalImg;
    }
    private Bitmap doCrossHatch(int intensity) {
        // Cross-hatch => do sobel edges, multiply them, partial posterize

        // 1) grayscale
        Bitmap gray = toGrayscale(bitmap, 0);
        // 2) sobel
        Bitmap edges = doSobel(gray);
        // 3) invert edges if needed
        // 4) multiply with grayscale
        Bitmap multiplied = multiplyBlend(gray, edges);

        // 5) posterize to small levels => hatch lines
        Bitmap finalImg = posterize(multiplied, intensity+100);
        Bitmap invert = toInverted(finalImg, 100);
        return invert;
    }
    private Bitmap doTechnicalDrawing(int intensity) {
        // Crisp lines, minimal shading
        Bitmap gray = toGrayscale(bitmap, 0);
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 80 + intensity/10);
        Bitmap blend = colorDodgeBlend(blur, gray, 100);




        // Possibly threshold to get clean lines
        Bitmap thresholded = applyThreshold(blend, 220 + intensity/3);



        // Slight contrast up
        Bitmap finalImg = adjustContrast(thresholded, 1.3f);
        return finalImg;
    }
    private Bitmap doVintagePencil(int intensity) {
        // Possibly a warmer / old paper effect => let's do partial gray, slight sepia approach

        // 1) partial grayscale
        Bitmap partialGray = toGrayscale(bitmap, 0);
        // 2) invert + blur
        Bitmap invert = toInverted(partialGray, 90);
        Bitmap blur = toBlur(invert, 50 + intensity/5);
        Bitmap blend = colorDodgeBlend(blur, partialGray, 90);

        // 3) slight sepia: we can do a simple color shift or a "sepia" method
        Bitmap sepia = applySepia(blend, 1.2f); // you'd create a simple function

        // 4) reduce contrast => older look
        Bitmap finalImg = adjustContrast(sepia, 1.3f);
        return finalImg;
    }
    private Bitmap doHighContrastPencil(int intensity) {
        Bitmap gray = toGrayscale(bitmap, 100);
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 20 + intensity/4);
        Bitmap blend = colorDodgeBlend(blur, gray, 100);

        // Then significantly increase contrast
        Bitmap finalImg = adjustContrast(blend, 1.5f + intensity/100f);
        return finalImg;
    }
    private Bitmap doStipple(int intensity) {
        // "Stipple" => many small black dots. We'll do threshold + posterize + partial invert
        Bitmap gray = toGrayscale(bitmap, 100);
        // invert + blur => basic sketch
        Bitmap invert = toInverted(gray, 80);
        Bitmap blur = toBlur(invert, 20 + intensity/4);
        Bitmap blend = colorDodgeBlend(blur, gray, 100);

        // threshold to create black vs. white
        Bitmap thresholded = applyThreshold(blend, 160 - (intensity/2));
        // mild posterize to create blotches
        Bitmap finalImg = posterize(thresholded, 2 + intensity/50);

        return finalImg;
    }
    /**
     * applySepia:
     * Applies a basic sepia effect to a Bitmap.
     *
     * The intensity parameter is how strongly we apply the sepia color shift.
     *
     * For a fully saturated sepia, you might fix intensity at 1.0f (or 0.7..1.0).
     * For a mild/vintage look, you might use intensity ~0.2..0.5.
     */
    private Bitmap applySepia(Bitmap src, float intensity) {
        // If intensity <= 0 => no effect, if > 1 => stronger effect
        if (intensity < 0f) intensity = 0f;
        if (intensity > 1f) intensity = 1f;

        // Create output
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int w = src.getWidth();
        int h = src.getHeight();

        int[] pixels = new int[w*h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < w*h; i++) {
            int c = pixels[i];
            int a = (c >> 24) & 0xFF;
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8)  & 0xFF;
            int b = (c)       & 0xFF;

            // Convert to grayscale (simple average)
            int gray = (r + g + b)/3;

            // Then apply a “sepia” tone
            int outR = (int)(gray * (1 + (0.2f * intensity)));  // typical: 112%
            int outG = (int)(gray * (1 + (0.05f * intensity))); // 105%
            int outB = (int)(gray * (1 - (0.1f * intensity)));  // ~90%

            // clamp
            if (outR > 255) outR = 255;
            if (outG > 255) outG = 255;
            if (outB > 255) outB = 255;

            pixels[i] = (a << 24) | (outR << 16) | (outG << 8) | outB;
        }

        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    private Bitmap doCrispInk(int intensity) {
        // 1) Grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) Invert fully
        Bitmap invert = toInverted(gray, 100-intensity);
        // 3) Blur moderately
        Bitmap blurred = toBlur(invert, 50 + (intensity/2));
        // 4) colorDodge with original grayscale
        Bitmap dodged = colorDodgeBlend(blurred, gray, 100);

        // 5) threshold to black/white lines
        int thr = 160 - (intensity/2); // range ~ (160..110)
        if (thr < 70) thr = 70;
        Bitmap thresholded = applyThreshold(dodged, thr);

        // Crisp lines
        return thresholded;
    }
    private Bitmap doShadedPencil(int intensity) {
        // 1) Full grayscale
        Bitmap gray = toGrayscale(bitmap, 0);
        // 2) Invert strongly
        Bitmap invert = toInverted(gray, 100);
        // 3) Blur with radius ~ (10..40)
        int blurVal = 10 + (intensity/3);
        Bitmap blurred = toBlur(invert, 100);
        // 4) colorDodge
        Bitmap dodged = colorDodgeBlend(blurred, gray, 100);

        // 5) reduce brightness a bit to bring out shading
        Bitmap darker = adjustBrightness(dodged, -100);
        // 6) mild contrast boost
        Bitmap finalImg = adjustContrast(darker, 1.0f);

        return finalImg;
    }
    private Bitmap doSoftShade(int intensity) {
        // 1) Partial grayscale: preserve some faint color (80 => mostly gray)
        Bitmap partialGray = toGrayscale(bitmap, 80);
        // 2) invert lightly
        Bitmap invert = toInverted(partialGray, 60 + intensity/2);
        // 3) small blur
        int blurVal = 5 + (intensity/4);
        Bitmap blurred = toBlur(invert, blurVal);
        // 4) colorDodge
        Bitmap dodged = colorDodgeBlend(blurred, partialGray, 80);
        // 5) lighten a bit
        Bitmap brightened = adjustBrightness(dodged, 15);
        // 6) reduce contrast => "soft"
        Bitmap finalImg = adjustContrast(brightened, 0.85f);

        return finalImg;
    }
    private Bitmap doHalftone(int intensity) {
        // 1) Basic grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) invert + blur => base sketch
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 40 + intensity/3);
        Bitmap sketch = colorDodgeBlend(blur, gray, 100);

        // 3) Posterize or threshold a bit
        Bitmap post = posterize(sketch, 4 + intensity/30);
        // 4) Now apply an artificial "halftone" style.
        // We'll do a threshold or repeated thresholds to create dot patterns,
        // but let's keep it simple:
        Bitmap thr = applyThreshold(post, 150);

        // For true halftone you'd do a grid of circles, but let's do a simple approach:
        // We'll consider thr as final or do multiply with original
        Bitmap finalImg = multiplyBlend(thr, gray);

        return finalImg;
    }
    private Bitmap doPosterizedSketch(int intensity) {
        // 1) grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) invert + blur => base
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 30 + intensity/4);
        Bitmap dodged = colorDodgeBlend(blur, gray, 100);

        // 3) Posterize heavily (levels ~2..5)
        int levels = 2 + (intensity/25);
        if (levels > 6) levels = 6;
        Bitmap finalImg = posterize(dodged, levels);

        return finalImg;
    }
    private Bitmap doPosterizedGraySketch(int intensity) {
        // 1) grayscale
        Bitmap gray = toGrayscale(bitmap, 100);
        // 2) invert + blur => base
        Bitmap invert = toInverted(gray, 100);
        Bitmap blur = toBlur(invert, 30 + intensity/4);
        Bitmap dodged = colorDodgeBlend(blur, gray, 100);

        // 3) Posterize heavily (levels ~2..5)
        int levels = 2 + (intensity/25);
        if (levels > 6) levels = 6;
        Bitmap finalImg = posterize(dodged, levels);
        Bitmap finalImg2 = toGrayScale(finalImg);
        //Bitmap invert2 = toInverted(finalImg2,100);

        return finalImg2;
    }
    private Bitmap doLineArt(int intensity) {
        // 1) doSobel to get edges
        Bitmap edges = doSobel(bitmap);
        // 2) partial invert edges so lines are black on white
        Bitmap edgesInv = toInverted(edges, 100);

        // 3) multiply with partial grayscale
        Bitmap partialGray = toGrayscale(bitmap, 0);
        Bitmap multiplied = multiplyBlend(partialGray, edgesInv);

        // 4) low-level posterize to unify shading
        Bitmap finalImg = posterize(multiplied, 3 + intensity/30);

        return finalImg;
    }
    private Bitmap doCharcoalSmudge(int intensity) {
        // 1) Full grayscale
        Bitmap gray = toGrayscale(bitmap, 0);
        // 2) invert partially
        Bitmap invert = toInverted(gray, intensity);
        // 3) blur strongly
        Bitmap blurred = toBlur(invert,  intensity);
        // 4) colorDodge with gray
        Bitmap dodge = colorDodgeBlend(blurred, gray, 100);

        // 5) reduce brightness => deeper shadows
        Bitmap dark = adjustBrightness(dodge, -20);
        // 6) mild posterize => smudgy
        Bitmap finalImg = posterize(dark, 4 + intensity/25);

        return finalImg;
    }

    private Bitmap doHatchedDetail(int intensity) {
        // 1) grayscale
        Bitmap gray = toGrayscale(bitmap, 0);
        // 2) sobel => edges
        Bitmap edges = doSobel(gray);

        // 3) colorDodge edges with original gray => sharpen details
        Bitmap detail = colorDodgeBlend(edges, gray, 100);

        // 4) partial posterize
        int levels = 3 + intensity/30; // ~3..6
        if (levels < 2) levels = 2;
        Bitmap post = posterize(detail, levels);

        // 5) mild contrast up
        Bitmap finalImg = adjustContrast(post, 1.3f);

        return finalImg;
    }

    /**
     * Overlays the 'overlay' image on top of 'sketch' using a uniform alpha (overlayAlpha).
     *
     * @param sketch        The base Bitmap (e.g. your sketch result).
     * @param overlay       The overlay Bitmap, possibly ARGB_8888 with alpha channel.
     * @param overlayAlpha  A float in [0..1]. If 1 => full overlay, 0 => invisible overlay.
     * @return A new Bitmap with the overlay drawn on top.
     */
    private Bitmap overlayTexture(Bitmap sketch, Bitmap overlay, float overlayAlpha) {
        // clamp overlayAlpha
        if (overlayAlpha < 0f) overlayAlpha = 0f;
        if (overlayAlpha > 1f) overlayAlpha = 1f;

        // Make sure both have same size or scale overlay as needed
        if (overlay.getWidth() != sketch.getWidth() || overlay.getHeight() != sketch.getHeight()) {
            overlay = Bitmap.createScaledBitmap(overlay, sketch.getWidth(), sketch.getHeight(), true);
        }

        // Copy the base into a mutable Bitmap
        Bitmap result = sketch.copy(Bitmap.Config.ARGB_8888, true);

        // Draw with partial alpha
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAlpha((int) (overlayAlpha * 255));

        canvas.drawBitmap(overlay, 0, 0, paint);
        return result;
    }
    /**
     * Multiply-blend the base with the overlay,
     * but first scale the overlay's color channels by overlayAlpha.
     *
     * newRGB = (baseRGB * (overlayRGB * overlayAlpha)) / 255
     *
     * @param base         Base Bitmap
     * @param //overlay      Overlay Bitmap
     * @param overlayAlpha A float in [0..1].
     *                     1 => full overlay color,
     *                     0 => no effect from overlay.
     * @return A new Bitmap result
     */
    private Bitmap multiplyBlend(Bitmap base, Bitmap blend, float overlayAlpha) {
        if (overlayAlpha < 0f) overlayAlpha = 0f;
        if (overlayAlpha > 1f) overlayAlpha = 1f;

        int w = Math.min(base.getWidth(), blend.getWidth());
        int h = Math.min(base.getHeight(), blend.getHeight());
        // create an output
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        int[] basePix = new int[w*h];
        int[] blendPix = new int[w*h];
        base.getPixels(basePix, 0, w, 0, 0, w, h);
        blend.getPixels(blendPix, 0, w, 0, 0, w, h);

        int[] outPix = new int[w*h];
        for (int i=0; i<w*h; i++){
            int c1 = basePix[i];
            int c2 = blendPix[i];

            int a1 = (c1 >> 24) & 0xFF;
            int r1 = (c1 >> 16) & 0xFF;
            int g1 = (c1 >> 8)  & 0xFF;
            int b1 =  c1        & 0xFF;

            int a2 = (c2 >> 24) & 0xFF;
            int r2 = (c2 >> 16) & 0xFF;
            int g2 = (c2 >> 8)  & 0xFF;
            int b2 =  c2        & 0xFF;


            // Multiply each channel
            int outA = (a1 * a2)/255;
            int outR = (r1 * r2)/255;
            int outG = (g1 * g2)/255;
            int outB = (b1 * b2)/255;

            outPix[i] = (outA<<24)|(outR<<16)|(outG<<8)|outB;
        }

        out.setPixels(outPix, 0, w, 0, 0, w, h);
        return out;
    }



}
