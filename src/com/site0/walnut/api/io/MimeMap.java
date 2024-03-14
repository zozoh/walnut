package com.site0.walnut.api.io;

import java.util.Set;

/**
 * 根据文件类型名，猜测文件的 MIME 类型
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MimeMap {

    String getMime(String type);

    String getMime(String type, String dftMime);

    Set<String> keys();

}
