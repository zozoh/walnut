package org.nutz.walnut.api.io;

public interface WnSecurity {

    WnNode enter(WnNode nd);

    WnNode access(WnNode nd);

    WnNode view(WnNode nd);

    WnNode read(WnNode nd);

    WnNode write(WnNode nd);
    
    WnNode remove(WnNode nd);

}
