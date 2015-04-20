package org.nutz.walnut.api.io;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.WnBean;

public interface WnObj extends WnNode, NutBean {

    String link();

    WnBean link(String lid);

    boolean isType(String tp);

    String type();

    WnBean type(String tp);

    String mime();

    WnBean mime(String mime);

    boolean hasSha1();

    String sha1();

    WnBean sha1(String sha1);

    boolean isSameSha1(String sha1);

    boolean hasData();

    String data();

    WnBean data(String data);

    long len();

    WnBean len(long len);

    int remain();

    WnBean remain(int remain);

    String creator();

    WnBean creator(String creator);

    String group();

    WnBean group(String grp);

    int mode();

    WnBean mode(int md);

    String d0();

    WnBean d0(String d0);

    String d1();

    WnBean d1(String d1);

    WnBean update(NutMap map);

    String[] labels();

    WnBean labels(String[] lbs);

    Date createTime();

    WnBean createTime(Date ct);

    Date lastModified();

    long nanoStamp();

    WnBean nanoStamp(long nano);

    boolean equals(Object obj);

    NutMap toMap4Update(String regex);

}
