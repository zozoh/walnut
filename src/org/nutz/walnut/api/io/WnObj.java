package org.nutz.walnut.api.io;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public interface WnObj extends WnNode, NutBean {

    WnObj setNode(WnNode nd);

    boolean isLink();

    String link();

    WnObj link(String lid);

    boolean isType(String tp);

    boolean hasType();

    String type();

    WnObj type(String tp);

    String mime();

    WnObj mime(String mime);

    boolean hasSha1();

    String sha1();

    WnObj sha1(String sha1);

    boolean isSameSha1(String sha1);

    boolean hasData();

    String data();

    WnObj data(String data);

    boolean isSameData(String data);

    WnObj len(long len);

    int remain();

    WnObj remain(int remain);

    WnObj creator(String creator);

    WnObj mender(String mender);

    WnObj group(String grp);

    WnObj mode(int md);

    String creator();

    String mender();

    String group();

    int mode();

    String d0();

    WnObj d0(String d0);

    String d1();

    WnObj d1(String d1);

    WnObj update(NutMap map);

    String[] labels();

    WnObj labels(String[] lbls);

    long createTime();

    WnObj createTime(long ct);

    long expireTime();

    WnObj expireTime(long expi);

    long syncTime();

    WnObj syncTime(long st);

    boolean isExpired();

    boolean isExpiredBy(long now);

    WnObj nanoStamp(long nano);

    boolean equals(Object obj);

    NutMap toMap4Update(String regex);

    NutMap toMap(String regex);

}
