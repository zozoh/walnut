package org.nutz.walnut.ext.data.fake.impl;

import java.util.Collection;

import org.nutz.walnut.ext.data.fake.WnFaker;
import org.nutz.walnut.util.Wuu;

public class WnEnumFaker implements WnFaker<Object> {

    private Object[] inputs;

    public WnEnumFaker(Collection<?> cols) {
        this.inputs = new Object[cols.size()];
        cols.toArray(this.inputs);
    }

    public WnEnumFaker(Object[] inputs) {
        this.inputs = inputs;
    }

    @Override
    public Object next() {
        if (null == inputs || inputs.length == 0) {
            return null;
        }
        if (1 == inputs.length) {
            return inputs[0];
        }
        int i = Wuu.random(0, inputs.length);
        return inputs[i];
    }

}
