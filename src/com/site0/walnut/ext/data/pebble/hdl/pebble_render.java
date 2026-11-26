package com.site0.walnut.ext.data.pebble.hdl;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.pebble.PebbleContext;
import com.site0.walnut.ext.data.pebble.PebbleFilter;
import com.site0.walnut.ext.data.pebble.WnPebbleLoader;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.template.PebbleTemplate;

/**
 * `@render` 渲染模板并输出：`-strict` 变量未定义即报错，`-maxsize` 输出字节上限。
 *
 * <p>
 * 上限是**渲染中断言**的：输出目标按 UTF-8 字节计数，一超限立刻抛出，不会先把
 * 整页渲染出来再检查，也不会输出半截内容。
 */
public class pebble_render extends PebbleFilter {

    public static final int DFT_MAX_SIZE = 8388608;

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(strict)$");
    }

    @Override
    protected void process(WnSystem sys, PebbleContext fc, ZParams params) {
        // 没有模板，什么也不输出
        if (null == fc.tplPath && null == fc.tmpl) {
            return;
        }

        boolean strict = params.is("strict", false);
        int maxSize = params.getInt("maxsize", DFT_MAX_SIZE);

        sys.out.print(render(sys, fc, strict, maxSize));
    }

    private String render(WnSystem sys, PebbleContext fc, boolean strict, int maxSize) {
        WnPebbleLoader loader = new WnPebbleLoader(sys.io, sys.session);

        PebbleEngine engine = new PebbleEngine.Builder().loader(loader)
                                                       .strictVariables(strict)
                                                       .cacheActive(false)
                                                       .maxRenderedSize(backstopOf(maxSize))
                                                       .build();

        MaxSizeWriter out = new MaxSizeWriter(maxSize);
        try {
            PebbleTemplate tpl = (null != fc.tplPath)
                    ? engine.getTemplate(fc.tplPath)
                    : engine.getLiteralTemplate(fc.tmpl);

            tpl.evaluate(out, fc.vars);
            return out.toString();
        }
        catch (MaxSizeExceeded e) {
            throw Er.create("e.cmd.pebble.too.large", e.size);
        }
        catch (PebbleException e) {
            MaxSizeExceeded ml = findMaxSizeExceeded(e);
            if (null != ml) {
                throw Er.create("e.cmd.pebble.too.large", ml.size);
            }
            throw Er.create("e.cmd.pebble.render", describe(e));
        }
        catch (IOException e) {
            throw Er.create("e.cmd.pebble.render", e.getMessage());
        }
    }

    /**
     * 引擎自身的字符级熔断留作后备。
     *
     * <p>
     * 设成上限的 2 倍以上：UTF-8 下「字节数 &gt;= 字符数」，所以我们的字节级
     * Writer 一定先触发，错误码也就稳定是 `e.cmd.pebble.too.large`。
     */
    static int backstopOf(int maxSize) {
        long n = (long) maxSize * 2 + 1024;
        return (int) Math.min(n, Integer.MAX_VALUE - 8);
    }

    private static MaxSizeExceeded findMaxSizeExceeded(Throwable e) {
        Throwable c = e;
        int guard = 0;
        while (null != c && guard++ < 32) {
            if (c instanceof MaxSizeExceeded) {
                return (MaxSizeExceeded) c;
            }
            c = c.getCause();
        }
        return null;
    }

    private String describe(PebbleException e) {
        StringBuilder sb = new StringBuilder();
        String f = e.getFileName();
        if (!Ws.isBlank(f)) {
            sb.append(f);
        }
        Integer line = e.getLineNumber();
        if (null != line) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append("(line ").append(line).append(')');
        }
        if (sb.length() > 0) {
            sb.append(" - ");
        }
        sb.append(e.getPebbleMessage());
        return sb.toString();
    }

    /**
     * 按 UTF-8 字节计数、超限即断的输出目标。
     */
    static class MaxSizeWriter extends Writer {

        private final int maxSize;

        private final StringBuilder sb = new StringBuilder();

        private int size;

        MaxSizeWriter(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            int n = new String(cbuf, off, len).getBytes(StandardCharsets.UTF_8).length;
            if (size + n > maxSize) {
                throw new MaxSizeExceeded(size + n);
            }
            size += n;
            sb.append(cbuf, off, len);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    static class MaxSizeExceeded extends RuntimeException {

        private static final long serialVersionUID = 1L;

        final int size;

        MaxSizeExceeded(int size) {
            super("max rendered size exceeded: " + size);
            this.size = size;
        }
    }
}
