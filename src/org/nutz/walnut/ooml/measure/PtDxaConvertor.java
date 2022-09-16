package org.nutz.walnut.ooml.measure;

public class PtDxaConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input * 20;
    }

}
