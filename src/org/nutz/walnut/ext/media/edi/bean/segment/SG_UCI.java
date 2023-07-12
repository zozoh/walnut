package org.nutz.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.util.Wlang;

public class SG_UCI {

    private String refNumber;

    private String creator;

    private String creatorIdCode;

    private String owner;

    private String recipient;

    private String recipientIdCode;

    private String recipientRoutingAddress;

    private String actionCode;

    public SG_UCI() {}

    public SG_UCI(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                                    "refNumber",
                                    "creator,creatorIdCode,owner",
                                    "recipient,recipientIdCode,recipientRoutingAddress",
                                    "actionCode");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public SG_UCI valueOf(NutBean bean) {
        this.refNumber = bean.getString("refNumber");
        this.creator = bean.getString("creator");
        this.creatorIdCode = bean.getString("creatorIdCode");
        this.owner = bean.getString("owner");
        this.recipient = bean.getString("recipient");
        this.recipientIdCode = bean.getString("recipientIdCode");
        this.recipientRoutingAddress = bean.getString("recipientRoutingAddress");
        this.actionCode = bean.getString("actionCode");
        return this;
    }

    public boolean isRejected() {
        return "4".equals(actionCode);
    }

    public boolean isNotExplicitlyRejected() {
        return "7".equals(actionCode);
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
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

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }
}
