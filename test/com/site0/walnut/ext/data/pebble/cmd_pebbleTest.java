package com.site0.walnut.ext.data.pebble;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.site0.walnut.BaseBoxTest;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;

/**
 * `pebble` 命令的行为契约（对应 man/zh_cn/cmd_pebble*.md）
 *
 * <ul>
 * <li>@load：参数 / -f 文件 / 管道
 * <li>@vars：参数 JSON / -f 文件 / 管道，多次融合
 * <li>@render：-strict / -maxsize，输出到 stdout
 * <li>模板内部：extends/include 支持 ~/ 绝对路径与相对当前模板目录
 * <li>安全：默认 HTML 转义，|raw 才原样输出
 * </ul>
 */
public class cmd_pebbleTest extends BaseBoxTest {

    // ---------------------------------------------------------------
    // 工具
    // ---------------------------------------------------------------

    private WnObj writeText(String ph, String content) {
        String path = Wn.normalizeFullPath(ph, se);
        WnObj o = io.create(null, path, WnRace.FILE);
        io.writeText(o, content);
        return o;
    }

    private void prepareLayout() {
        writeText("~/templates/layout.html",
                  "<!DOCTYPE html>\n"
                  + "<html><head>{% include \"partials/head.html\" %}</head>\n"
                  + "<body>{% include \"partials/header.html\" %}"
                  + "<main>{% block content %}{% endblock %}</main></body></html>");
        writeText("~/templates/partials/head.html", "<title>{{ page.title }}</title>");
        writeText("~/templates/partials/header.html", "<nav>菜单</nav>");
    }

    // ---------------------------------------------------------------
    // @load
    // ---------------------------------------------------------------

    @Test
    public void test_load_from_arg() {
        box.run("pebble @load '你好 {{ name }}' @vars '{name:\"wendal\"}' @render");
        assertEquals("你好 wendal", outs());
        assertEquals("", terrs());
    }

    @Test
    public void test_load_from_file() {
        writeText("~/templates/hello.html", "<h1>{{ title }}</h1>");

        box.run("pebble @load -f ~/templates/hello.html @vars '{title:\"首页\"}' @render");

        assertEquals("<h1>首页</h1>", outs());
        assertEquals("", terrs());
    }

    @Test
    public void test_load_file_not_exists() {
        box.run("pebble @load -f ~/templates/nothing.html @render");

        assertEquals("", outs());
        assertTrue(terrs(), terrs().contains("e.cmd.pebble.tpl.noexists"));
    }

    // ---------------------------------------------------------------
    // 转义
    // ---------------------------------------------------------------

    @Test
    public void test_escape_by_default() {
        box.run("pebble @load '{{ nm }}' @vars '{nm:\"A & B\"}' @render");
        assertEquals("A &amp; B", outs());
    }

    @Test
    public void test_raw_keeps_html() {
        // 正文里的尖括号不能走命令行参数（会被当成重定向），所以模板和变量都落文件
        writeText("~/templates/raw.html", "{{ body|raw }}");
        writeText("~/tmp/raw.json", "{\"body\":\"<b>粗</b>\"}");

        box.run("pebble @load -f ~/templates/raw.html @vars -f ~/tmp/raw.json @render");

        assertEquals("<b>粗</b>", outs());
        assertEquals("", terrs());
    }

    // ---------------------------------------------------------------
    // 继承与包含
    // ---------------------------------------------------------------

    @Test
    public void test_extends_abs_and_include_rel() {
        prepareLayout();
        writeText("~/templates/views/list.html",
                  "{% extends \"~/templates/layout.html\" %}"
                  + "{% block content %}共 {{ list|length }} 条{% endblock %}");

        box.run("pebble @load -f ~/templates/views/list.html"
                + " @vars '{page:{title:\"首页\"},list:[\"a\",\"b\",\"c\"]}' @render");

        String s = outs();
        assertEquals("", terrs());
        assertTrue(s, s.contains("<title>首页</title>"));
        assertTrue(s, s.contains("<nav>菜单</nav>"));
        assertTrue(s, s.contains("共 3 条"));
    }

    @Test
    public void test_include_relative_to_current_template() {
        writeText("~/templates/views/detail.html", "<div>{% include \"crumb.html\" %}</div>");
        writeText("~/templates/views/crumb.html", "<span>详情</span>");

        box.run("pebble @load -f ~/templates/views/detail.html @render");

        assertEquals("<div><span>详情</span></div>", outs());
        assertEquals("", terrs());
    }

    // ---------------------------------------------------------------
    // @vars
    // ---------------------------------------------------------------

    @Test
    public void test_vars_from_file() {
        writeText("~/templates/hello.html", "<h1>{{ title }}</h1>");
        writeText("~/tmp/vars.json", "{\"title\":\"来自文件\"}");

        box.run("pebble @load -f ~/templates/hello.html @vars -f ~/tmp/vars.json @render");

        assertEquals("<h1>来自文件</h1>", outs());
        assertEquals("", terrs());
    }

    @Test
    public void test_vars_from_pipe() {
        writeText("~/templates/hello.html", "<h1>{{ title }}</h1>");

        box.run("echo '{\"title\":\"来自管道\"}' | pebble @load -f ~/templates/hello.html @vars @render");

        assertEquals("<h1>来自管道</h1>", outs());
        assertEquals("", terrs());
    }

    @Test
    public void test_vars_merge() {
        writeText("~/templates/pair.html", "{{ a }}-{{ b }}");

        box.run("pebble @load -f ~/templates/pair.html"
                + " @vars '{a:1}' @vars '{b:2}' @render");

        assertEquals("1-2", outs());
        assertEquals("", terrs());
    }

    // ---------------------------------------------------------------
    // @render
    // ---------------------------------------------------------------

    @Test
    public void test_strict_off_renders_blank() {
        box.run("pebble @load '[{{ nope }}]' @render");
        assertEquals("[]", outs());
    }

    @Test
    public void test_strict_on_throws() {
        box.run("pebble @load '[{{ nope }}]' @render -strict");

        assertEquals("", outs());
        assertTrue(terrs(), terrs().contains("e.cmd.pebble.render"));
    }

    @Test
    public void test_render_syntax_error() {
        writeText("~/templates/bad.html", "{% if a %}没有 endif");

        box.run("pebble @load -f ~/templates/bad.html @vars '{a:1}' @render");

        assertEquals("", outs());
        assertTrue(terrs(), terrs().contains("e.cmd.pebble.render"));
        assertTrue(terrs(), terrs().contains("bad.html"));
    }

    @Test
    public void test_render_maxsize() {
        box.run("pebble @load '0123456789abcdef' @render -maxsize 10");

        assertEquals("", outs());
        assertTrue(terrs(), terrs().contains("e.cmd.pebble.too.large"));
    }

    @Test
    public void test_render_maxsize_count_utf8_bytes() {
        // 4 个汉字 = 12 字节，超过 6 字节上限；按字符数算会误判成 4 而放过
        box.run("pebble @load '你好世界' @render -maxsize 6");

        assertEquals("", outs());
        assertTrue(terrs(), terrs().contains("e.cmd.pebble.too.large"));
    }

    @Test
    public void test_render_maxsize_exact_boundary() {
        // 正好等于上限，不能误伤
        box.run("pebble @load 'abc' @render -maxsize 3");

        assertEquals("abc", outs());
        assertEquals("", terrs());
    }

    @Test
    public void test_render_redirect_to_file() {
        writeText("~/templates/hello.html", "<h1>{{ title }}</h1>");

        box.run("pebble @load -f ~/templates/hello.html @vars '{title:\"落盘\"}' @render > ~/out.html");

        WnObj o = this.check("~/out.html");
        assertEquals("<h1>落盘</h1>", io.readText(o));
    }
}
