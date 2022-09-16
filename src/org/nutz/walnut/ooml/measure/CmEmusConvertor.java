package org.nutz.walnut.ooml.measure;

public class CmEmusConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input * 360000;
    }

}
