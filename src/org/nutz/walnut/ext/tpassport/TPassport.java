package org.nutz.walnut.ext.tpassport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.qrcode.QRCode;
import org.nutz.qrcode.QRCodeFormat;
import org.nutz.walnut.impl.box.WnSystem;

public class TPassport {

    private static NutMap fontCache = NutMap.NEW();

    private WnSystem sys;
    private BufferedImage im;
    private Graphics2D gc;
    private int width;
    private int height;
    private Color bgColor;
    private String backgroundColor;
    private String backgroundImage;
    private List<TPassportDrawItem> items;
    private Color dftFontColor = Color.BLACK;
    private Color dftBgColor = Colors.as("rgba(0,0,0,0.3)");
    private String dftFont = "Microsoft Yahei.ttf";
    private int dftFontStyle = Font.PLAIN;
    private int dftFontSize = 13;
    
    public TPassport() {
        width = 840;
        height = 600;
        bgColor = Colors.as("#88000000");
    }

    public TPassport(NutMap conf, WnSystem wnSys) {
        sys = wnSys;
        // 默认配置
        width = conf.getInt("width", 840);
        height = conf.getInt("height", 600);
        bgColor = Colors.as(conf.getString("backgroundColor", "#88000000"));
        backgroundImage = conf.getString("backgroundImage", "");
        // 其他内容
        items = conf.getList("items", TPassportDrawItem.class);
    }

    public void prepare() {
        im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        gc = im.createGraphics();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setBackground(bgColor);
        gc.clearRect(0, 0, width, height);
        if (!Strings.isBlank(backgroundImage)) {
            try {
                BufferedImage bgImg = loadImage(backgroundImage);
                bgImg = zoomScaleImage(bgImg, width, height, bgColor);
                // Images.write(bgImg,
                // Files.createDirIfNoExists("~/tmp/testBig.png"));
                gc.drawImage(bgImg, 0, 0, null);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void render() {
        for (TPassportDrawItem dItem : items) {
            drawItem(dItem);
        }
    }

    public void finish() {
        if (null != gc) {
            gc.dispose();
            gc = null;
        }
    }

    public BufferedImage getImage() {
        return im;
    }

    private void drawItem(TPassportDrawItem tpItem) {
        // 绘制二维码
        if ("qrcode".equals(tpItem.as)) {
            BufferedImage qrImage = QRCode.toQRCode(tpItem.getContent(),
                                                    QRCodeFormat.NEW()
                                                                .setSize(tpItem.width)
                                                                .setMargin(tpItem.margin));
            gc.drawImage(qrImage, tpItem.left, tpItem.top, null);
        }
        // 绘制图片
        else if ("image".equals(tpItem.as)) {
            // TODO 头像啥的, 需要缩放什么的
        }
        // 绘制文字
        else {
            // 背景
            if (!Strings.isBlank(tpItem.bgColor)) {
                gc.setColor(getBgColor(tpItem));
                gc.fillRect(tpItem.left, tpItem.top, tpItem.width, tpItem.height);
            }
            // 内容
            Font cFont = getTextFont(tpItem);
            gc.setColor(getTextColor(tpItem));
            gc.setFont(cFont);
            FontMetrics cFontM = gc.getFontMetrics(cFont);
            int cW = cFontM.stringWidth(tpItem.getContent());
            int cH = cFontM.getHeight();
            int cHFix = cH / 3 * 2;
            int x, y;
            if ("left".equals(tpItem.align)) {
                x = tpItem.left;
            } else if ("right".equals(tpItem.align)) {
                x = tpItem.left + (tpItem.width - cW);
            } else {
                x = tpItem.left + (tpItem.width / 2 - cW / 2);
            }
            y = tpItem.top
                + cHFix
                + (tpItem.height / 2 - cH / 2)
                + (cFontM.getAscent() - tpItem.fontSize);
            gc.drawString(tpItem.getContent(), x, y);
        }
    }

    private Font getTextFont(TPassportDrawItem tpItem) {
        if (tpItem.fontSize == 0) {
            tpItem.fontSize = dftFontSize;
        }
        if (tpItem.fontStyle == 0) {
            tpItem.fontStyle = dftFontStyle;
        }
        if (Strings.isBlank(tpItem.font)) {
            tpItem.font = dftFont;
        }
        return getFont(tpItem.font, tpItem.fontStyle, tpItem.fontSize);
    }

    private Color getTextColor(TPassportDrawItem tpItem) {
        if (Strings.isBlank(tpItem.fontColor)) {
            return dftFontColor;
        }
        return Colors.as(tpItem.fontColor);
    }

    private Color getBgColor(TPassportDrawItem tpItem) {
        if (Strings.isBlank(tpItem.bgColor)) {
            return dftBgColor;
        }
        return Colors.as(tpItem.bgColor);
    }

    private Font getFont(String nm, int style, int size) {
        String fkey = nm + "_" + style + "_" + size;
        Font ffont = fontCache.getAs(fkey, Font.class);
        if (ffont == null) {
            InputStream ins = null;
            try {
                String path = "font/" + nm;
                try {
                    URL url = getClass().getClassLoader().getResource(path);
                    if (url != null) {
                        if (url.getFile() != null) {
                            ffont = Font.createFont(Font.TRUETYPE_FONT, new File(url.getFile()))
                                        .deriveFont(style, size);
                        }
                    }
                }
                catch (Exception e) {}
                ins = Streams.fileIn(path);
                if (ins != null)
                    ffont = Font.createFont(Font.TRUETYPE_FONT, ins).deriveFont(style, size);
            }
            catch (Exception e) {
                // 尝试直接获取字体
                ffont = new Font(nm, style, size);
            }
            finally {
                Streams.safeClose(ins);
            }
            if (ffont != null) {
                fontCache.setv(fkey, ffont);
            }
        }
        return ffont;
    }

    private BufferedImage loadImage(String img) throws IOException {
        if (img.startsWith("file://")) {
            return ImageIO.read(Streams.fileIn(Files.findFile(img.substring(7))));
        } else {
            if (sys != null) {
                return ImageIO.read(sys.io.getInputStream(sys.io.get(img), 0));
            }
            throw new RuntimeException("Need Walnut System");
        }
    }

    private BufferedImage zoomScaleImage(BufferedImage img, int tarW, int tarH, Color bgColor) {
        int bgW = img.getWidth();
        int bgH = img.getHeight();
        if (bgW != tarW || bgH != tarH) {
            return Images.zoomScale(img, tarW, tarH, bgColor);
        }
        return img;
    }

    public static NutMap getFontCache() {
        return fontCache;
    }

    public static void setFontCache(NutMap fontCache) {
        TPassport.fontCache = fontCache;
    }

    public WnSystem getSys() {
        return sys;
    }

    public void setSys(WnSystem sys) {
        this.sys = sys;
    }

    public BufferedImage getIm() {
        return im;
    }

    public void setIm(BufferedImage im) {
        this.im = im;
    }

    public Graphics2D getGc() {
        return gc;
    }

    public void setGc(Graphics2D gc) {
        this.gc = gc;
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

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getDftFontColor() {
        return dftFontColor;
    }

    public void setDftFontColor(Color dftFontColor) {
        this.dftFontColor = dftFontColor;
    }

    public Color getDftBgColor() {
        return dftBgColor;
    }

    public void setDftBgColor(Color dftBgColor) {
        this.dftBgColor = dftBgColor;
    }

    public String getDftFont() {
        return dftFont;
    }

    public void setDftFont(String dftFont) {
        this.dftFont = dftFont;
    }

    public int getDftFontStyle() {
        return dftFontStyle;
    }

    public void setDftFontStyle(int dftFontStyle) {
        this.dftFontStyle = dftFontStyle;
    }

    public int getDftFontSize() {
        return dftFontSize;
    }

    public void setDftFontSize(int dftFontSize) {
        this.dftFontSize = dftFontSize;
    }

    public List<TPassportDrawItem> getItems() {
        return items;
    }

    public void setItems(List<TPassportDrawItem> items) {
        this.items = items;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.bgColor = Colors.as(backgroundColor);
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    
    
}