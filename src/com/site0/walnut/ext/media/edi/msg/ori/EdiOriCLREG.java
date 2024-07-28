package com.site0.walnut.ext.media.edi.msg.ori;

import org.nutz.lang.util.NutMap;

public class EdiOriCLREG extends EdiOriObj {

    public NutMap CLREG;

    public String purpose;

    public String[] clientRoles;

    public NutMap BP;

    public NutMap A;

    public NutMap C;

    public NutMap CA;

    public EdiOriCLREG() {
        CLREG = new NutMap();
        BP = new NutMap();
        A = new NutMap();
        C = new NutMap();
        CA = new NutMap();
    }

}
