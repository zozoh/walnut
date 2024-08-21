package com.site0.walnut.ext.media.edi.bean.segment;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment:	UCM: Message Response
 *
 * @author jrrx
 */
public class ICS_UCM {

    // 入站报文的唯一交换标识符
    private String inMsgRefNum;

    //  报文消息类型"短码"
    private String msgTypeId;

    // 4 --> 报文被拒绝；7 --> 相应引用级别已确认；8 --> 收到交换信息；
    private String msgActionCode;

    // [参考链接](https://www.abf.gov.au/help-and-support/ics/integrated-cargo-system-(ics)/software-developers/reference-materials/code-list/control-message-syntax-error-codes)
    private String msgSyntaxErrorCode;

    // 出错报文段的段标记标识符
    private String msgWrapperSegTag;

    // 数据段中错误的数据元素位置
    private String dataElementPos;

    // 组件数据元素位置错误
    private String componentDataPos;

    private List<ICS_UCS> segsErr = new ArrayList<>();

    public ICS_UCM() {
    }

    public ICS_UCM(EdiSegment ucmSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                "inMsgRefNum", "msgTypeId,,",
                "msgActionCode", "msgSyntaxErrorCode",
                "msgWrapperSegTag", "dataElementPos,componentDataPos");
        ucmSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCM valueOf(NutBean bean) {
        this.inMsgRefNum = bean.getString("inMsgRefNum");
        this.msgTypeId = bean.getString("msgTypeId");
        this.msgActionCode = bean.getString("msgActionCode");
        this.msgSyntaxErrorCode = bean.getString("msgSyntaxErrorCode");
        this.msgWrapperSegTag = bean.getString("msgWrapperSegTag");
        this.dataElementPos = bean.getString("dataElementPos");
        this.componentDataPos = bean.getString("componentDataPos");
        return this;
    }

    public String getInMsgRefNum() {
        return inMsgRefNum;
    }

    public void setInMsgRefNum(String inMsgRefNum) {
        this.inMsgRefNum = inMsgRefNum;
    }

    public String getMsgTypeId() {
        return msgTypeId;
    }

    public void setMsgTypeId(String msgTypeId) {
        this.msgTypeId = msgTypeId;
    }

    public String getMsgActionCode() {
        return msgActionCode;
    }

    public void setMsgActionCode(String msgActionCode) {
        this.msgActionCode = msgActionCode;
    }

    public String getMsgSyntaxErrorCode() {
        return msgSyntaxErrorCode;
    }

    public void setMsgSyntaxErrorCode(String msgSyntaxErrorCode) {
        this.msgSyntaxErrorCode = msgSyntaxErrorCode;
    }

    public String getMsgWrapperSegTag() {
        return msgWrapperSegTag;
    }

    public void setMsgWrapperSegTag(String msgWrapperSegTag) {
        this.msgWrapperSegTag = msgWrapperSegTag;
    }

    public String getDataElementPos() {
        return dataElementPos;
    }

    public void setDataElementPos(String dataElementPos) {
        this.dataElementPos = dataElementPos;
    }

    public String getComponentDataPos() {
        return componentDataPos;
    }

    public void setComponentDataPos(String componentDataPos) {
        this.componentDataPos = componentDataPos;
    }

    public List<ICS_UCS> getSegsErr() {
        return segsErr;
    }

    public void setSegsErr(List<ICS_UCS> segsErr) {
        this.segsErr = segsErr;
    }
}
