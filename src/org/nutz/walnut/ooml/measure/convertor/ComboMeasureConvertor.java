package org.nutz.walnut.ooml.measure.convertor;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ooml.Oomls;

public class ComboMeasureConvertor implements MeasureConvertor {

    private MeasureConvertor[] convertors;

    public ComboMeasureConvertor(String... names) {
        this.convertors = new MeasureConvertor[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            MeasureConvertor mc = Oomls.getMeasureConvertor(name);
            if (null == mc) {
                throw Er.create("e.measure.InvalidCovertor", name);
            }
            this.convertors[i] = mc;
        }
    }

    @Override
    public double translate(double input) {
        double re = input;
        for (int i = 0; i < convertors.length; i++) {
            MeasureConvertor mc = convertors[i];
            re = mc.translate(re);
        }
        return re;
    }

}
