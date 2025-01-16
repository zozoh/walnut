package com.site0.walnut.impl.box.cmd;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.nutz.lang.Strings;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.Wtime;

public class cmd_date extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 分析参数
        ZParams params = ZParams.parse(args, "n", "^(myzone|syszone)$");

        // 获取时区
        TimeZone tz;
        if (params.has("tz")) {
            tz = Wtime.toTimeZone(params.getString("tz"));
        }
        // 获取会话时区
        else {
            tz = Wtime.getSessionTimeZone(sys.session);
        }

        // 将显示这个时间
        long now;

        // 从参数里解析日期字符串
        if (params.has("d")) {
            now = Wtime.parseAnyAMS(params.get("d"));
        }
        // 从参数里解析纳秒字符串
        else if (params.has("dns")) {
            now = params.getLong("dns") / 1000000L;
        }
        // 从参数里解析毫秒字符串
        else if (params.has("dms")) {
            now = params.getLong("dms");
        }
        // 从管线里
        else if (sys.pipeId > 0) {
            String s = Strings.trim(sys.in.readAll());
            now = Wtime.parseAMS(s);
        }
        // 当前时间
        else {
            now = Wn.now();
        }

        // -fmt
        if (params.has("fmt")) {
            String fmt = params.getString("fmt");
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, fmt, tz));
        }
        // -ss
        else if (params.is("ss")) {
            sys.out.print("" + now / 1000L);
        }
        // -ms
        else if (params.is("ms")) {
            sys.out.print("" + now);
        }
        // -1ms
        else if (params.is("1ms")) {
            sys.out.print("" + (now - (now / 1000L) * 1000L));
        }
        // -full
        else if (params.is("full")) {
            Date d = new Date(now);
            sys.out.print(d.toString());
        }
        // -dt
        else if (params.is("dt")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "yyyy-MM-dd HH:mm:ss", tz));
        }
        // -dtms
        else if (params.is("dtms")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "yyyy-MM-dd HH:mm:ss.SSS", tz));
        }
        // -dtt
        else if (params.is("dtt")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "yyyy-MM-dd'T'HH:mm:ss", tz));
        }
        // -dtms
        else if (params.is("dtmst")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "yyyy-MM-dd'T'HH:mm:ss.SSS", tz));
        }
        // -time
        else if (params.is("time")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "HH:mm:ss", tz));
        }
        // -timems
        else if (params.is("timems")) {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "HH:mm:ss.SSS", tz));
        }
        // -zone
        else if (params.is("myzone")) {
            if (null == tz) {
                sys.out.print("-no-set-");
            } else {
                sys.out.printf("%s/%s", tz.getDisplayName(), tz.getRawOffset());
            }

        }
        // -syszone
        else if (params.is("syszone")) {
            Calendar c = Calendar.getInstance();
            TimeZone zo = c.getTimeZone();
            sys.out.printf("%s%s", zo.getDisplayName(), zo.getRawOffset());
        }
        // 默认
        else {
            Date d = new Date(now);
            sys.out.print(Wtime.format(d, "yy-MM-dd HH:mm:ss", tz));
        }

        if (!params.is("n"))
            sys.out.println();
    }
}
