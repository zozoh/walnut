package com.site0.walnut.ext.media.edi.bean.segment;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

/**
 * UCS: Segment Error Indication
 *
 * @author jrrx
 */
public class ICS_UCS {

    // 报文消息中 "有错误的" 报文行(segment) 的行数(数字计数位置), 从 UNH 开始计算行数。
    private String segPos;

    // 报文行的语法错误 code
    private String errCode;

    private List<ICS_UCD> errItems = new ArrayList<>();


    public ICS_UCS() {
    }

    public ICS_UCS(EdiSegment ucsSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "segPos", "errCode");
        ucsSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCS valueOf(NutBean bean) {
        this.segPos = bean.getString("segPos");
        this.errCode = bean.getString("errCode");
        return this;
    }

    public String getSegPos() {
        return segPos;
    }

    public void setSegPos(String segPos) {
        this.segPos = segPos;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public List<ICS_UCD> getErrItems() {
        return errItems;
    }

    public void setErrItems(List<ICS_UCD> errItems) {
        this.errItems = errItems;
    }
}
