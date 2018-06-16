package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.util.NutMap;

public interface SheetHandler {

    List<NutMap> read(NutMap opt, InputStream ins);

    void write(NutMap opt, OutputStream ops, List<NutMap> list);

}
