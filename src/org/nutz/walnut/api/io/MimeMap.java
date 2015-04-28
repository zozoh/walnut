package org.nutz.walnut.api.io;

/**
 * 根据文件类型名，猜测文件的 MIME 类型
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface MimeMap {

    String getMime(String type);

    String getMime(String type, String dftMime);

}
