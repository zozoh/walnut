package com.site0.walnut.ooml.measure.convertor;

public class InchEmusConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return 914400 * input;
    }

}
