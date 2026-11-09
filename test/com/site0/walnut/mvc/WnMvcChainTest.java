package com.site0.walnut.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * T5 动作链顺序与 cleanup 测试（特性矩阵 §3.9 动作链；P0 fail-closed 用例）。
 *
 * <p>链语义：headFilters（不抛异常即通过）→ controller → tailFilters（finally 收尾，
 * 前置失败也执行）；过滤器异常记日志 + 失败视图 + 中断（fail-closed）。</p>
 */
public class WnMvcChainTest extends WnMvcTestBase {

    private static class Trace {
        final List<String> steps = new ArrayList<>();
        final MockWnMvcResponse resp = new MockWnMvcResponse();
        final WnMvcContext ctx;

        Trace() {
            ctx = new WnMvcFactory().newContext(MockWnMvcRequest.builder()
                                                                .path("/x")
                                                                .build(),
                                                resp);
        }

        WnMvcFilter head(String name) {
            return ctx -> steps.add("head:" + name);
        }

        WnMvcFilter tail(String name) {
            return ctx -> steps.add("tail:" + name);
        }

        WnMvcChain.Action action() {
            return ctx -> {
                steps.add("controller");
                return WnMvcView.json("done");
            };
        }
    }

    /**
     * T-60 顺序：head1 → head2 → controller → tail1 → tail2；视图返回给调度器。
     */
    @Test
    public void t60_chain_order() {
        Trace t = new Trace();
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{t.head("1"), t.head("2")},
                                          new WnMvcFilter[]{t.tail("1"), t.tail("2")},
                                          t.action());
        WnMvcView view = chain.invoke(t.ctx);
        assertEquals("[head:1, head:2, controller, tail:1, tail:2]",
                     t.steps.toString());
        // 视图交回调度器渲染
        view.render(t.ctx);
        assertEquals("\"done\"", t.resp.bodyString());
    }

    /**
     * T-61 head 过滤器拦截：抛 WnMvcFilterException → 不进 controller，
     * 失败视图按异常状态码/错误码渲染。
     */
    @Test
    public void t61_head_filter_breaks() {
        Trace t = new Trace();
        WnMvcFilter deny = ctx -> {
            t.steps.add("head:deny");
            throw new WnMvcFilterException(403, "e.forbid", "denied");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{t.head("1"), deny},
                                          new WnMvcFilter[0],
                                          t.action());
        WnMvcView view = chain.invoke(t.ctx);
        assertNull(view); // 失败视图已由链渲染，不再返回视图
        assertEquals("[head:1, head:deny]", t.steps.toString());
        assertEquals(403, t.resp.status());
        assertEquals("{\"ok\":false,\"errCode\":\"e.forbid\",\"msg\":\"denied\"}",
                     t.resp.bodyString());
    }

    /**
     * T-62 fail-closed（P0）：无上下文/未授权时<b>拒绝而不是放行</b>——
     * 会话检查过滤器抛 401，controller 未执行，响应 401。
     */
    @Test
    public void t62_fail_closed_no_session() {
        Trace t = new Trace();
        WnMvcFilter checkSession = ctx -> {
            if (ctx.session() == null)
                throw new WnMvcFilterException(401, "e.se.noexists", "no session");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{checkSession},
                                          new WnMvcFilter[0],
                                          t.action());
        WnMvcView view = chain.invoke(t.ctx);
        assertNull(view);
        assertTrue(t.steps.isEmpty()); // controller 未执行
        assertEquals(401, t.resp.status());
        assertTrue(t.resp.bodyString().contains("e.se.noexists"));
    }

    /**
     * T-63 cleanup 在 finally：head 过滤器抛异常时，尾部过滤器仍执行。
     */
    @Test
    public void t63_tail_runs_on_head_failure() {
        Trace t = new Trace();
        WnMvcFilter deny = ctx -> {
            throw new WnMvcFilterException(401, "e.se.noexists", "no session");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{deny},
                                          new WnMvcFilter[]{t.tail("cleanup")},
                                          t.action());
        chain.invoke(t.ctx);
        assertEquals("[tail:cleanup]", t.steps.toString());
        assertEquals(401, t.resp.status());
    }

    /**
     * T-64 controller 异常 → 500 失败视图；尾部过滤器仍执行。
     */
    @Test
    public void t64_controller_exception() {
        Trace t = new Trace();
        WnMvcChain.Action boom = ctx -> {
            t.steps.add("controller");
            throw new RuntimeException("boom");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{t.head("1")},
                                          new WnMvcFilter[]{t.tail("cleanup")},
                                          boom);
        WnMvcView view = chain.invoke(t.ctx);
        assertNull(view);
        assertEquals("[head:1, controller, tail:cleanup]", t.steps.toString());
        assertEquals(500, t.resp.status());
        assertTrue(t.resp.bodyString().contains("boom"));
    }

    /**
     * T-65 尾部过滤器异常：响应未提交且未进失败视图时，按异常状态渲染失败视图。
     */
    @Test
    public void t65_tail_failure_renders_fail_view() {
        Trace t = new Trace();
        WnMvcFilter badTail = ctx -> {
            throw new WnMvcFilterException(500, "e.tail", "tail boom");
        };
        // controller 返回视图但调度器尚未渲染 → 响应未提交
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[0],
                                          new WnMvcFilter[]{badTail},
                                          t.action());
        WnMvcView view = chain.invoke(t.ctx);
        assertNull(view); // 失败视图取代 controller 视图
        assertEquals(500, t.resp.status());
        assertTrue(t.resp.bodyString().contains("e.tail"));
    }

    /**
     * T-66 非 WnMvcFilterException 普适异常 → 一律 500。
     */
    @Test
    public void t66_plain_exception_is_500() {
        Trace t = new Trace();
        WnMvcFilter boom = ctx -> {
            throw new IllegalStateException("weird");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[]{boom},
                                          new WnMvcFilter[0],
                                          t.action());
        chain.invoke(t.ctx);
        assertEquals(500, t.resp.status());
        assertTrue(t.resp.bodyString().contains("weird"));
    }

    /**
     * T-67 响应已提交时失败视图不再渲染（只记日志）：controller 直写后抛异常，
     * 状态与正文保持原样。
     */
    @Test
    public void t67_committed_response_not_overwritten() {
        Trace t = new Trace();
        WnMvcChain.Action writeThenBoom = ctx -> {
            ctx.response().status(200);
            ctx.response().contentType("text/plain");
            try {
                java.io.Writer w = ctx.response().writer();
                w.write("partial");
                w.flush();
            }
            catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("late boom");
        };
        WnMvcChain chain = new WnMvcChain(new WnMvcFilter[0], new WnMvcFilter[0], writeThenBoom);
        chain.invoke(t.ctx);
        assertEquals(200, t.resp.status());
        assertEquals("partial", t.resp.bodyString());
    }

}
