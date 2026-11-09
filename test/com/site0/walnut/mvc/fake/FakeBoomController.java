package com.site0.walnut.mvc.fake;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcController;
import com.site0.walnut.mvc.WnMvcView;

/**
 * 爆炸假控制器：handle 一律抛 {@link RuntimeException}，
 * 用于验证「controller 异常 → 失败视图」路径。
 */
public class FakeBoomController implements WnMvcController {

    @Override
    public WnMvcView handle(WnMvcContext context) {
        throw new RuntimeException("boom");
    }

}
