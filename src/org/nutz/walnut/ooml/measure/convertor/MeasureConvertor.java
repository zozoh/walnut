package org.nutz.walnut.ooml.measure.convertor;

/**
 * 提供一个转换接口，子类将决定要转换的单位:
 * 
 * <pre>
 * 在 ooml_measure.md 里，有关于单位转换的一切
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MeasureConvertor {

    /**
     * @param input
     *            输入尺度
     * @return 输出尺度
     */
    double translate(double input);

}
