package org.nutz.walnut.impl.srv;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnUsrService;

/**
 * 所有扩展服务类的基类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnService {

    @Inject("refer:io")
    protected WnIo io;

    @Inject("refer:usrService")
    protected WnUsrService usrService;

}
