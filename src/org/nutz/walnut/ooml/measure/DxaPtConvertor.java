package org.nutz.walnut.ooml.measure;

public class DxaPtConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input / 20;
    }

}
