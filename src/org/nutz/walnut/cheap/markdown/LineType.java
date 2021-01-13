package org.nutz.walnut.cheap.markdown;

enum LineType {

    PARAGRAPH, HEADING, BLOCKQUOTE,

    LIST,

    CODE_BLOCK,

    HR,

    /**
     * 表格分割线
     */
    TABKE_HEAD_LINE, TABLE,

    /**
     * 空行
     */
    BLANK
}
