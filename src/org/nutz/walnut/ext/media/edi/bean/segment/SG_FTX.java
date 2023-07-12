package org.nutz.walnut.ext.media.edi.bean.segment;

public class SG_FTX {

    private String subjectCode;

    // TEXT FUNCTION, CODED

    private String textFunction;

    // X C107 TEXT REFERENCE C 1
    // X 4441 Free text value code M an..17
    // X 1131 Code list identification code C an..3
    // X 3055 Code list responsible agency code C an..3

    // C108 TEXT LITERAL C 1
    // M 4440 Free text value M an..512
    // - Title
    // - Contact Name
    // 4440 Free text value C an..512
    // First Name
    // - Contact Purpose
    // 4440 Free text value C an..512
    // - Second Name
    // 4440 Free text value C an..512
    // - Family Name
    // 4440 Free text value C an..512
    // - Suffix

    // X 3453 LANGUAGE NAME CODE C 1 an..3
    // X 4447 TEXT FORMATTING, CODED C 1 an..3
}
