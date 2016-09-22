package org.nutz.walnut.impl.srv;

import org.nutz.walnut.api.io.WnIo;

/**
 * 所有扩展服务类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnService {

    protected WnIo io;

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void on_create() {}

}
