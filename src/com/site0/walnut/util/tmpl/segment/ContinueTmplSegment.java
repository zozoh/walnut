package com.site0.walnut.util.tmpl.segment;

import com.site0.walnut.util.tmpl.WnTmplRenderContext;
import com.site0.walnut.util.tmpl.util.ContinueSegmentException;

public class ContinueTmplSegment extends AbstractTmplSegment {

    @Override
    public void renderTo(WnTmplRenderContext rc) {
        throw new ContinueSegmentException();
    }

}
