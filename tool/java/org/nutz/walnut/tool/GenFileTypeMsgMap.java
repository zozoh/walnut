package org.nutz.walnut.tool;

import java.util.Map;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class GenFileTypeMsgMap {

    public static void main(String[] args) {
        // 找到原来声明的文件类型
        String phFtypeJs = args.length > 0 ? args[0]
                                           : "~/workspace/git/github/walnut/ROOT/rs/core/js/ui/i18n/zh-cn.js";
        String text = Files.read(phFtypeJs);
        if (text.startsWith("define")) {
            text = text.substring("define".length());
        }
        NutMap map = Json.fromJson(NutMap.class, text);
        NutMap fmimes = map.getAs("fmime", NutMap.class);

        // 找到文件类型的全集
        PropertiesProxy pp = new PropertiesProxy("mime.properties");

        // 分类
        NutMap cates = new NutMap();
        cates.put("application", "应用");
        cates.put("audio", "音频");
        cates.put("image", "图像");
        cates.put("video", "视频");
        cates.put("text", "文本");
        cates.put("drawing", "绘图");
        cates.put("message", "消息");
        cates.put("x-world", "3D模型");

        for (Map.Entry<String, String> en : pp.entrySet()) {
            // String key = en.getKey();
            String val = en.getValue();
            String mime = val.replaceAll("[/-]", "_");
            if (!fmimes.containsKey(mime)) {
                String[] ss = val.split("/");
                String c0 = ss[0];
                String c1 = ss[1];
                String cnm = cates.getString(c0);
                fmimes.put(mime, Strings.upperFirst(c1) + " " + cnm);
            }
        }

        System.out.println(Json.toJson(fmimes));
    }

}
