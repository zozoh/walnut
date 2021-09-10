package org.nutz.walnut.cheap.api;

import org.nutz.walnut.cheap.dom.bean.CheapResource;

public interface CheapResourceLoader {

    CheapResource loadByPath(String path);

    CheapResource loadById(String id);

    String getMime(String typeName);

}
