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
    private String msgSegPosition;

    // 报文行的语法错误 code
    private String msgSegSyntaxErrCode;

    private List<ICS_UCD> errItems = new ArrayList<>();


    public ICS_UCS() {
    }

    public ICS_UCS(EdiSegment ucsSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "msgSegPosition", "msgSegSyntaxErrCode");
        ucsSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public ICS_UCS valueOf(NutBean bean) {
        this.msgSegPosition = bean.getString("msgSegPosition");
        this.msgSegSyntaxErrCode = bean.getString("msgSegSyntaxErrCode");
        return this;
    }

    public String getMsgSegPosition() {
        return msgSegPosition;
    }

    public void setMsgSegPosition(String msgSegPosition) {
        this.msgSegPosition = msgSegPosition;
    }

    public String getMsgSegSyntaxErrCode() {
        return msgSegSyntaxErrCode;
    }

    public void setMsgSegSyntaxErrCode(String msgSegSyntaxErrCode) {
        this.msgSegSyntaxErrCode = msgSegSyntaxErrCode;
    }

    public List<ICS_UCD> getErrItems() {
        return errItems;
    }

    public void setErrItems(List<ICS_UCD> errItems) {
        this.errItems = errItems;
    }
}
