package org.nutz.walnut.impl.box.cmd;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.ZType;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

/**
 * 读取图片, 返回分辨率等信息.
 * 
 * @author pw
 */
public class cmd_image extends JvmExecutor {

    protected NutMap getImgInfo(WnSystem sys, WnObj imgObj, boolean moreInfo) {
        NutMap imgInfo = NutMap.NEW();
        BufferedImage bimg = Images.read(sys.io.getInputStream(imgObj, 0));
        imgInfo.setv("width", bimg.getWidth());
        imgInfo.setv("height", bimg.getHeight());
        imgInfo.setv("type", bimg.getType());

        switch (bimg.getType()) {
        case BufferedImage.TYPE_INT_RGB:
            imgInfo.setv("typeName", "INT_RGB");
            break;
        case BufferedImage.TYPE_INT_ARGB:
            imgInfo.setv("typeName", "INT_ARGB");
            break;
        case BufferedImage.TYPE_INT_ARGB_PRE:
            imgInfo.setv("typeName", "INT_ARGB_PRE");
            break;
        case BufferedImage.TYPE_INT_BGR:
            imgInfo.setv("typeName", "INT_BGR");
            break;
        case BufferedImage.TYPE_3BYTE_BGR:
            imgInfo.setv("typeName", "3BYTE_BGR");
            break;
        case BufferedImage.TYPE_4BYTE_ABGR:
            imgInfo.setv("typeName", "4BYTE_ABGR");
            break;
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            imgInfo.setv("typeName", "4BYTE_ABGR_PRE");
            break;
        case BufferedImage.TYPE_BYTE_GRAY:
            imgInfo.setv("typeName", "BYTE_GRAY");
            break;
        case BufferedImage.TYPE_USHORT_GRAY:
            imgInfo.setv("typeName", "USHORT_GRAY");
            break;
        case BufferedImage.TYPE_BYTE_BINARY:
            imgInfo.setv("typeName", "BYTE_BINARY");
            break;
        case BufferedImage.TYPE_BYTE_INDEXED:
            imgInfo.setv("typeName", "BYTE_INDEXED");
            break;
        case BufferedImage.TYPE_USHORT_565_RGB:
            imgInfo.setv("typeName", "USHORT_565_RGB");
            break;
        case BufferedImage.TYPE_USHORT_555_RGB:
            imgInfo.setv("typeName", "USHORT_555_RGB");
            break;
        default:
            imgInfo.setv("typeName", "???");
        }

        // 更多信息
        if (moreInfo) {
            NutMap exMap = NutMap.NEW();
            imgInfo.setv("extra", exMap);
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(sys.io.getInputStream(imgObj,
                                                                                           0));
                if (metadata != null) {
                    for (Directory directory : metadata.getDirectories()) {
                        for (Tag tag : directory.getTags()) {
                            String dname = directory.getName();
                            NutMap dMap = (NutMap) exMap.get(dname, NutMap.NEW());
                            dMap.setv(tag.getTagName(), tag.getDescription());
                            exMap.setv(dname, dMap);
                        }
                        if (directory.hasErrors()) {
                            for (String error : directory.getErrors()) {
                                sys.err.printlnf("exif error: %s", error);
                            }
                        }
                    }
                }
            }
            catch (ImageProcessingException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgInfo;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        WnObj imgObj = getObj(sys, args);
        if (imgObj == null) {
            return;
        }
        if (!ZType.isImage(imgObj.type())) {
            sys.err.printf("obj %s(%s) is not a image", imgObj.name(), imgObj.id());
        } else {
            sys.out.println(Json.toJson(getImgInfo(sys, imgObj, params.is("more", false))));
        }
    }

}
