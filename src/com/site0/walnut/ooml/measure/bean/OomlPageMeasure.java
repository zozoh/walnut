package com.site0.walnut.ooml.measure.bean;

public class OomlPageMeasure {

    private OomlMeasure width;

    private OomlMeasure height;

    private OomlMeasure marginLeft;

    private OomlMeasure marginRight;

    private OomlMeasure marginTop;

    private OomlMeasure marginBottom;

    public void asDXA() {
        width.as(OomlMeaUnit.DXA);
        height.as(OomlMeaUnit.DXA);
        marginLeft.as(OomlMeaUnit.DXA);
        marginRight.as(OomlMeaUnit.DXA);
        marginTop.as(OomlMeaUnit.DXA);
        marginBottom.as(OomlMeaUnit.DXA);
    }

    public OomlMeasure getEditWidth() {
        return width.sub(marginLeft).sub(marginRight);
    }

    public OomlMeasure getEditHeight() {
        return height.sub(marginTop).sub(marginBottom);
    }

    public OomlMeasure getWidth() {
        return width;
    }

    public void setWidth(OomlMeasure width) {
        this.width = width;
    }

    public void setWidth(double width) {
        this.width = OomlMeasure.DXA(width);
    }

    public OomlMeasure getHeight() {
        return height;
    }

    public void setHeight(OomlMeasure height) {
        this.height = height;
    }

    public void setHeight(double height) {
        this.height = OomlMeasure.DXA(height);
    }

    public OomlMeasure getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(OomlMeasure marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginLeft(double height) {
        this.marginLeft = OomlMeasure.DXA(height);
    }

    public OomlMeasure getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(OomlMeasure marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginRight(double marginRight) {
        this.marginRight = OomlMeasure.DXA(marginRight);
    }

    public OomlMeasure getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(OomlMeasure marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginTop(double marginTop) {
        this.marginTop = OomlMeasure.DXA(marginTop);
    }

    public OomlMeasure getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(OomlMeasure marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setMarginBottom(double marginBottom) {
        this.marginBottom = OomlMeasure.DXA(marginBottom);
    }

}
