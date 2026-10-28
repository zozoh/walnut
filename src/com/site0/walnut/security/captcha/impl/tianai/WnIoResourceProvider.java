package com.site0.walnut.security.captcha.impl.tianai;

import java.io.InputStream;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;

import cloud.tianai.captcha.resource.ResourceProvider;
import cloud.tianai.captcha.resource.common.model.dto.Resource;

/**
 * 从 Walnut 对象存储（{@link WnIo}）读取验证码资源（背景图/字体）的提供者。
 *
 * <p>
 * 资源类型为 <code>wnio</code>，{@link Resource#getData()} 是对象存储里的
 * 文件路径（如 <code>~/.captcha/bg/a.jpg</code>）。
 */
public class WnIoResourceProvider implements ResourceProvider {

    public static final String TYPE = "wnio";

    private WnIo io;

    private NutBean vars;

    public WnIoResourceProvider(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    @Override
    public InputStream getResourceInputStream(Resource data) {
        WnObj o = Wn.checkObj(io, vars, data.getData());
        return io.getInputStream(o, 0);
    }

    @Override
    public boolean supported(Resource resource) {
        return null != resource && TYPE.equals(resource.getType());
    }

    @Override
    public String getName() {
        return TYPE;
    }

}
