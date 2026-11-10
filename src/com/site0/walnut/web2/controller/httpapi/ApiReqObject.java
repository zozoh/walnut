package com.site0.walnut.web2.controller.httpapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Streams;
import org.nutz.lang.stream.ComboOutputStream;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnStr;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

/**
 * JETTY-09：照抄旧 {@code HttpApiModule} 的请求对象方法组：
 * {@code __gen_tmp_obj}（393-395）、{@code __gen_req_obj}（511-605，
 * {@code http-body} text|form|json + {@code http-post-json-merge} +
 * {@code ComboOutputStream} 双写）、{@code __find_post_json_merge_update}（607-630）。
 */
public class ApiReqObject {

    private final WnIo io;

    public ApiReqObject(WnIo io) {
        this.io = io;
    }

    public WnObj genTmpObj(WnHttpApiContext apc) {
        return io.createIfNoExists(apc.oHome, ".regapi/tmp", WnRace.DIR);
    }

    @SuppressWarnings("unchecked")
    public WnObj genReqObj(WnHttpApiContext apc)
            throws UnsupportedEncodingException, IOException {

        // .........................................
        // 创建临时文件以便保存请求的内容
        String uriName = apc.uri.replaceAll("[/\\\\]", "_").substring(1);
        WnObj oReq = io.create(apc.oTmp, uriName + "_${id}", WnRace.FILE);

        // .........................................
        // 更新头信息
        io.appendMeta(oReq, apc.reqMeta);

        // 增加一个输入缓冲
        String in_tp = apc.oApi.getString("http-body");
        StringBuilder in_sb = new StringBuilder();
        OutputStream in_ops = null;
        if (null != in_tp && in_tp.matches("^(text|form|json)$")) {
            in_ops = Wlang.ops(in_sb);
        }

        // .........................................
        // 看看是否声明了有效的 http-post-json-merge
        NutMap jsonUpdate = null;
        boolean needUpdateBody = false;
        if ("json".equals(in_tp)) {
            NutMap juMap = this.__find_post_json_merge_update(apc);
            if (null != juMap) {
                jsonUpdate = (NutMap) Wn.explainObj(apc.reqMeta, juMap);
                needUpdateBody = (null != jsonUpdate && !jsonUpdate.isEmpty());
            }
        }

        // .........................................
        // 保存请求体
        InputStream ins = apc.req.getInputStream();
        OutputStream ops;

        // 嗯？ 需要动态更新一下内容，那么就先读取吧
        if (needUpdateBody) {
            ops = in_ops;
        }
        // 需要同时将请求内容保存到元数据里，那么先复制一份把
        else if (null != in_ops) {
            OutputStream ioOps = io.getOutputStream(oReq, 0);
            ops = new ComboOutputStream(ioOps, in_ops);
        }
        // 直接写咯
        else {
            ops = io.getOutputStream(oReq, 0);
        }

        // 写入流
        try {
            Streams.write(ops, ins);
            Streams.safeFlush(ops);
        }
        finally {
            Streams.safeClose(ops);
        }

        // .........................................
        // 计入元数据！注意！这个 http-body 是不持久化的，否则有可能太大
        if (in_sb.length() > 0) {
            // 纯文本
            if ("text".equals(in_tp)) {
                oReq.put("body", in_sb.toString());
            }
            // 表单
            else if ("form".equals(in_tp)) {
                NutMap map = WnStr.parseFormData(in_sb.toString());
                oReq.put("body", map);
            }
            // JSON
            else if ("json".equals(in_tp)) {
                Object body = Json.fromJson(in_sb);
                // 更新一下 json，进入了这个分支，意味着请求对象的 body并未被写入
                // 因为内容需要被 update， 等合并完，需要补写一下。
                if (needUpdateBody) {
                    // 安全起见，只有 Map 才能融合 Map
                    if (body instanceof Map) {
                        NutMap map = NutMap.WRAP((Map<String, Object>) body);
                        map.mergeWith(jsonUpdate);
                        body = map;
                    }
                    // 写入
                    String json = Json.toJson(body);
                    io.writeText(oReq, json);
                }
                oReq.put("body", body);
            }
        }
        // .........................................
        // 搞定
        return oReq;
    }

    private NutMap __find_post_json_merge_update(WnHttpApiContext apc) {
        List<NutMap> list = apc.oApi.getAsList("http-post-json-merge", NutMap.class);
        if (null == list || list.isEmpty()) {
            return null;
        }
        // 逐个判断
        for (NutMap jm : list) {
            Object test = jm.get("test");
            // 需要判断
            if (null != test) {
                WnMatch m = new AutoMatch(test);
                if (!m.match(apc.reqMeta)) {
                    continue;
                }
            }
            // 嗯就是这个条件咯
            NutMap update = jm.getAs("update", NutMap.class);
            if (null == update || update.isEmpty()) {
                return null;
            }
            return update;
        }
        return null;
    }

}
