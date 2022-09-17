package org.nutz.walnut.ooml.measure.convertor;

public class InchCmConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input * 2.54;
    }

}
