package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.ext.sheet.impl.SheetResult;

public interface SheetHandler {

    SheetResult read(InputStream ins, NutMap conf);

    void write(OutputStream ops, List<NutMap> list, NutMap conf);

    void setProcess(WnOutputable out, String process);

    void setProcess(WnOutputable out, Tmpl process);

}
