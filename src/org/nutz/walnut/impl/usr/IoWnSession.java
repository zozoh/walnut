package org.nutz.walnut.impl.usr;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.AbstractWnSession;
import org.nutz.walnut.api.usr.WnSession;

/**
 * 记录一个 ZSession 相关信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnSession extends AbstractWnSession {

    private NutMap vars;

    private WnIo io;

    private WnObj obj;

    public IoWnSession(WnIo io, WnObj obj) {
        this.io = io;
        this.obj = obj;
    }

    WnObj getObj() {
        return obj;
    }

    /**
     * @return 会话ID
     */
    @Override
    public String id() {
        return obj.id();
    }

    @Override
    public boolean isSame(String seid) {
        return id().equals(seid);
    }

    @Override
    public String getParentSessionId() {
        return obj.getString("p_se_id");
    }

    @Override
    public NutMap vars() {
        if (null == vars) {
            synchronized (this) {
                if (null == vars) {
                    vars = io.readJson(obj, NutMap.class);
                    if (null == vars)
                        vars = new NutMap();
                }
            }
        }
        return vars;
    }

    @Override
    public void save() {
        NutMap map = vars();
        io.writeJson(obj, map, JsonFormat.nice().setIgnoreNull(true).setQuoteName(false));

        // 看看有没有必要更新名称
        String nmInObj = obj.getString("me");
        String grpInObj = obj.getString("grp");
        String nmInVar = map.getString("MY_NM");
        String grpInVar = map.getString("MY_GRP");

        // TODO 这样直接改会不会有安全隐患呢 ....
        if (!nmInObj.equals(nmInVar) || !grpInObj.equals(grpInVar)) {
            obj.put("me", nmInVar);
            obj.put("grp", grpInVar);
            io.set(obj, "^(me|grp)$");
        }
    }

    @Override
    public WnSession var(String nm, Object val) {
        vars().setOrRemove(nm, val);
        return this;
    }

    @Override
    public Object var(String nm) {
        return vars().get(nm);
    }

    @Override
    public String me() {
        return obj.getString("me");
    }

    @Override
    public String group() {
        return obj.getString("grp");
    }

    @Override
    public long duration() {
        return obj.getLong("du");
    }

    @Override
    public long expireTime() {
        return obj.expireTime();
    }

    @Override
    public WnSession clone() {
        return new IoWnSession(io, obj.clone());
    }

}
