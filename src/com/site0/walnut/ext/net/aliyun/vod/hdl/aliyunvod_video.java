package com.site0.walnut.ext.net.aliyun.vod.hdl;

import org.nutz.lang.Strings;
import com.site0.walnut.ext.net.aliyun.sdk.WnAliyuns;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodConf;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class aliyunvod_video implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到配置对象
        WnAliyunVodConf conf = WnAliyuns.getConf(hc, WnAliyunVodConf.class);

        // 准备服务类
        WnAliyunVodService vods = new WnAliyunVodService(conf);

        // 获取视频 ID
        String videoId = hc.params.val_check(0);

        // 输出结果：多个视频
        if (hc.params.vals.length > 1) {
            String vids = Strings.join(",", hc.params.vals);
            hc.output = vods.getVideoInfos(vids).getVideoList();
        }
        // 输出结果：单个视频
        else {
            hc.output = vods.getVideoInfo(videoId).getVideo();
        }
    }

}
