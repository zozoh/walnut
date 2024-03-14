package com.site0.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;

/**
 * 
 * <pre>
 * DOC+CCI:::AAA3436797Y'
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @see https://www.abf.gov.au/help-and-support/ics/integrated-cargo-system-(ics)/software-developers/messaging/ics-message-implementation-guidelines/clntdup-client-duplicate-response
 */
public class ICS_DOC {

    // DOCUMENT/MESSAGE NAME

    private String nameCode;

    private String codeListIdCode;

    private String codeListAgencyCode;

    private String docName;

    // DOCUMENT/MESSAGE DETAILS

    private String docNumber;

    private String docStatusCode;

    private String docSource;

    private String version;

    private String revisionNumber;

    // COMMUNICATION MEDIUM TYPE CODE

    private String mediumTypeCode;

    private String copiesRequiredNumber;

    private String originalRequieredNumber;

    public ICS_DOC() {}

    public ICS_DOC(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                                    "nameCode,codeListIdCode,codeListAgencyCode,docName",
                                    "docNumber,docStatusCode,docSource,version,revisionNumber",
                                    "mediumTypeCode",
                                    "copiesRequiredNumber",
                                    "originalRequieredNumber");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_DOC valueOf(NutBean bean) {
        this.nameCode = bean.getString("nameCode");
        this.codeListIdCode = bean.getString("codeListIdCode");
        this.codeListAgencyCode = bean.getString("codeListAgencyCode");
        this.docName = bean.getString("docName");
        this.docNumber = bean.getString("docNumber");
        this.docStatusCode = bean.getString("docStatusCode");
        this.docSource = bean.getString("docSource");
        this.version = bean.getString("version");
        this.revisionNumber = bean.getString("revisionNumber");
        this.mediumTypeCode = bean.getString("mediumTypeCode");
        this.copiesRequiredNumber = bean.getString("copiesRequiredNumber");
        this.originalRequieredNumber = bean.getString("originalRequieredNumber");
        return this;
    }

    public String getNameCode() {
        return nameCode;
    }

    public void setNameCode(String nameCode) {
        this.nameCode = nameCode;
    }

    public String getCodeListIdCode() {
        return codeListIdCode;
    }

    public void setCodeListIdCode(String codeListIdCode) {
        this.codeListIdCode = codeListIdCode;
    }

    public String getCodeListAgencyCode() {
        return codeListAgencyCode;
    }

    public void setCodeListAgencyCode(String codeListAgencyCode) {
        this.codeListAgencyCode = codeListAgencyCode;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getDocStatusCode() {
        return docStatusCode;
    }

    public void setDocStatusCode(String docStatusCode) {
        this.docStatusCode = docStatusCode;
    }

    public String getDocSource() {
        return docSource;
    }

    public void setDocSource(String docSource) {
        this.docSource = docSource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(String revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getMediumTypeCode() {
        return mediumTypeCode;
    }

    public void setMediumTypeCode(String mediumTypeCode) {
        this.mediumTypeCode = mediumTypeCode;
    }

    public String getCopiesRequiredNumber() {
        return copiesRequiredNumber;
    }

    public void setCopiesRequiredNumber(String copiesRequiredNumber) {
        this.copiesRequiredNumber = copiesRequiredNumber;
    }

    public String getOriginalRequieredNumber() {
        return originalRequieredNumber;
    }

    public void setOriginalRequieredNumber(String originalRequieredNumber) {
        this.originalRequieredNumber = originalRequieredNumber;
    }

}
