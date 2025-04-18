package com.site0.walnut.ext.sys.dsync.bean;

import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WnRestoreAction {

    private Object test;

    private String run;

    private boolean replaceDom;

    private boolean parseDomAsBody;

    private WnMatch _match;

    public String getTypeName() {
        StringBuilder sb = new StringBuilder();
        if (this.hasRun()) {
            sb.append("run");
        }
        if (this.replaceDom) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append("dom");
        }
        return sb.toString();
    }

    public boolean match(WnObj o) {
        if (null == test) {
            return false;
        }
        if (null == _match) {
            _match = new AutoMatch(test);
        }
        return _match.match(o);
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
    }

    private WnTmpl _run_tmpl;

    public String getRunCommand(NutBean context) {
        // 直接渲染
        if (null != _run_tmpl) {
            return _run_tmpl.render(context);
        }
        // 木有命令模板
        if (!this.hasRun()) {
            return null;
        }
        // 编译一下命令模板
        _run_tmpl = WnTmpl.parse(run);
        // 渲染命令
        return _run_tmpl.render(context);
    }

    public boolean hasRun() {
        return !Ws.isBlank(run);
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public boolean isReplaceDom() {
        return replaceDom;
    }

    public void setReplaceDom(boolean replaceDom) {
        this.replaceDom = replaceDom;
    }

    public boolean isParseDomAsBody() {
        return parseDomAsBody;
    }

    public void setParseDomAsBody(boolean parseDomAsBody) {
        this.parseDomAsBody = parseDomAsBody;
    }

}
