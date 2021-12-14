package org.nutz.walnut.ext.media.sheet.impl;

import java.util.List;

import org.nutz.lang.util.NutBean;

public class SheetResult {

    public List<? extends NutBean> list;

    public List<SheetImage> images;

    public SheetResult() {}

    public SheetResult(List<? extends NutBean> list) {
        this.list = list;
    }
}
