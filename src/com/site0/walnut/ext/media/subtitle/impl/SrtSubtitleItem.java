package com.site0.walnut.ext.media.subtitle.impl;

import java.util.Iterator;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtimes;
import com.site0.walnut.ext.media.subtitle.bean.SubtitleItem;

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
            line = Ws.trim(it.next());
            if (!Ws.isEmpty(line))
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
                String beginTm = Ws.trim(times.substring(0, pos));
                String endTm = Ws.trim(times.substring(pos + 3));
                this.beginTime = Wtimes.Ti(beginTm);
                this.endTime = Wtimes.Ti(endTm);

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
            line = Ws.trim(it.next());
            if (Ws.isEmpty(line))
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
