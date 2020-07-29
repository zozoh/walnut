package org.nutz.walnut.core;

import org.nutz.walnut.api.io.MimeMap;

public abstract class IoCoreTest {

    protected IoCoreSetup setup;

    public IoCoreTest() {
        setup = new IoCoreSetup();
    }

    protected MimeMap mimes() {
        return this.setup.getMimes();
    }

}
