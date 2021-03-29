package org.nutz.walnut.util.archive.impl;

import java.io.IOException;

import org.nutz.walnut.util.archive.WnArchiveWriting;

public abstract class AbstractWnArchiveWriting implements WnArchiveWriting {

    @Override
    public void addFileEntry(String entryName, byte[] b) throws IOException {
        this.addFileEntry(entryName, b, 0, b.length);
    }

}
