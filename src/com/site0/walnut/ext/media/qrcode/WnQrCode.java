package com.site0.walnut.ext.media.qrcode;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Ws;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class WnQrCode {

    private String text;

    private int margin;

    private int width;

    private int height;

    private Image icon;

    private int iconWidth;

    private int iconHeight;

    private String charset;

    private WnQrCodeCorrection correction;

    public WnQrCode() {
        this(null, 1, 256, 256);
    }

    public WnQrCode(BufferedImage im) {
        this(null, 1, -1, -1);
        this.fromImage(im);
    }

    public WnQrCode(InputStream ins) throws IOException {
        this(null, 1, -1, -1);
        this.fromStream(ins);
    }

    public WnQrCode(String content) {
        this(content, 1, 256, 256);
    }

    public WnQrCode(String content, int margin, int width, int height) {
        this.text = content;
        this.margin = margin;
        this.charset = "utf-8";
        this.correction = WnQrCodeCorrection.Q;
        this.setSize(width, height);
    }

    public WnQrCode fromStream(InputStream ins) throws IOException {
        try {
            BufferedImage im = ImageIO.read(ins);
            return this.fromImage(im);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public WnQrCode fromImage(BufferedImage im) {
        QRCodeReader qr = new QRCodeReader();
        this.setSize(im.getWidth(), im.getHeight());
        BufferedImageLuminanceSource imr = new BufferedImageLuminanceSource(im);
        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(imr));
        try {
            Result re = qr.decode(bb);
            this.text = re.getText();
        }
        catch (NotFoundException | ChecksumException | FormatException e) {
            throw Er.wrap(e);
        }
        return this;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = width;
        this.iconWidth = this.width / 6;
        this.iconHeight = this.height / 6;
    }

    BitMatrix toMatrix() {
        //
        // 得到纠错级别
        //
        ErrorCorrectionLevel el = ErrorCorrectionLevel.Q;
        switch (correction) {
        case L:
            el = ErrorCorrectionLevel.L;
            break;
        case M:
            el = ErrorCorrectionLevel.M;
            break;
        case Q:
            el = ErrorCorrectionLevel.Q;
            break;
        case H:
            el = ErrorCorrectionLevel.H;
            break;
        default:
            el = ErrorCorrectionLevel.Q;
        }
        //
        // 输出内容
        //
        try {
            QRCodeWriter qrw = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, el);
            hints.put(EncodeHintType.CHARACTER_SET, charset);
            hints.put(EncodeHintType.MARGIN, margin);
            return qrw.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        }
        catch (WriterException e) {
            throw Er.wrap(e);
        }
    }

    public BufferedImage toImage() {
        BitMatrix bm = this.toMatrix();
        BufferedImage im = MatrixToImageWriter.toBufferedImage(bm);

        // 绘制中间图标
        if (this.hasIcon()) {
            BufferedImage im2 = new BufferedImage(im.getWidth(),
                                                  im.getHeight(),
                                                  BufferedImage.TYPE_INT_BGR);
            Graphics2D g2d = (Graphics2D) im2.getGraphics();

            // 绘制原始图像
            g2d.drawImage(im, 0, 0, null);

            int x = (width - iconWidth) / 2;
            int y = (height - iconHeight) / 2;

            int iw = this.iconWidth;
            int ih = this.iconHeight;

            g2d.drawImage(this.icon, x, y, iw, ih, null);

            // 切换到新的图像
            im = im2;
        }

        // 搞定
        return im;
    }

    public void writeTo(OutputStream ops, String fmt) throws IOException {
        BufferedImage im = this.toImage();
        ImageIO.write(im, fmt, ops);
    }

    public void writeAndClose(OutputStream ops, String fmt) {
        BufferedImage im = this.toImage();
        try {
            ImageIO.write(im, fmt, ops);
        }
        catch (IOException e) {
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(ops);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String content) {
        this.text = content;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void asCorrectionL() {
        this.correction = WnQrCodeCorrection.L;
    }

    public void asCorrectionM() {
        this.correction = WnQrCodeCorrection.M;
    }

    public void asCorrectionQ() {
        this.correction = WnQrCodeCorrection.Q;
    }

    public void asCorrectionH() {
        this.correction = WnQrCodeCorrection.H;
    }

    public WnQrCodeCorrection getCorrection() {
        return correction;
    }

    public void setCorrection(WnQrCodeCorrection correction) {
        this.correction = correction;
    }

    public void setCorrection(String level) {
        if (Ws.isBlank(level)) {
            this.correction = WnQrCodeCorrection.Q;
        } else {
            String ec = level.toUpperCase();
            this.correction = WnQrCodeCorrection.valueOf(ec);
        }
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

    public boolean hasIcon() {
        return null != icon;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

}
