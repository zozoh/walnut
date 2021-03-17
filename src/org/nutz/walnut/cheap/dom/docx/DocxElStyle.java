package org.nutz.walnut.cheap.dom.docx;

class DocxElStyle {

    boolean bold;

    boolean italic;

    boolean underline;

    boolean hasStyle() {
        return bold || italic || underline;
    }

}
