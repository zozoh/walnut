package org.nutz.walnut.impl.box;

import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxService;

public class JvmBoxService implements WnBoxService {

    @Override
    public WnBox get(String boxId) {
        return null;
    }

    @Override
    public WnBox alloc(long timeout) {
        return null;
    }

    @Override
    public void free(WnBox box) {}

}
