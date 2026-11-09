package com.site0.walnut.mvc.fake;

import java.util.function.Function;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcView;

/**
 * 万能假控制器：请求逻辑由静态 {@link #HANDLER} 注入，测试用例按需替换。
 * 每个用例前务必 {@link #reset()} 或重新赋值，避免用例间串扰。
 */
public class FakeController implements WnMvcController {

    public static Function<WnMvcContext, WnMvcView> HANDLER = ctx -> WnMvcView.none();

    public static void reset() {
        HANDLER = ctx -> WnMvcView.none();
    }

    @Override
    public WnMvcView handle(WnMvcContext context) {
        return HANDLER.apply(context);
    }

}
