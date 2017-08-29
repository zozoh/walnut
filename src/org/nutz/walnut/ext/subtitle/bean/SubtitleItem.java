package org.nutz.walnut.ext.subtitle.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Times;

public abstract class SubtitleItem implements Comparable<SubtitleItem> {

    public int index;

    public Times.TmInfo beginTime;

    public Times.TmInfo endTime;

    public List<String> lines;

    public SubtitleItem() {
        lines = new LinkedList<>();
    }

    public List<String> cloneStdLines() {
        List<String> ls = new ArrayList<>(lines.size());
        ls.addAll(lines);
        return ls;
    }

    public String line(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= lines.size())
            return null;
        return lines.get(lineIndex);
    }

    public SubtitleItem setLine(int lineIndex, String str) {
        lines.set(lineIndex, str);
        return this;
    }

    public SubtitleItem setLines(String[] list) {
        lines.clear();
        for (String line : list)
            lines.add(line);
        return this;
    }

    @Override
    public int compareTo(SubtitleItem sto) {
        return this.beginTime.valueInMillisecond - sto.beginTime.valueInMillisecond;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinText(sb);
        return sb.toString();
    }

    public abstract void joinText(StringBuilder sb);

    /**
     * @param index
     *            序号（从0开始）
     * @param it
     *            迭代器
     * @return false 表示找不到项目了
     */
    public abstract boolean parse(int index, Iterator<String> it);

    public abstract void duplicate(SubtitleItem si);

}
