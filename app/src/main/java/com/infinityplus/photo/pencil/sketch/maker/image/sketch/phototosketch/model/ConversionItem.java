package com.infinityplus.photo.pencil.sketch.maker.image.sketch.phototosketch.model;

public class ConversionItem {
    private final int type;       // e.g. SketchImage.ORIGINAL_TO_GRAY, etc.
    private final String name;    // e.g. "Gray", "Sketch", ...
    private final int previewRes; // For displaying a small icon

    public ConversionItem(int type, String name, int previewRes) {
        this.type = type;
        this.name = name;
        this.previewRes = previewRes;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getPreviewRes() {
        return previewRes;
    }
}
