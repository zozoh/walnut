package org.nutz.walnut.ext.sys.task.hdl;

import java.util.Date;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sys.task.TaskCtx;
import org.nutz.walnut.ext.sys.task.TaskStatus;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 命令格式
 * 
 * <pre>
   task 3acd.. update '任务标题'
        -lbls    "标签A,标签B" 
        -status  "DONE | NEW" 
        -ow      "zozoh"
        -d_start "2015-07-21 13:33:32"
        -d_stop  "2015-07-21 14:33:32"
        -du      3600000
        -done    "2015..004"
        -verify  "2015..781"
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Th_update extends AbstractTaskModifyHdl {

    @Override
    public void invoke(WnSystem sys, TaskCtx sc) throws Exception {
        if (null == sc.oTask) {
            throw Er.create("e.cmd.task.empty");
        }

        ZParams params = ZParams.parse(sc.args, "v");

        StringBuilder sb = new StringBuilder();

        // 标题
        if (params.vals.length > 0) {
            sc.oTask.setv("title", params.vals[0]);
            sb.append("^title");
        }

        // -lbls "标签A,标签B"
        if (params.has("lbls")) {
            String[] lbls = Strings.splitIgnoreBlank(params.get("lbls"));
            sc.oTask.labels(lbls);
            sb.append(sb.length() == 0 ? "^" : "|").append("lbls");
        }
        // -status "DONE|NEW"
        if (params.has("status")) {
            TaskStatus status = params.getEnum("status", TaskStatus.class);
            sc.oTask.setv("status", status);
            sb.append(sb.length() == 0 ? "^" : "|").append("status");
        }
        // -ow "zozoh"
        if (params.has("ow")) {
            sc.oTask.setv("ow", params.get("ow"));
            sb.append(sb.length() == 0 ? "^" : "|").append("ow");
        }
        // -d_start "2015-07-21 13:33:32"
        if (params.has("d_start")) {
            Date t = Times.D(params.get("d_start"));
            sc.oTask.setv("d_start", t.getTime());
            sb.append(sb.length() == 0 ? "^" : "|").append("d_start");
        }
        // -d_stop "2015-07-21 14:33:32"
        if (params.has("d_stop")) {
            Date t = Times.D(params.get("d_stop"));
            sc.oTask.setv("d_stop", t.getTime());
            sb.append(sb.length() == 0 ? "^" : "|").append("d_stop");
        }
        // -du 3600000
        if (params.has("du")) {
            long ms = params.getLong("du");
            sc.oTask.setv("du", ms);
            sb.append(sb.length() == 0 ? "^" : "|").append("du");
        }
        // -done "2015..004"
        if (params.has("done")) {
            String cnm = params.get("done");
            sys.io.check(sc.oTask, "comments/" + cnm + ".md");
            sc.oTask.setv("done", cnm);
            sb.append(sb.length() == 0 ? "^" : "|").append("done");
        }
        // -verify "2015..781"
        if (params.has("verify")) {
            String cnm = params.get("verify");
            sys.io.check(sc.oTask, "comments/" + cnm + ".md");
            sc.oTask.setv("verify", cnm);
            sb.append(sb.length() == 0 ? "^" : "|").append("verify");
        }

        // 最后看看是不是要更新
        if (sb.length() > 0) {
            sb.append('$');
            sys.io.appendMeta(sc.oTask, sb.toString());
        }

        // 完毕
        this._done(sys, params, sc.oTask);
    }

}
