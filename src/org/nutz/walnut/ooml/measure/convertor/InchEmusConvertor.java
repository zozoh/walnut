package org.nutz.walnut.ooml.measure.convertor;

public class InchEmusConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return 914400 * input;
    }

}
