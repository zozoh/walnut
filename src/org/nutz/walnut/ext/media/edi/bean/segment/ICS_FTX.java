package org.nutz.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.util.Wlang;

/**
 * 
 * <pre>
 * FTX+ABN++14165610382'
 * FTX+AAO+++BUSINESS ADDRESS LOCALITY SUPPLIED WITHOUT A VALID CLIENT TYPE'
 * FTX+AAO+++THIS TRANSACTION WAS REJECTED'
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @see https://www.abf.gov.au/help-and-support/ics/integrated-cargo-system-(ics)/software-developers/messaging/ics-message-implementation-guidelines/clreg-client-registration
 */
public class ICS_FTX {

    private String subjectCode;

    // TEXT FUNCTION, CODED

    private String funcCoded;

    // X C107 TEXT REFERENCE C 1
    // X 4441 Free text value code M an..17
    // X 1131 Code list identification code C an..3
    // X 3055 Code list responsible agency code C an..3
    private String reference;

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
    private String literal;

    // X 3453 LANGUAGE NAME CODE C 1 an..3
    private String language;
    // X 4447 TEXT FORMATTING, CODED C 1 an..3
    private String formatting;

    public ICS_FTX() {}

    public ICS_FTX(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                                    "subjectCode",
                                    "funcCoded",
                                    "reference",
                                    "literal",
                                    "language",
                                    "formatting");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_FTX valueOf(NutBean bean) {
        this.subjectCode = bean.getString("subjectCode");
        this.funcCoded = bean.getString("funcCoded");
        this.reference = bean.getString("reference");
        this.literal = bean.getString("literal");
        this.language = bean.getString("language");
        this.formatting = bean.getString("formatting");
        return this;
    }

    public boolean isSubject(String code) {
        if (null == code || null == this.subjectCode) {
            return false;
        }
        return code.equals(this.subjectCode);
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getFuncCoded() {
        return funcCoded;
    }

    public void setFuncCoded(String funcCoded) {
        this.funcCoded = funcCoded;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFormatting() {
        return formatting;
    }

    public void setFormatting(String formatting) {
        this.formatting = formatting;
    }

}
