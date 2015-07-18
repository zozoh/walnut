package org.nutz.walnut.impl.box.cmd;

import java.awt.image.BufferedImage;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZType;

/**
 * 读取图片, 返回分辨率等信息.
 * 
 * @author pw
 */
public class cmd_image extends JvmExecutor {

    protected NutMap getImgInfo(WnSystem sys, WnObj imgObj) {
        NutMap imgInfo = NutMap.NEW();
        BufferedImage bimg = Images.read(sys.io.getInputStream(imgObj, 0));
        imgInfo.setv("width", bimg.getWidth());
        imgInfo.setv("height", bimg.getHeight());
        return imgInfo;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        WnObj imgObj = getObj(sys, args);
        if (imgObj == null) {
            return;
        }
        if (!ZType.isImage(imgObj.type())) {
            sys.err.printf("obj %s(%s) is not a image", imgObj.name(), imgObj.id());
        } else {
            sys.out.println(Json.toJson(getImgInfo(sys, imgObj)));
        }
    }

}
