package com.site0.walnut.ooml.measure.convertor;

public class EmusInchConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input / 914400;
    }

}