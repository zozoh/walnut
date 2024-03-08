package org.nutz.walnut.util.tmpl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public class WnTmplRenderContext {

    public WnTmplRenderContext() {
        this(null);
    }

    public WnTmplRenderContext(NutBean context) {
        this.context = null == context ? new NutMap() : context;
        this.out = new StringBuilder();
    }

    public WnTmplRenderContext(NutBean context, boolean showKey) {
        this.out = new StringBuilder();
        this.context = null == context ? new NutMap() : context;
        this.showKey = showKey;
    }

    public WnTmplRenderContext(StringBuilder sb, boolean showKey) {
        this.out = null == sb ? new StringBuilder() : sb;
        this.context = new NutMap();
        this.showKey = showKey;
    }

    public WnTmplRenderContext(StringBuilder sb, NutBean context) {
        this.out = null == sb ? new StringBuilder() : sb;
        this.context = null == context ? new NutMap() : context;
    }

    public WnTmplRenderContext(StringBuilder sb, NutBean context, boolean showKey) {
        this.out = null == sb ? new StringBuilder() : sb;
        this.context = null == context ? new NutMap() : context;
        this.showKey = showKey;
    }

    public StringBuilder out;

    public NutBean context;

    public boolean showKey;

}
