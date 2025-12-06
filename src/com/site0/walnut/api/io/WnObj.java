package com.site0.walnut.api.io;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.core.bean.WnObjId;

public interface WnObj extends NutBean, Comparable<WnObj> {

    // WnTree tree();
    //
    // WnObj setTree(WnTree tree);

    boolean isRootNode();

    String id();

    WnObj id(String id);

    WnObjId OID();

    String myId();

    boolean hasWriteHandle();

    String getWriteHandle();

    WnObj setWriteHandle(String hid);

    boolean hasID();

    boolean isSameId(WnObj o);

    boolean isSameId(String id);

    boolean isSameName(String nm);

    boolean isMyParent(WnObj p);

    boolean isMyAncestor(WnObj an);

    boolean isRWMeta();

    WnObj setRWMeta(boolean rwmeta);

    boolean hasRWMetaKeys();

    String getRWMetaKeys();

    WnObj setRWMetaKeys(String regex);

    WnObj clearRWMetaKeys();

    String getPath();

    String path();

    WnObj path(String path);

    /**
     * @return 对象全路径，如果是目录，则一定为 "/" 结尾
     */
    String getRegularPath();

    /**
     * 获取统一形式的路径，即，如果路径在主目录中，用 "~" 开头。
     * <p>
     * 这个判断主要依靠 `d0` 和 `d1`，即，如果 `d0` 为 "home" 且如果路径已 `/{$d0}/{$d1}` 开头，则替换为 `~`
     * 
     * @param isRegular
     *            是否规范路径
     * @return 统一形式的路径
     * 
     * @see #getRegularPath()
     */
    String getFormedPath(boolean isRegular);

    WnObj appendPath(String path);

    String name();

    WnObj name(String nm);

    WnRace race();

    WnObj race(WnRace race);

    boolean isRace(WnRace race);

    boolean isDIR();

    boolean isFILE();

    boolean isHidden();

    boolean hasParent();

    WnObj parent();

    List<WnObj> parents();

    NutBean getCustomizedPrivilege();

    /**
     * 将自己以及自己所有的祖先与给定的自定义权限融合
     * <p>
     * 权限集合键有下面几种形式：
     * <ul>
     * <li><code>4a98..a123</code> : 直接是用户的 ID
     * <li><code>@SYS_ADMIN</code> : 角色【域账户】
     * <li><code>@[demo]</code> : 用户名【域账户】
     * <li><code>+M0A</code> : 用户所属职位或部门
     * </ul>
     * 
     * 值可以是
     * <ul>
     * <li><code>"0777"</code> : 八进制
     * <li><code>511</code> : 十进制
     * <li><code>"rwxr-xr-x"</code> : 全文本
     * <li><code>"rwx"</code> : 文本，相当于 "rwxrwxrwx"
     * <li><code>7</code> : 0-7 整数相当于 "0777"
     * </ul>
     * 
     * 值的混合模式:
     * <ul>
     * <li><code>DEFAULT</code> : 默认混合。支持<code>"0777",</code>
     * <li><code>WEAK</code> : <code>`+DA:-7` 或者 `+DA:"~0777"`</code>
     * <li><code>STRONG</code> : <code>`+DA:"!rwx"` 或者 `+DA:"!0777"`</code>
     * </ul>
     * 
     * 
     * @param pvg
     *            输入的自定义权限集
     * 
     * @return 融合后的自定义权限集合
     */
    NutBean joinCustomizedPrivilege(NutBean pvg);

    WnObj loadParents(List<WnObj> list, boolean force);

    void setParent(WnObj parent);

    String parentId();

    String mount();

    WnObj mount(String mnt);

    boolean hasMountRootId();

    String mountRootId();

    WnObj mountRootId(String mrid);

    /**
     * 判断一个对象是不是挂载对象，挂载点以及其内的对象都是挂载对象
     * 
     * @return
     */
    boolean isMount();

    /**
     * 判断当前对象是否是挂载入口对象，这个对象被全局管理器管理 且其设置了mnt属性
     */
    boolean isMountEntry();

    /**
     * 判断当前对象挂载点内对象
     */
    boolean isMountedObj();

    long len();

    long lastModified();

    WnObj lastModified(long lm);

    String toString();

    WnObj clone();

    WnObj update(Map<? extends String, ? extends Object> map);

    WnObj updateBy(WnObj o);

    boolean isFromLink();

    String fromLink();

    WnObj fromLink(String link);

    boolean isLink();

    String link();

    WnObj link(String lid);

    boolean isType(String tp);

    boolean hasType();

    String type();

    WnObj type(String tp);

    String mime();

    WnObj mime(String mime);

    boolean hasMime();

    boolean isMime(String mime);

    boolean hasSha1();

    String sha1();

    WnObj sha1(String sha1);

    boolean isSameSha1(String sha1);

    boolean hasThumbnail();

    String thumbnail();

    WnObj thumbnail(String thumbnail);

    @Deprecated
    boolean hasData();

    @Deprecated
    String data();

    @Deprecated
    WnObj data(String data);

    @Deprecated
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

    String[] dN();

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

    boolean equals(Object obj);

    NutMap toMap4Update(String regex);

    NutMap toMap(String regex);

}
