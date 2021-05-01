package org.nutz.walnut.ext.media.subtitle.impl;

import org.nutz.walnut.ext.media.subtitle.bean.SubtitleItem;

public class SrtSubtitleService extends AbstractSubtitleService {

    protected SubtitleItem createItem() {
        return new SrtSubtitleItem();
    }

}
