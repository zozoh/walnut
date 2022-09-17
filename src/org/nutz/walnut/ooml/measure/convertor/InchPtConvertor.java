package org.nutz.walnut.ooml.measure.convertor;

public class InchPtConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input * 72;
    }

}
