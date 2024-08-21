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

    private List<ICS_UCS> segsErr = new ArrayList<>();

    public ICS_UCM() {
    }

    public ICS_UCM(EdiSegment ucmSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null,
                "refNum", "type,,",
                "actionCode", "errCode",
                "segTag", "elementPos,componentPos");
        ucmSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCM valueOf(NutBean bean) {
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

    public List<ICS_UCS> getSegsErr() {
        return segsErr;
    }

    public void setSegsErr(List<ICS_UCS> segsErr) {
        this.segsErr = segsErr;
    }
}
