package org.nutz.walnut.cheap.dom.bean;

import java.io.IOException;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;

public class CheapResource {

    private String alt;

    private String name;

    private byte[] content;

    private int width;

    private int height;

    private BinaryPartAbstractImage img;

    public boolean isEmpty() {
        return Ws.isBlank(name) || null == content || content.length == 0;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String title) {
        this.alt = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffixName() {
        return Wpath.getSuffixName(name);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ImageInfo getImageInfo() throws ImageReadException, IOException {
        return Imaging.getImageInfo(content);
    }

    public BinaryPartAbstractImage getImage(WordprocessingMLPackage wp) throws Exception {
        if (null == img) {
            this.img = BinaryPartAbstractImage.createImagePart(wp, content);
        }
        return this.img;
    }

}
