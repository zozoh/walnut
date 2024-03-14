package com.site0.walnut.ext.net.aliyun.vod.hdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.aliyun.sdk.WnAliyunMediaQuery;
import com.site0.walnut.ext.net.aliyun.sdk.WnAliyuns;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodConf;
import com.site0.walnut.ext.net.aliyun.vod.WnAliyunVodService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.WnPager;

import com.aliyuncs.vod.model.v20170321.SearchMediaResponse;
import com.aliyuncs.vod.model.v20170321.SearchMediaResponse.Media;

@JvmHdlParamArgs("cqn")
public class aliyunvod_search implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 得到配置对象
        WnAliyunVodConf conf = WnAliyuns.getConf(hc, WnAliyunVodConf.class);

        // 准备服务类
        WnAliyunVodService vods = new WnAliyunVodService(conf);

        // 准备翻页参数
        int pn = hc.params.getInt("pn", 1);
        int pgsz = hc.params.getInt("pgsz", 20);
        String scroll = hc.params.getString("scroll", null);

        // -------------------------------------------------------
        // 准备查询对象
        WnAliyunMediaQuery mq = new WnAliyunMediaQuery();
        mq.setFeilds(hc.params.getString("fields"));
        mq.setMatch(hc.params.getString("match"));
        mq.setPageNo(pn);
        mq.setPageSize(pgsz);
        mq.setSortBy(hc.params.getString("sort"));
        mq.setSearchType(hc.params.val(0, "video"));
        mq.setScrollToken(scroll);

        // 得到查询结果
        SearchMediaResponse resp = vods.searchMedia(mq);
        scroll = resp.getScrollToken();
        // -------------------------------------------------------
        // 整理成标准 Walnut 对象格式
        String as = hc.params.getString("as", "raw");
        if ("page".equals(as)) {
            // 汇总媒体列表
            List<String> rmks = new ArrayList<>();
            List<NutMap> list = new ArrayList<>();
            for (Media media : resp.getMediaList()) {
                NutMap obj = Lang.obj2map(media.getVideo(), NutMap.class);
                obj.put("mediaId", media.getMediaId());
                obj.put("mediaType", media.getMediaType());
                obj.putDefault("creationTime", media.getCreationTime());
                // 移除空对象
                rmks.clear();
                for (Map.Entry<String, Object> en : obj.entrySet()) {
                    String key = en.getKey();
                    Object val = en.getValue();
                    // 无值
                    if (null == val) {
                        rmks.add(key);
                    }
                    // 空集合
                    else if (val instanceof Collection<?>) {
                        if (((Collection<?>) val).isEmpty()) {
                            rmks.add(key);
                        }
                    }
                }
                for (String k : rmks) {
                    obj.remove(k);
                }
                // 汇入列表
                list.add(obj);
            }

            // 计算翻页器
            long total = resp.getTotal();
            WnPager wp = new WnPager(pgsz, (pn - 1) * pgsz);
            wp.setSumCount(total);

            // 准备输出 Map
            NutMap out = Cmds.createQueryResult(wp, list);
            if (!Strings.isBlank(scroll)) {
                out.put("scrollToken", scroll);
            }
            hc.output = out;
        }
        // -------------------------------------------------------
        // 输出原始结果
        else {
            hc.output = resp;
        }
    }

}
