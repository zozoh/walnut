package com.site0.walnut.ext.media.edi.msg.reply.contrl;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

/**
 * Segment UCI: Interchange Response:
 * 对应 "CONTRL" 返回消息的 UCI 报文行
 * 1. 模板: `UCI+{InterchangeControlReferenceNumber}+{InbCreator}::{InbOwner}+{InbRecipientId}+{ActionCode}+{ErrorCode}+{SegmentTag}+{DateElementPosn}:{ComponentDataPosn}'`
 * 2. 样例: `UCI+23031014150005+AAR399A::AAR399A+AAA336C+7'`
 * 3. 变量-InterchangeControlReferenceNumber: 和 "Client Register, CargoReport"等报文的 `UNB` 报文行中的变量 {InterchangeControlReferenceNumber} 内容一致。
 * 4. 变量-InbCreator: 入站信息的 Creator 的站点号，Ronnie 给到的站点编号是 `AAR399A`。
 * 5. 变量-InbOwner: 我们的情况，和 Creator 一致。
 * 6. 变量-InbRecipientId: 收信息方的 站点 id。
 * 7. 变量-ActionCode: 当 code 为 `7` 的时候，后面的变量可能是没有的。
 * - 4: 本级和所有下级拒绝。This level and all lower levels rejected。
 * - 7: 本级承认，如果没有明确拒绝，下一级承认。This level acknowledged, next lower level acknowledged if not explicitly rejected。
 * - 8 : 表明收到交换信息。
 * 8. 变量-ErrorCode：用来表示在交换中检测到的语法错误的代码。具体代码列表，请参考[相关文档](<https://www.abf.gov.au/help-and-support/ics/integrated-cargo-system-(ics)/software-developers/reference-materials/code-list/control-message-syntax-error-codes>)
 * 9. 变量-SegmentTag: 出错的交换服务段的段标标识，它是 UNA、UNB 或 UNZ 中的一个。
 * 10. 变量-DateElementPosn: 错误的数据元素在段内的位置。
 * 11. 变量-ComponentDataPosn: 错误的组件数据元素在数据元素中的位置。
 */
public class CntrIcRes {

    //  发送的 interchange 的 refNum
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

    private String componentPos;


    public CntrIcRes() {
    }

    public CntrIcRes(EdiSegment seg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                "refNum", "creator,,owner", "recipient",
                "actionCode", "errCode", "segTag", "elementPos,componentPos");
        seg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public CntrIcRes valueOf(NutBean bean) {
        this.refNum = bean.getString("refNum");
        this.creator = bean.getString("creator");
        this.owner = bean.getString("owner");
        this.recipient = bean.getString("recipient");
        this.actionCode = bean.getString("actionCode");
        this.errCode = bean.getString("errCode");
        this.segTag = bean.getString("segTag");
        this.elementPos = bean.getString("elementPos");
        this.componentPos = bean.getString("componentPos");
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

    public String getComponentPos() {
        return componentPos;
    }

    public void setComponentPos(String componentPos) {
        this.componentPos = componentPos;
    }
}
