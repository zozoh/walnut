package org.nutz.walnut.ext.media.subtitle.impl;

import java.util.Iterator;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.walnut.ext.media.subtitle.bean.SubtitleItem;

public class SrtSubtitleItem extends SubtitleItem {

    public void duplicate(SubtitleItem si) {
        this.beginTime = si.beginTime;
        this.endTime = si.endTime;
        this.index = si.index;
        this.lines = si.cloneStdLines();
    }

    public boolean parse(int i, Iterator<String> it) {
        String line = null;
        if (!it.hasNext())
            return false;
        // 找到第一个非空行部分
        do {
            line = Strings.trim(it.next());
            if (!Strings.isEmpty(line))
                break;
        } while (it.hasNext());

        // 读取序号和时间
        while (null != line) {
            // 从序号开始
            try {
                this.index = Integer.parseInt(line);

                // 读取时间
                String times = it.next();
                int pos = times.indexOf("-->");
                String beginTm = Strings.trim(times.substring(0, pos));
                String endTm = Strings.trim(times.substring(pos + 3));
                this.beginTime = Times.Ti(beginTm);
                this.endTime = Times.Ti(endTm);

                // 嗯
                break;
            }
            // 不是序号的话，忍耐一下 ...
            catch (NumberFormatException e) {
                line = it.hasNext() ? it.next() : null;
            }
        }

        // 一直读取内，直到空行
        while (it.hasNext()) {
            line = Strings.trim(it.next());
            if (Strings.isEmpty(line))
                break;
            // TODO 暂时过滤一下样式
            this.lines.add(line.replaceAll("[{][^}]+[}]", ""));
        }

        // 标识一下，自己是否解析成功
        return this.index > 0;
    }

    public void joinText(StringBuilder sb) {
        // 输出序号
        sb.append(index).append('\n');

        // 时间
        sb.append(beginTime.toString("HH:mm:ss,SSS"));
        sb.append(" --> ");
        sb.append(endTime.toString("HH:mm:ss,SSS"));
        sb.append('\n');

        // 内容
        for (String line : lines) {
            sb.append(line).append('\n');
        }

        // 来个空行
        sb.append('\n');
    }

}
