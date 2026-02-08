package com.example.myapplication;

public class SectionBgItem {
    String id;
    String name;
    String color;
    int opacity;
    int radius;
    float lineHeight;
    int rotation;
    int nameFontSize;
    int titleFontSize;
    int alignment;
    int fontSize;
    boolean isNameProfession;
    boolean isSelected;
    boolean isMaster;

    public SectionBgItem(String id, String name, String color, int opacity, int radius, float lineHeight, boolean isMaster) {
        this(id, name, color, opacity, radius, lineHeight, isMaster, 0, 100, 100, 1, 14, false);
    }

    public SectionBgItem(String id, String name, String color, int opacity, int radius, float lineHeight, boolean isMaster, int rotation, int nameFontSize, int titleFontSize, int alignment, int fontSize, boolean isNameProfession) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.opacity = opacity;
        this.radius = radius;
        this.lineHeight = lineHeight;
        this.isMaster = isMaster;
        this.isSelected = false;
        this.rotation = rotation;
        this.nameFontSize = nameFontSize;
        this.titleFontSize = titleFontSize;
        this.alignment = alignment;
        this.fontSize = fontSize;
        this.isNameProfession = isNameProfession;
    }
}
