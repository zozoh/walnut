package org.nutz.walnut.util.tmpl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class WnTmplRenderContext {

    public WnTmplRenderContext() {
        this(null);
    }

    public WnTmplRenderContext(NutBean context) {
        this.context = null == context ? new NutMap() : context;
        this.sb = new StringBuilder();
    }

    public WnTmplRenderContext(NutBean context, boolean showKey) {
        this.sb = new StringBuilder();
        this.context = null == context ? new NutMap() : context;
        this.showKey = showKey;
    }

    public WnTmplRenderContext(StringBuilder sb, boolean showKey) {
        this.sb = null == sb ? new StringBuilder() : sb;
        this.context = new NutMap();
        this.showKey = showKey;
    }

    public WnTmplRenderContext(StringBuilder sb, NutBean context) {
        this.sb = null == sb ? new StringBuilder() : sb;
        this.context = null == context ? new NutMap() : context;
    }

    public WnTmplRenderContext(StringBuilder sb, NutBean context, boolean showKey) {
        this.sb = null == sb ? new StringBuilder() : sb;
        this.context = null == context ? new NutMap() : context;
        this.showKey = showKey;
    }

    public StringBuilder sb;

    public NutBean context;

    public boolean showKey;

}
