package com.site0.walnut.ext.net.ipv4.hdl;

import javax.servlet.http.HttpServletRequest;

import org.nutz.mvc.Mvcs;

import com.site0.walnut.ext.net.ipv4.Ipv4Context;
import com.site0.walnut.ext.net.ipv4.Ipv4Filter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.ZParams;

public class ipv4_self extends Ipv4Filter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(strict)$");
    }

    @Override
    protected void process(WnSystem sys, Ipv4Context fc, ZParams params) {
        HttpServletRequest req = Mvcs.getReq();
        boolean strict = params.is("strict", false);
        fc.ip = Wlang.getIP(req, strict);
    }

}
