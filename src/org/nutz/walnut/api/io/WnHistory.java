package org.nutz.walnut.api.io;

/**
 * 记录一个对象内容修改的一条历史记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnHistory {

    String oid();

    WnHistory oid(String oid);

    String data();

    WnHistory data(String data);
    
    boolean isSameData(String data);

    String sha1();

    WnHistory sha1(String sha1);

    boolean isSameSha1(String sha1);

    String owner();

    WnHistory owner(String ow);

    long len();

    WnHistory len(long len);

    long nanoStamp();

    WnHistory nanoStamp(long nano);

}