package com.site0.walnut.util.tmpl.segment;

import com.site0.walnut.util.tmpl.WnTmplRenderContext;
import com.site0.walnut.util.tmpl.util.BreakSegmentException;

public class BreakTmplSegment extends AbstractTmplSegment {

    @Override
    public void renderTo(WnTmplRenderContext rc) {
        throw new BreakSegmentException();
    }
}
