package org.nutz.walnut.cheap.markdown;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CheapMarkdownParsingTest {

    @Test
    public void test_parse_block_code_block() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("    aaa");
        list.add("    bbb");
        list.add("   ");
        list.add("ddd");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing(4);

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.CODE_BLOCK, blocks.get(0).type);

        assertEquals("aaa", blocks.get(0).line(0).content);
        assertEquals("bbb", blocks.get(0).line(1).content);

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3) ddd", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_simple() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("bbb");
        list.add("ccc");
        list.add("   ");
        list.add("ddd");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.PARAGRAPH, blocks.get(0).type);

        assertEquals("Line(0) aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1) bbb", blocks.get(0).line(1).toString());
        assertEquals("Line(2) ccc", blocks.get(0).line(2).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4) ddd", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_list_multi_line() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("- aaa");
        list.add("   1. bbb");
        list.add("hello");
        list.add("   world");
        list.add(" ");
        list.add("xxx");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals("Line(0) - aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1)    1. bbb", blocks.get(0).line(1).toString());
        assertEquals("Line(2) hello", blocks.get(0).line(2).toString());
        assertEquals("Line(3)    world", blocks.get(0).line(3).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(5) xxx", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_ul_ol_2() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("- aaa");
        list.add("1. bbb");
        list.add("- ccc");
        list.add(" ");
        list.add("xxx");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(4, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);
        assertEquals("Line(0) - aaa", blocks.get(0).line(0).toString());

        assertEquals(LineType.LIST, blocks.get(1).type);
        assertEquals("Line(1) 1. bbb", blocks.get(1).line(0).toString());

        assertEquals(LineType.LIST, blocks.get(2).type);
        assertEquals("Line(2) - ccc", blocks.get(2).line(0).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(3).type);
        assertEquals("Line(4) xxx", blocks.get(3).line(0).toString());
    }

    @Test
    public void test_parse_block_ul_ol() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("- aaa");
        list.add("   1. bbb");
        list.add("   2. ccc");
        list.add(" ");
        list.add("xxx");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.UL, blocks.get(0).line(0).listType);
        assertEquals("Line(0) - aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.OL, blocks.get(0).line(1).listType);
        assertEquals("Line(1)    1. bbb", blocks.get(0).line(1).toString());

        assertEquals(ListType.OL, blocks.get(0).line(2).listType);
        assertEquals("Line(2)    2. ccc", blocks.get(0).line(2).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4) xxx", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_quote() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("> aaa");
        list.add("bbb");
        list.add(" ");
        list.add("ccc");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.BLOCKQUOTE, blocks.get(0).type);
        assertEquals("Line(0) > aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1) bbb", blocks.get(0).line(1).toString());
        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3) ccc", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_ul() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("- aaa");
        list.add("- bbb");
        list.add(" ");
        list.add("ccc");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.UL, blocks.get(0).line(0).listType);
        assertEquals("Line(0) - aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.UL, blocks.get(0).line(1).listType);
        assertEquals("Line(1) - bbb", blocks.get(0).line(1).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3) ccc", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_ol() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("1. aaa");
        list.add("2. bbb");
        list.add(" ");
        list.add("ccc");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapMarkdownParsing ing = new CheapMarkdownParsing();

        // 测试
        List<CheapBlock> blocks = ing.parseBlocks(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.OL, blocks.get(0).line(0).listType);
        assertEquals("Line(0) 1. aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.OL, blocks.get(0).line(1).listType);
        assertEquals("Line(1) 2. bbb", blocks.get(0).line(1).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3) ccc", blocks.get(1).line(0).toString());
    }

}
