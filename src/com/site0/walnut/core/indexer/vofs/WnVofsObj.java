package com.site0.walnut.core.indexer.vofs;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.json.ToJson;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.ext.xo.bean.XoBean;
import com.site0.walnut.ext.xo.impl.XoService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

@ToJson
public class WnVofsObj extends NutMap implements WnObj {

    WnObj oRoot;
    XoBean xo;
    private XoService api;
    private MimeMap mimes;

    public WnVofsObj(WnObj oRoot, XoService api, MimeMap mimes, XoBean xo) {
        this.oRoot = oRoot;
        this.api = api;
        this.mimes = mimes;
        this.xo = xo;
        this._put_all_to_map(this);
    }

    @Override
    public WnObj clone() {
        return new WnVofsObj(oRoot, api, mimes, xo);
    }

    @Override
    public int compareTo(WnObj o) {
        String myPath = path();
        String taPath = o.path();
        return myPath.compareTo(taPath);
    }

    @Override
    public boolean isRootNode() {
        return false;
    }

    private WnObjId _id;

    private String _ph;

    private String VID(String key) {
        return oRoot.id() + ":" + Wobj.encodePathToBase64(key);
    }

    public String getObjKey() {
        return xo.getKey();
    }

    @Override
    public WnObjId OID() {
        if (null == _id) {
            _id = Wobj.genPart2IDByVPath(oRoot, xo.getKey());
        }
        return _id;
    }

    @Override
    public String id() {
        return OID().toString();
    }

    @Override
    public WnObj id(String id) {
        throw Wlang.noImplement();
    }

    @Override
    public String myId() {
        return OID().getMyId();
    }

    @Override
    public boolean hasID() {
        return null != OID();
    }

    @Override
    public boolean isSameId(WnObj o) {
        return isSameId(o.id());
    }

    @Override
    public boolean isSameId(String id) {
        if (null == id)
            return false;
        return id().equals(id);
    }

    @Override
    public boolean isSameName(String nm) {
        return name().equals(nm);
    }

    @Override
    public boolean isMyParent(WnObj p) {
        if (null == p) {
            return false;
        }
        return p.isSameId(parentId());
    }

    @Override
    public boolean isMyAncestor(WnObj an) {
        String myPath = path();
        String taPath = an.path();
        return myPath.startsWith(taPath);
    }

    @Override
    public String getPath() {
        return path();
    }

    @Override
    public String path() {
        if (null == _ph) {
            String rootPath = this.oRoot.path();
            String key = xo.getKey();
            _ph = Wn.appendPath(rootPath, key);
        }
        return _ph;
    }

    @Override
    public String getRegularPath() {
        return path();
    }

    @Override
    public String getFormedPath(boolean isRegular) {
        return path();
    }

    @Override
    public String name() {
        return xo.getName();
    }

    @Override
    public WnRace race() {
        return xo.getRace();
    }

    @Override
    public boolean isRace(WnRace race) {
        return race == xo.getRace();
    }

    @Override
    public boolean isDIR() {
        return xo.isDIR();
    }

    @Override
    public boolean isFILE() {
        return xo.isFILE();
    }

    @Override
    public boolean isHidden() {
        return xo.getName().startsWith(".");
    }

    @Override
    public boolean hasParent() {
        return true;
    }

    public static String getParentKey(String key) {
        // 如果以 / 结束，那么删掉它，再搜索
        if (key.endsWith("/")) {
            key = key.substring(0, key.length() - 1);
        }
        int pos = key.lastIndexOf('/');

        if (pos <= 0) {
            return null;
        }
        // 获取自己上一级, (确保 / 结尾)
        return key.substring(0, pos + 1);
    }

    @Override
    public WnObj parent() {
        String pkey = getParentKey(xo.getKey());

        if (null == pkey) {
            return this.oRoot;
        }
        // 获取自己上一级, (确保 / 结尾)
        XoBean pvo = api.getObj(pkey);

        // 建立一个虚拟
        if (null == pvo) {
            pvo = _create_virtual_dir(pkey);
        }
        // 搞定
        return new WnVofsObj(oRoot, api, mimes, pvo);
    }

    private XoBean _create_virtual_dir(String pph) {
        if (!pph.endsWith("/")) {
            pph += "/";
        }
        XoBean pvo = new XoBean();
        pvo.setKey(pph);
        xo.setSize(0L);
        pvo.setLastModified(new Date(oRoot.lastModified()));
        pvo.setVirtual(true);
        return pvo;
    }

    @Override
    public List<WnObj> parents() {
        List<WnObj> list = new LinkedList<>();
        this.loadParents(list, false);
        return list;
    }

    @Override
    public NutBean getCustomizedPrivilege() {
        return oRoot.getCustomizedPrivilege();
    }

    @Override
    public NutBean joinCustomizedPrivilege(NutBean pvg) {
        return oRoot.joinCustomizedPrivilege(pvg);
    }

    @Override
    public WnObj loadParents(List<WnObj> list, boolean force) {
        // 加入父
        WnObj re = oRoot.loadParents(list, force);

        // 追加自己
        String[] path = Ws.splitIgnoreBlank(xo.getKey(), "/");
        int lastI = path.length - 1;
        for (int i = 1; i < lastI; i++) {
            String pph = Ws.join(path, "/", 0, i);
            XoBean pvo = _create_virtual_dir(pph);
            re = new WnVofsObj(oRoot, api, mimes, pvo);
            list.add(re);
        }

        return re;
    }

    @Override
    public String parentId() {
        String pkey = getParentKey(xo.getKey());
        if (null == pkey) {
            return this.oRoot.id();
        }
        return VID(pkey);
    }

    @Override
    public String mount() {
        return oRoot.mount();
    }

    @Override
    public boolean hasMountRootId() {
        return true;
    }

    @Override
    public String mountRootId() {
        return oRoot.id();
    }

    @Override
    public boolean isMount() {
        return true;
    }

    @Override
    public boolean isMountEntry() {
        return false;
    }

    @Override
    public boolean isMountedObj() {
        return false;
    }

    @Override
    public long len() {
        return xo.getSize();
    }

    @Override
    public long lastModified() {
        return xo.getLastModified().getTime();
    }

    @Override
    public long createTime() {
        return xo.getLastModified().getTime();
    }

    @Override
    public boolean isFromLink() {
        return false;
    }

    @Override
    public String fromLink() {
        return null;
    }

    @Override
    public WnObj fromLink(String link) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String link() {
        return null;
    }

    @Override
    public boolean hasType() {
        return !Strings.isBlank(type());
    }

    @Override
    public boolean isType(String tp) {
        String mytp = type();
        if (null == mytp)
            return null == tp;
        if (null == tp)
            return false;
        // 用正则
        if (tp.startsWith("^")) {
            return mytp.matches(tp);
        }
        // 精确匹配
        return mytp.equals(tp);
    }

    @Override
    public String type() {
        if (this.isFILE())
            return Files.getSuffixName(xo.getKey());
        return null;
    }

    @Override
    public String mime() {
        if (this.isFILE())
            return mimes.getMime(type());
        return null;
    }

    @Override
    public boolean hasMime() {
        return !Ws.isBlank(mime());
    }

    @Override
    public boolean isMime(String mime) {
        String myMime = mime();
        if (null == myMime)
            return null == mime;
        if (null == mime)
            return false;
        // 用正则
        if (mime.startsWith("^")) {
            return myMime.matches(mime);
        }
        // 精确匹配
        return myMime.equals(mime);
    }

    @Override
    public boolean hasSha1() {
        return !Ws.isBlank(sha1());
    }

    @Override
    public String sha1() {
        return xo.getEtag();
    }

    @Override
    public boolean isSameSha1(String sha1) {
        String mySha1 = sha1();
        if (null == sha1)
            return null == mySha1;

        if (null == mySha1)
            return false;
        return mySha1.equals(sha1);
    }

    @Override
    public String creator() {
        return oRoot.creator();
    }

    @Override
    public String mender() {
        return oRoot.mender();
    }

    @Override
    public String group() {
        return oRoot.group();
    }

    @Override
    public int mode() {
        return oRoot.mode();
    }

    @Override
    public String d0() {
        return oRoot.d0();
    }

    @Override
    public String d1() {
        return oRoot.d1();
    }

    @Override
    public String[] dN() {
        return oRoot.dN();
    }

    @Override
    public String[] labels() {
        return null;
    }

    @Override
    public long expireTime() {
        return -1;
    }

    @Override
    public long syncTime() {
        return -1;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isExpiredBy(long now) {
        return false;
    }

    @Override
    public NutMap toMap4Update(String regex) {
        return toMap(regex);
    }

    @Override
    public NutMap toMap(String regex) {
        if (null != regex) {
            return this.pickBy(regex);
        }

        NutMap map = new NutMap();
        map.putAll(this);
        return map;
    }

    // private void _fill_vals(NutBean map) {
    // // 预先填充自定义属性
    // for (Map.Entry<String, Object> en : this.entrySet()) {
    // String key = en.getKey();
    // Object val = en.getValue();
    // map.put(key, val);
    // }
    //
    // // 填充一遍固定属性
    // _put_all_to_map(map);
    // }

    private void _put_all_to_map(NutBean map) {
        map.put("m", mender());
        map.put("c", creator());
        map.put("g", group());
        map.put("id", id());
        map.put("pid", parentId());
        map.put("nm", name());

        map.put("ph", path());
        map.put("ct", createTime());
        map.put("lm", lastModified());
        map.put("tp", type());
        map.put("d0", d0());
        map.put("d1", d1());
        map.put("md", mode());
        map.put("len", len());
        map.put("mnt", oRoot.mount());
        map.put("race", race());
        map.put("mime", mime());
        map.putAll(xo.rawMeta());
        map.putAll(xo.userMeta());

        // 因为 xo.rawMeta 总是有 Date 类型的对象,为了统一
        // 都用 UTC 字符串来覆盖
        String lms = Wtime.formatUTC(xo.getLastModified(),
                                     "yyyy-MM-dd HH:mm:ss.SSS");
        map.put("Last-Modified", lms);
    }

    public String toJson(JsonFormat jfmt) {
        NutMap map = toMap(null);
        return Json.toJson(map, jfmt);
    }

    public String toString() {
        return String.format("vofs://%s;ID(%s)==%s", path(), id(), mount());
    }

    @Override
    public boolean hasThumbnail() {
        return false;
    }

    @Override
    public String thumbnail() {
        return null;
    }

    @Override
    public boolean hasWriteHandle() {
        throw Wlang.noImplement();
    }

    @Override
    public String getWriteHandle() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj setWriteHandle(String hid) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean isRWMeta() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj setRWMeta(boolean rwmeta) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean hasRWMetaKeys() {
        throw Wlang.noImplement();
    }

    @Override
    public String getRWMetaKeys() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj setRWMetaKeys(String regex) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj clearRWMetaKeys() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj path(String path) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj appendPath(String path) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj name(String nm) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj race(WnRace race) {
        throw Wlang.noImplement();
    }

    @Override
    public void setParent(WnObj parent) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj mount(String mnt) {
        return this;
    }

    @Override
    public WnObj mountRootId(String mrid) {
        return this;
    }

    @Override
    public WnObj lastModified(long lm) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj update(Map<? extends String, ? extends Object> map) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj updateBy(WnObj o) {
        if (this == o || null == o) {
            return this;
        }
        if (o instanceof WnVofsObj) {
            WnVofsObj vo = (WnVofsObj) o;
            this.xo = vo.xo.clone();
            return this;
        }
        throw Wlang.noImplement();
    }

    @Override
    public WnObj link(String lid) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj type(String tp) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj mime(String mime) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj thumbnail(String thumbnail) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean hasData() {
        throw Wlang.makeThrow("data not supported anymore");
    }

    @Override
    public String data() {
        throw Wlang.makeThrow("data not supported anymore");
    }

    @Override
    public WnObj data(String data) {
        throw Wlang.noImplement();
    }

    @Override
    public boolean isSameData(String data) {
        throw Wlang.makeThrow("data not supported anymore");
    }

    @Override
    public WnObj len(long len) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj sha1(String sha1) {
        throw Wlang.noImplement();
    }

    @Override
    public int remain() {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj remain(int remain) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj creator(String creator) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj mender(String mender) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj group(String grp) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj mode(int md) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj d0(String d0) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj d1(String d1) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj labels(String[] lbls) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj createTime(long ct) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj expireTime(long expi) {
        throw Wlang.noImplement();
    }

    @Override
    public WnObj syncTime(long st) {
        throw Wlang.noImplement();
    }

}
