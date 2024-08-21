package com.site0.walnut.ext.media.edi.bean.segment;


import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

/**
 * UCD: Data Element Error Indication
 *
 * @author jrrx
 */
public class ICS_UCD {

    // UCS 中错误的报文行的 DataElement 的行内位置 (+号分隔)
    private String elementPos;

    // 特定 Elment 中 component 的位置 (:号分隔)
    private String componentPos;

    // 报文行 具体 DataElement 的 具体 component 的具体错误code
    private String errCode;


    public ICS_UCD() {
    }

    public ICS_UCD(EdiSegment ucdSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "errCode", "elementPos,componentPos");
        ucdSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCD valueOf(NutBean bean) {
        this.elementPos = bean.getString("elementPos");
        this.componentPos = bean.getString("componentPos");
        this.errCode = bean.getString("errCode");
        return this;
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

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        errCode = errCode;
    }
}
