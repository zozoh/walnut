package org.nutz.walnut.ext.qrcode;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.nutz.img.Images;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class cmd_qrcode extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (args.length == 0) {
            sys.err.println(this.getManual());
            return;
        }
        ZParams params = ZParams.parse(args, "dp");
        boolean decode = params.is("d");
        if (decode) {
            _decode(sys, params);
        } else {
            _encode(sys, params);
        }
    }

    public void _encode(WnSystem sys, ZParams params) {
        if (params.vals.length == 0) { // TODO 支持从输入流读取
            sys.err.println(this.getManual());
            return;
        }
        int size = params.getInt("size", 256);
        // 当前仅支持输入字符串
        String content = params.vals[0];
        
        
        BitMatrix matrix = null;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            matrix = new QRCodeWriter().encode(content,
                                               BarcodeFormat.QR_CODE,
                                               size,
                                               size,
                                               hints);
        }
        catch (WriterException e) {
            throw new RuntimeException(e);
        }

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int fgColor = Color.BLACK.getRGB();
        int bgColor = Color.WHITE.getRGB();
        if (params.vals.length > 1) {
            BufferedImage image = new BufferedImage(width, height, ColorSpace.TYPE_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? fgColor : bgColor);
                }
            }
            WnObj f = sys.io.create(null, params.vals[1], WnRace.FILE);
            try {
                ImageIO.write(image, "png", sys.io.getOutputStream(f, 0));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream(width * height);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    out.write(matrix.get(x, y) ? '@' : ' ');
                }
                out.write('\n');
            }
            sys.out.write(out.toByteArray());
        }
    }

    public void _decode(WnSystem sys, ZParams params) {
        if (params.vals.length == 0) { // TODO 支持从输入流读取
            sys.err.println(this.getManual());
            return;
        }
        WnObj image = sys.io.check(null, params.vals[0]);
        InputStream ins = sys.io.getInputStream(image, 0);
        BufferedImage img = Images.read(ins);
        Streams.safeClose(ins);
        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new QRCodeReader().decode(bitmap);
            String content = result.getText();
            sys.out.print(content);
            return;
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (ChecksumException e) {
            throw new RuntimeException(e);
        }
        catch (FormatException e) {
            throw new RuntimeException(e);
        }
    }
}
