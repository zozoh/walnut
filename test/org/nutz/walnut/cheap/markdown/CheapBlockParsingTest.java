package org.nutz.walnut.cheap.markdown;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CheapBlockParsingTest {

    @Test
    public void test_parse_block_table_2() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("-|-");
        list.add("-|-");
        list.add("c|d");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(1, blocks.size());

        CheapBlock b0 = blocks.get(0);
        assertEquals(LineType.TABLE, b0.type);
        assertEquals("Line(0)<PARAGRAPH> -|-", b0.line(0).toString());
        assertEquals("Line(1)<TABKE_HEAD_LINE> -|-", b0.line(1).toString());
        assertEquals("Line(2)<PARAGRAPH> c|d", b0.line(2).toString());

    }

    @Test
    public void test_parse_block_table() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("a|b");
        list.add("-|-");
        list.add("c|d");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(1, blocks.size());

        CheapBlock b0 = blocks.get(0);
        assertEquals(LineType.TABLE, b0.type);
        assertEquals("Line(0)<PARAGRAPH> a|b", b0.line(0).toString());
        assertEquals("Line(1)<TABKE_HEAD_LINE> -|-", b0.line(1).toString());
        assertEquals("Line(2)<PARAGRAPH> c|d", b0.line(2).toString());

    }

    @Test
    public void test_parse_block_heading() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("# A");
        list.add("xxx");
        list.add("## B");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(3, blocks.size());

        assertEquals("Line(0)<HEADING> # A", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<PARAGRAPH> xxx", blocks.get(1).line(0).toString());
        assertEquals("Line(2)<HEADING> ## B", blocks.get(2).line(0).toString());

        assertEquals(1, blocks.get(0).line(0).level);
        assertEquals(2, blocks.get(2).line(0).level);
    }

    @Test
    public void test_parse_block_p_hr_p() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("---");
        list.add("bbb");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(3, blocks.size());

        assertEquals("Line(0)<PARAGRAPH> aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<HR> ---", blocks.get(1).line(0).toString());
        assertEquals("Line(2)<PARAGRAPH> bbb", blocks.get(2).line(0).toString());
    }

    @Test
    public void test_parse_block_code_fenced() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("```test");
        list.add("aaa");
        list.add(" bb");
        list.add("```");
        list.add("ddd");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.CODE_BLOCK, blocks.get(0).type);

        assertEquals("Line(0)<CODE_BLOCK:FENCED> ```test", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<CODE_BLOCK:FENCED> aaa", blocks.get(0).line(1).toString());
        assertEquals("Line(2)<CODE_BLOCK:FENCED>  bb", blocks.get(0).line(2).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4)<PARAGRAPH> ddd", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_code_block_indent_2() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("    aaa");
        list.add("");
        list.add("    bbb");
        list.add("   ");
        list.add("ddd");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.CODE_BLOCK, blocks.get(0).type);

        assertEquals("aaa", blocks.get(0).line(0).content);
        assertEquals("", blocks.get(0).line(1).content);
        assertEquals("bbb", blocks.get(0).line(2).content);

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4)<PARAGRAPH> ddd", blocks.get(1).line(0).toString());
    }

    @Test
    public void test_parse_block_code_block_indent() {
        // 准备输入
        List<String> list = new ArrayList<>();
        list.add("    aaa");
        list.add("    bbb");
        list.add("   ");
        list.add("ddd");
        String[] lines = list.toArray(new String[list.size()]);

        // 准备解析器
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.CODE_BLOCK, blocks.get(0).type);

        assertEquals("aaa", blocks.get(0).line(0).content);
        assertEquals("bbb", blocks.get(0).line(1).content);

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3)<PARAGRAPH> ddd", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.PARAGRAPH, blocks.get(0).type);

        assertEquals("Line(0)<PARAGRAPH> aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<PARAGRAPH> bbb", blocks.get(0).line(1).toString());
        assertEquals("Line(2)<PARAGRAPH> ccc", blocks.get(0).line(2).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4)<PARAGRAPH> ddd", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals("Line(0)<LIST:UL> - aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<LIST:OL>    1. bbb", blocks.get(0).line(1).toString());
        assertEquals("Line(2)<PARAGRAPH> hello", blocks.get(0).line(2).toString());
        assertEquals("Line(3)<PARAGRAPH>    world", blocks.get(0).line(3).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(5)<PARAGRAPH> xxx", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(4, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);
        assertEquals("Line(0)<LIST:UL> - aaa", blocks.get(0).line(0).toString());

        assertEquals(LineType.LIST, blocks.get(1).type);
        assertEquals("Line(1)<LIST:OL> 1. bbb", blocks.get(1).line(0).toString());

        assertEquals(LineType.LIST, blocks.get(2).type);
        assertEquals("Line(2)<LIST:UL> - ccc", blocks.get(2).line(0).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(3).type);
        assertEquals("Line(4)<PARAGRAPH> xxx", blocks.get(3).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());

        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.UL, blocks.get(0).line(0).listType);
        assertEquals("Line(0)<LIST:UL> - aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.OL, blocks.get(0).line(1).listType);
        assertEquals("Line(1)<LIST:OL>    1. bbb", blocks.get(0).line(1).toString());

        assertEquals(ListType.OL, blocks.get(0).line(2).listType);
        assertEquals("Line(2)<LIST:OL>    2. ccc", blocks.get(0).line(2).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(4)<PARAGRAPH> xxx", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.BLOCKQUOTE, blocks.get(0).type);
        assertEquals("Line(0)<BLOCKQUOTE> > aaa", blocks.get(0).line(0).toString());
        assertEquals("Line(1)<PARAGRAPH> bbb", blocks.get(0).line(1).toString());
        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3)<PARAGRAPH> ccc", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.UL, blocks.get(0).line(0).listType);
        assertEquals("Line(0)<LIST:UL> - aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.UL, blocks.get(0).line(1).listType);
        assertEquals("Line(1)<LIST:UL> - bbb", blocks.get(0).line(1).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3)<PARAGRAPH> ccc", blocks.get(1).line(0).toString());
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
        CheapBlockParsing ing = new CheapBlockParsing();

        // 测试
        List<CheapBlock> blocks = ing.invoke(lines);
        assertEquals(2, blocks.size());
        assertEquals(LineType.LIST, blocks.get(0).type);

        assertEquals(ListType.OL, blocks.get(0).line(0).listType);
        assertEquals("Line(0)<LIST:OL> 1. aaa", blocks.get(0).line(0).toString());

        assertEquals(ListType.OL, blocks.get(0).line(1).listType);
        assertEquals("Line(1)<LIST:OL> 2. bbb", blocks.get(0).line(1).toString());

        assertEquals(LineType.PARAGRAPH, blocks.get(1).type);
        assertEquals("Line(3)<PARAGRAPH> ccc", blocks.get(1).line(0).toString());
    }

}