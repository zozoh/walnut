package org.nutz.walnut.api.io;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public interface WnObj extends WnNode, NutBean {

    WnObj setNode(WnNode nd);

    String link();

    WnObj link(String lid);

    boolean isType(String tp);

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

    long len();

    WnObj len(long len);

    int remain();

    WnObj remain(int remain);

    String creator();

    WnObj creator(String creator);

    String mender();

    WnObj mender(String mender);

    String group();

    WnObj group(String grp);

    int mode();

    WnObj mode(int md);

    String d0();

    WnObj d0(String d0);

    String d1();

    WnObj d1(String d1);

    WnObj update(NutMap map);

    String[] labels();

    WnObj labels(String[] lbs);

    Date createTime();

    WnObj createTime(Date ct);

    Date lastModified();

    long nanoStamp();

    WnObj nanoStamp(long nano);

    boolean equals(Object obj);

    NutMap toMap4Update(String regex);

}
