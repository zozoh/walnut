package org.nutz.walnut.ooml.measure;

public class CmInchConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input / 2.54;
    }

}
