package org.nutz.walnut.ext.net.aliyun.vod.hdl;

import org.nutz.walnut.ext.net.aliyun.sdk.WnAliyuns;
import org.nutz.walnut.ext.net.aliyun.vod.WnAliyunVodConf;
import org.nutz.walnut.ext.net.aliyun.vod.WnAliyunVodService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class aliyunvod_playinfo implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到配置对象
        WnAliyunVodConf conf = WnAliyuns.getConf(hc, WnAliyunVodConf.class);

        // 获取视频 ID
        String videoId = hc.params.val_check(0);

        // 准备服务类
        WnAliyunVodService vods = new WnAliyunVodService(conf);

        // 输出结果
        hc.output = vods.getPlayInfo(videoId);
    }

}
