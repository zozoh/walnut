package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;

public interface SheetHandler {

    List<NutMap> read(InputStream ins, NutMap conf);

    void write(OutputStream ops, List<NutMap> list, NutMap conf);

    void setProcess(WnOutputable out, String process);

    void setProcess(WnOutputable out, Tmpl process);

}
