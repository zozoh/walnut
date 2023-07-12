package org.nutz.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.util.Wlang;

/**
 * <code>UNB</code>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class SG_UNB {

    private String syntaxId;

    private String syntaxVersion;

    private String creator;

    private String creatorIdCode;

    private String owner;

    private String recipient;

    private String recipientIdCode;

    private String recipientRoutingAddress;

    private String transDate;

    private String transTime;

    private String controlRefNumber;

    // 一下是可选

    private String recipientRefPassword;

    private String recipientRefPasswordQualifier;

    private String applicationReference;

    private String processingPriorityCode;

    private String requested;

    private String test;

    public SG_UNB() {}

    public SG_UNB(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                                    "syntaxId,syntaxVersion",
                                    "creator,creatorIdCode,owner",
                                    "recipient,recipientIdCode,recipientRoutingAddress",
                                    "transDate,transTime",
                                    "controlRefNumber",
                                    "recipientRefPassword",
                                    "recipientRefPasswordQualifier",
                                    "applicationReference",
                                    "processingPriorityCode",
                                    "requested",
                                    "test");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public SG_UNB valueOf(NutBean bean) {
        this.syntaxId = bean.getString("syntaxId");
        this.syntaxVersion = bean.getString("syntaxVersion");
        this.creator = bean.getString("creator");
        this.creatorIdCode = bean.getString("creatorIdCode");
        this.owner = bean.getString("owner");
        this.recipient = bean.getString("recipient");
        this.recipientIdCode = bean.getString("recipientIdCode");
        this.recipientRoutingAddress = bean.getString("recipientRoutingAddress");
        this.transDate = bean.getString("transDate");
        this.transTime = bean.getString("transTime");
        this.controlRefNumber = bean.getString("controlRefNumber");
        this.recipientRefPassword = bean.getString("recipientRefPassword");
        this.recipientRefPasswordQualifier = bean.getString("recipientRefPasswordQualifier");
        this.applicationReference = bean.getString("applicationReference");
        this.processingPriorityCode = bean.getString("processingPriorityCode");
        this.requested = bean.getString("requested");
        this.test = bean.getString("test");
        return this;
    }

    public String getSyntaxId() {
        return syntaxId;
    }

    public void setSyntaxId(String syntaxId) {
        this.syntaxId = syntaxId;
    }

    public String getSyntaxVersion() {
        return syntaxVersion;
    }

    public void setSyntaxVersion(String syntaxVersion) {
        this.syntaxVersion = syntaxVersion;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorIdCode() {
        return creatorIdCode;
    }

    public void setCreatorIdCode(String creatorIdCode) {
        this.creatorIdCode = creatorIdCode;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipientIdCode() {
        return recipientIdCode;
    }

    public void setRecipientIdCode(String recipientIdCode) {
        this.recipientIdCode = recipientIdCode;
    }

    public String getRecipientRoutingAddress() {
        return recipientRoutingAddress;
    }

    public void setRecipientRoutingAddress(String recipientRoutingAddress) {
        this.recipientRoutingAddress = recipientRoutingAddress;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getControlRefNumber() {
        return controlRefNumber;
    }

    public void setControlRefNumber(String controlRefNumber) {
        this.controlRefNumber = controlRefNumber;
    }

    public String getRecipientRefPassword() {
        return recipientRefPassword;
    }

    public void setRecipientRefPassword(String recipientRefPassword) {
        this.recipientRefPassword = recipientRefPassword;
    }

    public String getRecipientRefPasswordQualifier() {
        return recipientRefPasswordQualifier;
    }

    public void setRecipientRefPasswordQualifier(String recipientRefPasswordQualifier) {
        this.recipientRefPasswordQualifier = recipientRefPasswordQualifier;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
    }

    public String getProcessingPriorityCode() {
        return processingPriorityCode;
    }

    public void setProcessingPriorityCode(String processingPriorityCode) {
        this.processingPriorityCode = processingPriorityCode;
    }

    public String getRequested() {
        return requested;
    }

    public void setRequested(String requested) {
        this.requested = requested;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

}
