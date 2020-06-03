package org.nutz.walnut.ext.entity;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.redis.Wedis;
import org.nutz.walnut.ext.redis.WedisConfig;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.ajax.Ajax;

public abstract class JvmRedisEntityExecutor<T> extends JvmExecutor {

    protected Class<T> myEntityType;

    public JvmRedisEntityExecutor() {
        this.myEntityType = Mirror.getTypeParam(this.getClass(), 0);
    }

    protected WedisConfig prepareConfig(WnSystem sys, ZParams params, String typeName) {
        String nmConf = params.get("conf", "_" + typeName);
        if (!nmConf.endsWith(".json")) {
            nmConf += ".json";
        }
        String phConf = "~/.domain/" + typeName + "/" + nmConf;
        return Wedis.loadConfig(sys, phConf);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void output(WnSystem sys, ZParams params, Object re, RedisEntityPrinter<T> printer) {
        boolean asJson = params.is("json");
        if (params.is("ajax")) {
            re = Ajax.ok().setData(re);
            asJson = true;
        }

        if (asJson) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(re, jfmt));
        }
        // 输出Map
        else if (re instanceof Map) {
            NutMap map = NutMap.WRAP((Map) re);
            int i = 0;
            for (Map.Entry<String, Object> en : map.entrySet()) {
                sys.out.printlnf("%d) %s : %s", i++, en.getKey(), en.getValue());
            }
        }
        // 输出集合
        else if (re instanceof Collection<?>) {
            Collection<T> co = (Collection<T>) re;
            if (co.isEmpty()) {
                sys.out.println("(~nil~)");
            } else {
                int skipI = params.getInt("skip", 0);
                int i = params.getInt("i", 1) + skipI;
                for (T it : co) {
                    printer.print(it, i++);
                }
            }
        }
        // 输出数组
        else if (re.getClass().isArray()) {
            Object[] objs = (Object[]) re;
            T[] ary = (T[]) Array.newInstance(this.myEntityType, objs.length);
            System.arraycopy(objs, 0, ary, 0, objs.length);
            if (ary.length == 0) {
                sys.out.println("(~nil~)");
            } else {
                int skipI = params.getInt("skip", 0);
                int i = params.getInt("i", 1) + skipI;
                for (T it : ary) {
                    printer.print(it, i++);
                }
            }
        }
        // 直接输出把
        else {
            sys.out.println(re.toString());
        }
    }

}
