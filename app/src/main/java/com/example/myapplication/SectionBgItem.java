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

    // Constructor for Blank Section (Extended)
    public int blur;
    public int zIndex; // 0=Bottom, 1=Normal, 2=Top
    public int x;
    public int y;
    public boolean isBlank;
    public boolean isStick;
    public String imageMode = "cover"; // "cover", "contain", "center"
    public String shapeType = "rectangle"; // "rectangle", "circle", "triangle", "hexagon"
    public String holeShapeType = "circle"; // "circle", "rectangle", "triangle", "hexagon"
    public int holeSize = 0; // 0-100%
    public int smoothing = 0; // 0-100%
    public int leftShape = 0; // 0=None, 1=Circle, 2=Triangle, 3=Square
    public int rightShape = 0; // 0=None, 1=Circle, 2=Triangle, 3=Square
    public int marginTop = 20;
    public int marginBottom = 20;
    public int xAxis = 100; // 100 represents 0 offset, range 0-200

    // New tabbed sticky section properties
    public int stickLeftShape = 0;
    public int stickLeftW = 20;
    public int stickLeftH = 20;
    public boolean stickLeftProp = true;
    public int stickLeftRot = 0;
    public String stickLeftColor = "#000000";

    public int stickMidShape = 0; // None by default (lines are separate elements)
    public int stickMidW = 20;
    public int stickMidH = 20;
    public boolean stickMidProp = false;
    public int stickMidRot = 0;
    public String stickMidColor = "#000000";

    public int stickRightShape = 0;
    public int stickRightW = 20;
    public int stickRightH = 20;
    public boolean stickRightProp = true;
    public int stickRightRot = 0;
    public String stickRightColor = "#000000";

    public String stickLineColor = "#000000";
    public int stickLineThickness = 2;

    public SectionBgItem setStickLineColor(String c) { this.stickLineColor = c; return this; }
    public SectionBgItem setStickLineThickness(int t) { this.stickLineThickness = t; return this; }

    public SectionBgItem setBlur(int blur) { this.blur = blur; return this; }
    public SectionBgItem setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
    public SectionBgItem setX(int x) { this.x = x; return this; }
    public SectionBgItem setY(int y) { this.y = y; return this; }
    public SectionBgItem setXAxis(int xAxis) { this.xAxis = xAxis; return this; }

    public SectionBgItem setIsBlank(boolean isBlank) { this.isBlank = isBlank; return this; }
    public SectionBgItem setIsStick(boolean isStick) { this.isStick = isStick; return this; }
    public SectionBgItem setImageMode(String imageMode) { this.imageMode = imageMode; return this; }
    public SectionBgItem setShapeType(String shapeType) { this.shapeType = shapeType; return this; }
    public SectionBgItem setHoleShapeType(String holeShapeType) { this.holeShapeType = holeShapeType; return this; }
    public SectionBgItem setHoleSize(int holeSize) { this.holeSize = holeSize; return this; }
    public SectionBgItem setSmoothing(int smoothing) { this.smoothing = smoothing; return this; }
    public SectionBgItem setLeftShape(int leftShape) { this.leftShape = leftShape; return this; }
    public SectionBgItem setRightShape(int rightShape) { this.rightShape = rightShape; return this; }
    public SectionBgItem setMarginTop(int marginTop) { this.marginTop = marginTop; return this; }
    public SectionBgItem setMarginBottom(int marginBottom) { this.marginBottom = marginBottom; return this; }

    public SectionBgItem setStickLeftShape(int stickLeftShape) { this.stickLeftShape = stickLeftShape; return this; }
    public SectionBgItem setStickLeftW(int stickLeftW) { this.stickLeftW = stickLeftW; return this; }
    public SectionBgItem setStickLeftH(int stickLeftH) { this.stickLeftH = stickLeftH; return this; }
    public SectionBgItem setStickLeftProp(boolean stickLeftProp) { this.stickLeftProp = stickLeftProp; return this; }
    public SectionBgItem setStickLeftRot(int stickLeftRot) { this.stickLeftRot = stickLeftRot; return this; }
    public SectionBgItem setStickLeftColor(String stickLeftColor) { this.stickLeftColor = stickLeftColor; return this; }

    public SectionBgItem setStickMidShape(int stickMidShape) { this.stickMidShape = stickMidShape; return this; }
    public SectionBgItem setStickMidW(int stickMidW) { this.stickMidW = stickMidW; return this; }
    public SectionBgItem setStickMidH(int stickMidH) { this.stickMidH = stickMidH; return this; }
    public SectionBgItem setStickMidProp(boolean stickMidProp) { this.stickMidProp = stickMidProp; return this; }
    public SectionBgItem setStickMidRot(int stickMidRot) { this.stickMidRot = stickMidRot; return this; }
    public SectionBgItem setStickMidColor(String stickMidColor) { this.stickMidColor = stickMidColor; return this; }

    public SectionBgItem setStickRightShape(int stickRightShape) { this.stickRightShape = stickRightShape; return this; }
    public SectionBgItem setStickRightW(int stickRightW) { this.stickRightW = stickRightW; return this; }
    public SectionBgItem setStickRightH(int stickRightH) { this.stickRightH = stickRightH; return this; }
    public SectionBgItem setStickRightProp(boolean stickRightProp) { this.stickRightProp = stickRightProp; return this; }
    public SectionBgItem setStickRightRot(int stickRightRot) { this.stickRightRot = stickRightRot; return this; }
    public SectionBgItem setStickRightColor(String stickRightColor) { this.stickRightColor = stickRightColor; return this; }
}
