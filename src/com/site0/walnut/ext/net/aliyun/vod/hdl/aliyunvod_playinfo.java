package com.site0.walnut.ext.net.aliyun.vod.hdl;

import com.site0.walnut.ext.net.aliyun.sdk.WnAliyuns;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodConf;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
