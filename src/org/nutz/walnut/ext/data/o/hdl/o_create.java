package org.nutz.walnut.ext.data.o.hdl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class o_create extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(keep|auto|upsert)$");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        WnObj oP;
        // 首先确认一下父目录
        String pph = params.getString("p");
        if (!Strings.isBlank(pph)) {
            String apph = Wn.normalizeFullPath(pph, sys);
            // 自动创建
            if (params.is("auto")) {
                oP = sys.io.createIfNoExists(null, apph, WnRace.DIR);
            }
            // 确保存在
            else {
                oP = sys.io.check(null, apph);
            }
        }
        // 如果上下文里有对象，采用第一个对象
        else if (!fc.list.isEmpty()) {
            oP = fc.list.get(0);
        }
        // 采用当前目录
        else {
            oP = sys.getCurrentObj();
        }

        // 确保是目录
        if (oP.isFILE()) {
            oP = oP.parent();
        }

        // 默认种族
        WnRace race = params.getEnum("race", WnRace.class);
        if (null == race) {
            race = WnRace.FILE;
        }

        // 准备创建列表
        List<Object> list;

        // 从参数中指定创建对象
        if (params.vals.length > 0) {
            list = new ArrayList<>(params.vals.length);
            for (String str : params.vals) {
                if (Strings.isQuoteBy(str, '{', '}')) {
                    list.add(Json.fromJson(str));
                } else {
                    list.add(str);
                }
            }
        }
        // 从标准输入读取
        else {
            String json = fc.sys.in.readLine();
            Object input = Json.fromJson(json);
            if (input instanceof Collection<?>) {
                Collection<?> col = (Collection<?>) input;
                list = new ArrayList<>(col.size());
                list.addAll(col);
            } else {
                list = new ArrayList<>(1);
                list.add(input);
            }
        }

        // 参数神码都没有，就读取标准输入
        if (list.isEmpty()) {
            String json = Strings.trim(sys.in.readAll());
            Object input = Json.fromJson(json);
            if (null != input) {
                // 集合
                if (input instanceof Collection<?>) {
                    list.addAll((Collection<Object>) input);
                }
                // 数组
                else if (input.getClass().isArray()) {
                    int len = Array.getLength(input);
                    for (int i = 0; i < len; i++) {
                        Object ele = Array.get(input, i);
                        if (null != ele) {
                            list.add(ele);
                        }
                    }
                }
                // 其他
                else {
                    list.add(input);
                }
            }
        }

        // 清空上下文
        if (!params.is("keep")) {
            fc.clearAll();
        }

        // 然后依次创建对象，并加入到上下文
        boolean isUpsert = params.is("upsert");
        for (Object li : list) {
            // 字符串的话，作为文件名
            if (li instanceof CharSequence) {
                String fname = li.toString();
                WnObj o;
                if (isUpsert) {
                    o = sys.io.createIfNoExists(oP, fname, race);
                } else {
                    o = sys.io.create(oP, fname, race);
                }
                fc.add(o);
            }
            // 否则作为 Map
            else if (li instanceof Map<?, ?>) {
                NutMap meta = NutMap.WRAP((Map<String, Object>) li);
                WnObj obj = new WnIoObj();
                obj.putAll(meta);
                obj.putDefault("race", race);
                WnObj o;
                if (isUpsert) {
                    o = sys.io.createIfNoExists(oP, obj);
                } else {
                    o = sys.io.create(oP, obj);
                }
                fc.add(o);
            }
            // 其他就无视
        }
    }

}
