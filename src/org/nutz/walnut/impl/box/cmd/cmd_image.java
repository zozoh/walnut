package org.nutz.walnut.impl.box.cmd;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.ZType;

/**
 * 读取图片, 返回分辨率等信息.
 * 
 * @author pw
 */
public class cmd_image extends JvmExecutor {

    protected WnObj getImgObj(WnSystem sys, ZParams params) {
        List<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);
        if (list.size() <= 0) {
            sys.err.print("need a obj");
            return null;
        }
        if (list.size() > 1) {
            sys.err.print("too many objs, only handler one obj at once");
            return null;
        }
        // 默认只处理第一个
        WnObj imgObj = list.get(0);
        if (ZType.isImage(imgObj.type())) {
            return imgObj;
        } else {
            sys.err.printf("obj %s(%s) is not a image", imgObj.name(), imgObj.id());
            return null;
        }
    }

    protected NutMap getImgInfo(WnSystem sys, WnObj imgObj) {
        NutMap imgInfo = NutMap.NEW();
        BufferedImage bimg = Images.read(sys.io.getInputStream(imgObj, 0));
        imgInfo.setv("width", bimg.getWidth());
        imgInfo.setv("height", bimg.getHeight());
        return imgInfo;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        WnObj imgObj = getImgObj(sys, params);
        if (imgObj != null) {
            sys.out.println(Json.toJson(getImgInfo(sys, imgObj)));
        }
    }

}
