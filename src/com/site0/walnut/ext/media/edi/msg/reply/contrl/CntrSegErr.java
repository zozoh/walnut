package com.site0.walnut.ext.media.edi.msg.reply.contrl;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment Group 2: UCS-UCD, SG2包括 UCS 和 UCD 报文行
 * <p>
 * ## SG2-`UCS` Segment Error Indication (M-1)
 * 1. 对应
 * 2. SG2 是可选的，如果无错误，那么 UCS-UCD 报文行是不会出现的。
 * 3. 模板： `UCS+{SegPos}+{ErrorCode}'`
 * 4. 变量-SegPos: 一条信息中某段的数字计数位置。
 * 5. 变量-ErrorCode: 信息中语法错误的代码 Code。
 *
 * @author jrrx
 */
public class CntrSegErr {

    // 报文消息中 "有错误的" 报文行(segment) 的行数(数字计数位置), 从 UNH 开始计算行数。
    private String segPos;

    // 报文行的语法错误 code
    private String errCode;

    private List<CntrEleErr> errItems = new ArrayList<>();


    public CntrSegErr() {
    }

    public CntrSegErr(EdiSegment ucsSeg) {
        NutMap bean = new NutMap();
        String[] keys = Wlang.array(null, "segPos", "errCode");
        ucsSeg.fillBean(bean, keys);
        this.valueOf(bean);
    }

    public CntrSegErr valueOf(NutBean bean) {
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

    public List<CntrEleErr> getErrItems() {
        return errItems;
    }

    public void setErrItems(List<CntrEleErr> errItems) {
        this.errItems = errItems;
    }
}
