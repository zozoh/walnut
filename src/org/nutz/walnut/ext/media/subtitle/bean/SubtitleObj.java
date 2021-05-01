package org.nutz.walnut.ext.media.subtitle.bean;

import java.util.LinkedList;
import java.util.List;

public class SubtitleObj {

    public List<SubtitleItem> items;

    public SubtitleObj() {
        this.items = new LinkedList<>();
    }

    /**
     * 整体移动所有的字幕项
     * 
     * @param ms
     *            毫秒，负数表示向前移动，正数表示向后移动，0 不移动
     */
    public void offset(int ms) {
        if (ms != 0) {
            for (SubtitleItem si : items) {
                si.beginTime.offsetInMillisecond(ms);
                si.endTime.offsetInMillisecond(ms);
            }
        }
    }

}
