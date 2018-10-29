package org.nutz.walnut.ext.timg.builder;

import org.nutz.walnut.ext.timg.AbstractCartonBuilder;
import org.nutz.walnut.ext.timg.CartonCtx;

public class NopCartonBuilder extends AbstractCartonBuilder {

    public void _invoke(CartonCtx ctx) {
        exportOrigin(ctx, ctx.cur.cartonTime / (1000 / ctx.fps));
    }

}
