package com.site0.walnut.ext.media.edi.bean.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;

public class ICS_UCI {

    private String refNum;

    // 发件人站点 Id
    private String creator;

    // 所有者站点 Id
    private String owner;

    // 收件人站点 Id
    private String recipient;

    // 4 --> 报文被拒绝；7 --> 相应引用级别已确认；[8：交换已收到(未在文档中列出)]
    private String actionCode;

    private String errCode;

    // 出现错误的交换服务段的段标记标识符，即 UNA、UNB 或 UNZ
    private String segTag;

    private String elementPos;

    public ICS_UCI() {
    }

    public ICS_UCI(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                "refNum", "creator,,owner", "recipient",
                "actionCode", "errCode", "segTag", "elementPos");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCI valueOf(NutBean bean) {
        this.refNum = bean.getString("refNum");
        this.creator = bean.getString("creator");
        this.owner = bean.getString("owner");
        this.recipient = bean.getString("recipient");
        this.actionCode = bean.getString("actionCode");
        return this;
    }

    public boolean isRejected() {
        return "4".equals(actionCode);
    }

    public boolean isNotExplicitlyRejected() {
        return "7".equals(actionCode);
    }

    public String getRefNum() {
        return refNum;
    }

    public void setRefNum(String refNum) {
        this.refNum = refNum;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getSegTag() {
        return segTag;
    }

    public void setSegTag(String segTag) {
        this.segTag = segTag;
    }

    public String getElementPos() {
        return elementPos;
    }

    public void setElementPos(String elementPos) {
        this.elementPos = elementPos;
    }
}
