package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Streams;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_json extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "cqn");

        // 读取输入
        String json = Streams.read(sys.in.getReader()).toString();

        // 格式化
        Object obj = Json.fromJson(json);

        // 过滤字段
        JsonFormat fmt;
        if (params.is("c"))
            fmt = JsonFormat.compact();
        else
            fmt = JsonFormat.forLook();

        fmt.setQuoteName(params.is("q"));
        fmt.setIgnoreNull(params.is("n"));

        if (params.has("e")) {
            String regex = params.get("e");
            if (regex.startsWith("!")) {
                fmt.setLocked(regex.substring(1));
            } else {
                fmt.setActived(regex);
            }
        }

        if (params.has("d")) {
            fmt.setDateFormat(params.get("d"));
        }

        // 最后输出
        sys.out.println(Json.toJson(obj, fmt));

    }

}
