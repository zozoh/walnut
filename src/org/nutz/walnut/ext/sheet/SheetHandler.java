package org.nutz.walnut.ext.sheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.lang.util.NutMap;

public interface SheetHandler {

    List<NutMap> read(InputStream ins, NutMap conf);

    void write(OutputStream ops, List<NutMap> list, NutMap conf);

}