package org.nutz.walnut.impl.io;

import org.nutz.walnut.api.io.WnBucketSealStrategy;
import org.nutz.walnut.api.io.WnObj;

public class WnBucketSealStrategyImpl implements WnBucketSealStrategy {

    @Override
    public boolean shouldSealed(WnObj o) {
        // 图片和影音文件一律封盖
        if (o.mime().matches("^(image|video|audio)/.+$")) {
            return true;
        }
        
        // 其他不要封盖
        return false;
    }

}
