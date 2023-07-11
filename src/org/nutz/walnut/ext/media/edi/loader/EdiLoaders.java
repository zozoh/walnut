package org.nutz.walnut.ext.media.edi.loader;

/**
 * 封装报文解析的高级类的工厂方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class EdiLoaders {

    public static ICLoader getInterchangeLoader() {
        return new ICLoader();
    }

    public static class CLREG {

        public static CLREGRLoader getLoader() {
            return new CLREGRLoader();
        }
    }

}
