package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.walnut.ext.media.edi.bean.EdiMessage;

public interface EdiMsgLoader<T> {

    Class<T> getResultType();

    T load(EdiMessage msg);

}
