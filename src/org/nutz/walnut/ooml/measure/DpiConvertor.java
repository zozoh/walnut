package org.nutz.walnut.ooml.measure;

public abstract class DpiConvertor implements MeasureConvertor {

    public static final int DPI_WIN = 96;
    public static final int DPI_MAC = 72;

    /**
     * DPI (Dots Per Inch, 每英寸像素数)
     * <ul>
     * <li><b>Windows</b> : <code>96</code>
     * <li><b>MacOS</b> : <code>72</code>
     * </ul>
     *
     */
    protected double dpi;

    public DpiConvertor(int dpi) {
        this.dpi = dpi;
    }

}
