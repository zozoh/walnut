package com.site0.walnut.ext.media.timg.builder;

import com.site0.walnut.ext.media.timg.AbstractCartonBuilder;
import com.site0.walnut.ext.media.timg.CartonCtx;

public class NopCartonBuilder extends AbstractCartonBuilder {

    public void _invoke(CartonCtx ctx) {
        exportOrigin(ctx, ctx.cur.cartonTime / (1000 / ctx.fps));
    }

}
