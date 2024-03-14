package com.site0.walnut.ext.media.subtitle.impl;

import com.site0.walnut.ext.media.subtitle.bean.SubtitleItem;

public class SrtSubtitleService extends AbstractSubtitleService {

    protected SubtitleItem createItem() {
        return new SrtSubtitleItem();
    }

}
