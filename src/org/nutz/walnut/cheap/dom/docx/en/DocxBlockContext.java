package org.nutz.walnut.cheap.dom.docx.en;

import org.nutz.walnut.util.Ws;

public class DocxBlockContext {

    public String align;

    public boolean hasAlign() {
        return !Ws.isBlank(align);
    }
}
