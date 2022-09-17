package org.nutz.walnut.ooml.measure.convertor;

public class DxaPtConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input / 20;
    }

}
