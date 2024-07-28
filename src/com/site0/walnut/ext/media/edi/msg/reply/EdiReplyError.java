package com.site0.walnut.ext.media.edi.msg.reply;

import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_FTX;

/**
 * <pre>
 * ERP+1'
 * ERC+ERROR:80:95'
 * ERC+CL0404:6:95'
 * FTX+AAO+++ADDRESS TYPE SUPPLIED ALONG WITH ABN BUT NO CAC SUPPLIED'
 * </pre>
 * 
 * @author HUAWEI
 *
 */
public class EdiReplyError {

    private String type;

    private String code;

    private String content;

    public EdiReplyError() {}

    public EdiReplyError(List<EdiSegment> segs) {
        for (EdiSegment seg : segs) {
            // 错误类型
            if (seg.is("ERC", "^(ERROR|WARN|ADVICE)$")) {
                NutBean bean = seg.getBean(null, "type,,");
                this.type = bean.getString("type");
            }
            // 错误编码
            else if (seg.is("ERC")) {
                NutBean bean = seg.getBean(null, "code,,");
                this.code = bean.getString("code");
            }
            // 错误消息
            else if (seg.is("FTX", "AAO")) {
                ICS_FTX ftx = new ICS_FTX(seg);
                this.content = ftx.getLiteral();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", type, code, content);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
