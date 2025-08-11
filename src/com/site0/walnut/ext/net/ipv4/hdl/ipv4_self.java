package com.site0.walnut.ext.net.ipv4.hdl;

import com.site0.walnut.ext.net.ipv4.Ipv4Context;
import com.site0.walnut.ext.net.ipv4.Ipv4Filter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class ipv4_self extends Ipv4Filter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(strict)$");
    }

    @Override
    protected void process(WnSystem sys, Ipv4Context fc, ZParams params) {
        fc.ip = Wn.WC().getIPv4();
    }

}
