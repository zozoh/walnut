package com.site0.walnut.ext.media.edi.msg.reply.contrl;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment UCM: Message Response
 * 1. 模板: `UCM+{InbRefNum}+{MsgTypeId}:{MsgTypeVers}:{MsgTypeRelNum}:UN+{ActionCode}+{ErrorCode}+{SegmentTag}+{DataElementPosn}:{ComponentDataPosn}'`
 * 2. 样例: `UCM+1+CUSCAR:D:99B:UN+7'`
 * 3. 变量-InbRefNum: 入站消息的 Message 的 refNum;
 * 4. 变量-MsgTypeId: 和注册报文中的 `Message type identifier` 一致，固定为 `CUSCAR`: Customs cargo report message。
 * 5. 变量-MsgTypeVers: 固定为 `D`;
 * 6. 变量-MsgTypeRelNum: 固定为 `99B`;
 * 7. 变量-ActionCode: 同 `UCI` 中的 ActionCode。
 * 8. 后续变量同 `UCI` 中的变量，若无错误，不会渲染。
 *
 * @author jrrx
 */
public class CntrMsgRes {

    // 发送的 interchange 中 Message 的 refNum;
    private String refNum;

    //  报文消息类型"短码"
    private String type;

    // 4 --> 报文被拒绝；7 --> 相应引用级别已确认；8 --> 收到交换信息；
    private String actionCode;

    // [参考链接](https://www.abf.gov.au/help-and-support/ics/integrated-cargo-system-(ics)/software-developers/reference-materials/code-list/control-message-syntax-error-codes)
    private String errCode;

    // 出错报文段的段标记标识符
    private String segTag;

    // 数据段中错误的数据元素位置
    private String elementPos;

    // 组件数据元素位置错误
    private String componentPos;

    private List<CntrSegErr> segsErr = new ArrayList<>();

    public CntrMsgRes() {
    }

    public CntrMsgRes(EdiSegment ucmSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                "refNum", "type,,",
                "actionCode", "errCode",
                "segTag", "elementPos,componentPos");
        ucmSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public CntrMsgRes valueOf(NutBean bean) {
        this.refNum = bean.getString("refNum");
        this.type = bean.getString("type");
        this.actionCode = bean.getString("actionCode");
        this.errCode = bean.getString("errCode");
        this.segTag = bean.getString("segTag");
        this.elementPos = bean.getString("elementPos");
        this.componentPos = bean.getString("componentPos");
        return this;
    }

    public String getRefNum() {
        return refNum;
    }

    public void setRefNum(String refNum) {
        this.refNum = refNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<CntrSegErr> getSegsErr() {
        return segsErr;
    }

    public void setSegsErr(List<CntrSegErr> segsErr) {
        this.segsErr = segsErr;
    }
}
