package org.nutz.walnut.ooml.measure.convertor;

public class EmusCmConvertor implements MeasureConvertor {

    @Override
    public double translate(double input) {
        return input / 360000;
    }

}
