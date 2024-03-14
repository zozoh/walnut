package com.site0.walnut.ext.data.fake.impl;

import com.site0.walnut.ext.data.fake.WnFaker;

public class WnStaticFaker implements WnFaker<Object> {

    private Object value;

    public WnStaticFaker(Object value) {
        this.value = value;
    }

    @Override
    public Object next() {
        return this.value;
    }

}
