package org.nutz.walnut.ooml.measure.convertor;

public class PtPxConvertor extends DpiConvertor {

    public PtPxConvertor(int dpi) {
        super(dpi);
    }

    @Override
    public double translate(double input) {
        return input * dpi / 72;
    }

}
