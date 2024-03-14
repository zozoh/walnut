package com.site0.walnut.core;

import com.site0.walnut.api.io.MimeMap;

public abstract class IoCoreTest {

    protected IoCoreSetup setup;

    public IoCoreTest() {
        setup = new IoCoreSetup();
    }

    protected MimeMap mimes() {
        return this.setup.getMimes();
    }

}
