package org.nutz.walnut.ext.timg.builder;

import org.nutz.walnut.ext.timg.AbstraceCartonBuilder;
import org.nutz.walnut.ext.timg.CartonCtx;

public class NopCartonBuilder extends AbstraceCartonBuilder {

    public void invoke(CartonCtx ctx) {
        prepareImages(ctx);
        exportOrigin(ctx, ctx.cur.playTime / (1000 / ctx.fps));
        exportOrigin(ctx, ctx.cur.cartonTime / (1000 / ctx.fps));
    }

}
