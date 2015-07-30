package org.nutz.walnut.api.io;

public interface WnSecurity {

    WnObj enter(WnObj nd);

    WnObj access(WnObj nd);

    WnObj view(WnObj nd);

    WnObj read(WnObj nd);

    WnObj write(WnObj nd);
    
    WnObj remove(WnObj nd);

}
