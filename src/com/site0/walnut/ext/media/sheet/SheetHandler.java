package com.site0.walnut.ext.media.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.ext.media.sheet.impl.SheetResult;

public interface SheetHandler {

    SheetResult read(InputStream ins, NutMap conf);

    void write(OutputStream ops, List<NutBean> list, List<String> headKeys, NutMap conf);

    void setProcess(WnOutputable out, String process);

    void setProcess(WnOutputable out, WnTmpl process);

}
