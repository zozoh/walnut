package org.nutz.walnut.impl.box.cmd;

import java.util.Date;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Times;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

/**
 * 首先从 "-s" 里读，如果没有，从 pipe 里读，还没有则用当前系统时间
 * 
 * <pre>
 *  -full    全部
 *  -dt      显示日期时间到秒
 *  -dtms    显示日期时间到毫秒
 *  -time    显示时间字符串到秒
 *  -timems  显示时间字符串到毫秒
 *  -ss      显示成秒数
 *  -ms      显示成毫秒数
 *  -fmt     日期字符串格式化
 *  -d       输入的日期字符串
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
public class cmd_date extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 分析参数
        ZParams params = ZParams.parse(args, "f");

        Date d;
        // 得到原始时间
        // 从参数里
        if (params.has("d")) {
            d = Times.D(params.get("d"));
        }
        // 从管线里
        else if (null != sys.in) {
            String s = sys.in.readAll();
            d = Times.D(s);
        }
        // 当前时间
        else {
            d = Times.now();
        }

        // -fmt
        if (params.has("fmt")) {
            sys.out.writeLine(Times.format(params.get("fmt"), d));
        }
        // -ss
        else if (params.is("ss")) {
            sys.out.writeLine("" + d.getTime() / 1000);
        }
        // -ms
        else if (params.is("ms")) {
            sys.out.writeLine("" + d.getTime());
        }
        // -full
        else if (params.is("full")) {
            sys.out.writeLine(d.toString());
        }
        // -dt
        else if (params.is("dt")) {
            sys.out.writeLine(Times.format("yyyy-MM-dd HH:mm:ss", d));
        }
        // -dtms
        else if (params.is("dtms")) {
            sys.out.writeLine(Times.format("yyyy-MM-dd HH:mm:ss.SSS", d));
        }
        // -time
        else if (params.is("time")) {
            sys.out.writeLine(Times.format("HH:mm:ss", d));
        }
        // -timems
        else if (params.is("timems")) {
            sys.out.writeLine(Times.format("HH:mm:ss.SSS", d));
        }
        // 默认
        else {
            sys.out.writeLine(Times.format("yy-MM-dd HH:mm:ss", d));
        }
    }
}
