package com.site0.walnut.ext.media.subtitle.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.ext.media.subtitle.SubtitleService;
import com.site0.walnut.ext.media.subtitle.bean.SubtitleItem;
import com.site0.walnut.ext.media.subtitle.bean.SubtitleObj;

public abstract class AbstractSubtitleService implements SubtitleService {

    public AbstractSubtitleService() {
        super();
    }

    @Override
    public SubtitleObj parse(CharSequence cs) {
        // 准备返回对象
        SubtitleObj sto = new SubtitleObj();

        // 序号从 0 开始
        int index = 0;

        // 解析成行
        String[] lines = cs.toString().split("\r?\n");
        List<String> list = Wlang.list(lines);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            SubtitleItem si = createItem();
            if (si.parse(index++, it)) {
                sto.items.add(si);
            } else {
                break;
            }
        }

        // 对对象按照序号排序
        Collections.sort(sto.items);

        // 返回
        return sto;
    }

    @Override
    public String render(SubtitleObj sto) {
        // 准备输出
        StringBuilder sb = new StringBuilder();

        // 逐个输出
        for (SubtitleItem si : sto.items) {
            SubtitleItem si2 = this.createItem();
            si2.duplicate(si);
            si2.joinText(sb);
        }

        // 输出
        return sb.toString();
    }

    protected abstract SubtitleItem createItem();
}