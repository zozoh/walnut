package org.nutz.walnut.ooml.measure;

public class PxPtConvertor extends DpiConvertor {

    public PxPtConvertor(int dpi) {
        super(dpi);
    }

    @Override
    public double translate(double input) {
        return input * 72 / dpi;
    }

}
