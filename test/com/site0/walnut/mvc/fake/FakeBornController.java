package com.site0.walnut.mvc.fake;

import java.util.concurrent.atomic.AtomicInteger;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcView;

/**
 * born 计数假控制器：构造器自增 {@link #BORN_COUNT}，
 * 用于验证「每请求 born 一个新实例」。
 */
public class FakeBornController implements WnMvcController {

    public static final AtomicInteger BORN_COUNT = new AtomicInteger(0);

    public FakeBornController() {
        BORN_COUNT.incrementAndGet();
    }

    @Override
    public WnMvcView handle(WnMvcContext context) {
        return WnMvcView.json("born-" + BORN_COUNT.get());
    }

}
