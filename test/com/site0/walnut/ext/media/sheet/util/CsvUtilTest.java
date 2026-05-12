package com.site0.walnut.ext.media.sheet.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static com.site0.walnut.ext.media.sheet.util.CsvUtil.*;

public class CsvUtilTest {

    private void assertSplit(String line, char[] seps, String... expecteds) {
        List<String> re = splitLine(line, seps);
        assertEquals(Arrays.asList(expecteds), re);
    }

    @Test
    public void split_single_cell_without_separator() {
        assertSplit("abc", new char[]{','}, "abc");
    }

    @Test
    public void split_simple_csv_by_comma() {
        assertSplit("a,b,c", new char[]{','}, "a", "b", "c");
    }

    @Test
    public void split_by_multiple_separators() {
        assertSplit("a,b;c d", new char[]{' ', ',', ';'}, "a", "b", "c", "d");
    }

    @Test
    public void split_consecutive_separators_keep_empty_cells() {
        assertSplit("a,,b", new char[]{','}, "a", "", "b");
    }

    @Test
    public void split_leading_and_trailing_separator_keep_empty_cells() {
        assertSplit(",a,b,", new char[]{','}, "", "a", "b", "");
    }

    @Test
    public void split_quoted_cell_with_separator_inside() {
        assertSplit("a,\"b,c\",d", new char[]{','}, "a", "b,c", "d");
    }

    @Test
    public void split_quoted_cell_with_multiple_separators_inside() {
        assertSplit("x;\"a,b;c d\";y",
                    new char[]{' ', ',', ';'},
                    "x",
                    "a,b;c d",
                    "y");
    }

    @Test
    public void split_escaped_quote_in_quoted_cell() {
        assertSplit("a,\"b\"\"c\"", new char[]{','}, "a", "b\"c");
    }

    @Test
    public void split_empty_quoted_cell() {
        assertSplit("\"\",x", new char[]{','}, "", "x");
    }

    @Test
    public void split_all_empty_cells() {
        assertSplit(",,", new char[]{','}, "", "", "");
    }

}
