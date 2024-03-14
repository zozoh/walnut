package com.site0.walnut.cheap.dom.docx.en;

import com.site0.walnut.util.Ws;

public class DocxBlockContext {

    public String align;

    public boolean hasAlign() {
        return !Ws.isBlank(align);
    }
}
