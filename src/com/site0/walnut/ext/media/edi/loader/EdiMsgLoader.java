package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;

public interface EdiMsgLoader<T> {

    Class<T> getResultType();

    T load(EdiMessage msg);

}
