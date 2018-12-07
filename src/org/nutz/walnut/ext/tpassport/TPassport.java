package org.nutz.walnut.ext.tpassport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.qrcode.QRCode;
import org.nutz.qrcode.QRCodeFormat;
import org.nutz.repo.cache.simple.LRUCache;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public class TPassport {

    private static LRUCache<String, Font> fontCache = new LRUCache<>(128);
    private static LRUCache<String, BufferedImage> bgCache = new LRUCache<>(128);
    private static final Log log = Logs.get();

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
        im = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
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
        Font ffont = fontCache.get(fkey);
        if (ffont == null) {
            WnObj wobj = sys.io.fetch(null, "/home/" + sys.me.name() + "/.font/" + nm);
            if (wobj != null && wobj.isFILE()) {
                try (InputStream ins = sys.io.getInputStream(wobj, 0)) {
                    ffont = Font.createFont(Font.TRUETYPE_FONT, ins).deriveFont(style, size);
                }
                catch (IOException | FontFormatException e) {
                    log.info("bad font data: " + wobj.path() , e);
                }
            }
            if (ffont != null) {
                fontCache.put(fkey, ffont);
            }
            else {
                ffont = new Font(nm, style, size);
            }
        }
        return ffont;
    }

    private BufferedImage loadImage(String img) throws IOException {
        if (img.startsWith("file://")) {
            return ImageIO.read(Streams.fileIn(Files.findFile(img.substring(7))));
        } else {
            if (sys != null) {
                WnObj wobj = sys.io.get(img);
                if (wobj == null)
                    throw new IOException("no such image : " + img);
                BufferedImage image = bgCache.get(wobj.sha1());
                if (image == null) {
                    image = sys.io.readImage(wobj);
                    bgCache.put(wobj.sha1(), image);
                }
                return image;
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
