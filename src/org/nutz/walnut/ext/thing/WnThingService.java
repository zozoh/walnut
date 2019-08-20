package org.nutz.walnut.ext.thing;

import java.util.Collection;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.CleanTmpFileAction;
import org.nutz.walnut.ext.thing.impl.CreateThingAction;
import org.nutz.walnut.ext.thing.impl.CreateTmpFileAction;
import org.nutz.walnut.ext.thing.impl.DeleteThingAction;
import org.nutz.walnut.ext.thing.impl.FileAddAction;
import org.nutz.walnut.ext.thing.impl.FileDeleteAction;
import org.nutz.walnut.ext.thing.impl.FileGetAction;
import org.nutz.walnut.ext.thing.impl.FileQueryAction;
import org.nutz.walnut.ext.thing.impl.FileReadAction;
import org.nutz.walnut.ext.thing.impl.FileUpdateCountAction;
import org.nutz.walnut.ext.thing.impl.GetThingAction;
import org.nutz.walnut.ext.thing.impl.QueryThingAction;
import org.nutz.walnut.ext.thing.impl.UpdateThingAction;
import org.nutz.walnut.ext.thing.impl.sql.SqlCreateThingAction;
import org.nutz.walnut.ext.thing.impl.sql.SqlDeleteThingAction;
import org.nutz.walnut.ext.thing.impl.sql.SqlQueryThingAction;
import org.nutz.walnut.ext.thing.impl.sql.SqlUpdateThingAction;
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.ThingConf;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.ext.thing.util.WnPathNormalizing;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.walnut.util.WnPager;

public class WnThingService {

    protected WnIo io;

    protected WnObj oTs;

    protected WnPathNormalizing pathNormalizing;

    public WnThingService(WnIo io, WnObj oTs) {
        this.io = io;
        this.oTs = oTs;
        NutMap vars = new NutMap();
        String homePath = Wn.appendPath(oTs.d0(), oTs.d1());
        vars.put("HOME", homePath);
        vars.put("PWD", homePath);
        this.pathNormalizing = new WnPathNormalizing() {
            public String normalizeFullPath(String ph) {
                return Wn.normalizeFullPath(ph, vars);
            }
        };
    }

    public WnThingService(WnIo io, WnObj oTs, WnPathNormalizing pathNormalizing) {
        this.io = io;
        this.oTs = oTs;
        this.pathNormalizing = pathNormalizing;
    }

    public WnThingService(WnSystem sys, WnObj oTs) {
        this.io = sys.io;
        this.oTs = oTs;
        this.pathNormalizing = new WnPathNormalizing() {
            public String normalizeFullPath(String ph) {
                return Wn.normalizeFullPath(ph, sys);
            }
        };
    }

    /**
     * 创建一个新的服务类。如果给定的数据集对象与自己的相同，那么就返回自身
     * 
     * @param oTsOther
     *            另外一个集合
     * @param forceGen
     *            true: 表示指定集合即使是自身，也要创建一个新的服务类实例
     * @return 一个新的服务类或者自身
     */
    public WnThingService gen(WnObj oTsOther, boolean forceGen) {
        if (!forceGen && null != oTs && null != oTsOther && oTs.isSameId(oTsOther)) {
            return this;
        }
        return new WnThingService(io, oTsOther, pathNormalizing);
    }

    // .....................................................................
    private <T extends ThingAction<?>> T _A(T a) {
        a.setService(this);
        a.setIo(io);
        a.setoTs(oTs);
        return a;
    }

    private <T extends ThingDataAction<?>> T _AD(T a, String dirName, WnObj oT) {
        a.setService(this);
        a.setIo(io);
        a.setoTs(oTs);
        a.setDirName(dirName).setThing(oT);
        return a;
    }

    private ThingConf checkConf() {
        WnObj oConf = Things.fileTsConf(io, oTs);
        return io.readJson(oConf, ThingConf.class);
    }

    public String normalizeFullPath(String ph) {
        return this.pathNormalizing.normalizeFullPath(ph);
    }

    // .....................................................................
    public WnObj fileAdd(String dirName,
                         WnObj oT,
                         String fnm,
                         Object src,
                         String dupp,
                         boolean overwrite) {
        FileAddAction a = _AD(new FileAddAction(), dirName, oT);
        a.fnm = fnm;
        a.dupp = dupp;
        a.overwrite = overwrite;
        a.src = src;
        return a.invoke();
    }

    public List<WnObj> fileDelete(String dirName, WnObj oT, String... fnms) {
        FileDeleteAction a = _AD(new FileDeleteAction(), dirName, oT);
        a.fnms = fnms;
        return a.invoke();
    }

    public WnObj fileGet(String dirName, WnObj oT, String fnm, boolean quiet) {
        FileGetAction a = _AD(new FileGetAction(), dirName, oT);
        a.fnm = fnm;
        a.quiet = quiet;
        return a.invoke();
    }

    public List<WnObj> fileQuery(String dirName, WnObj oT, NutMap sort) {
        FileQueryAction a = _AD(new FileQueryAction(), dirName, oT);
        a.sort = sort;
        return a.invoke();
    }

    public WnHttpResponse fileRead(String dirName,
                                   WnObj oT,
                                   String fnm,
                                   String etag,
                                   String range,
                                   String userAgent,
                                   boolean quiet) {
        FileReadAction a = _AD(new FileReadAction(), dirName, oT);
        a.fnm = fnm;
        a.etag = etag;
        a.range = range;
        a.userAgent = userAgent;
        a.quiet = quiet;
        return a.invoke();
    }

    public WnObj fileUpdateCount(String dirName, WnObj oT) {
        FileUpdateCountAction a = _AD(new FileUpdateCountAction(), dirName, oT);
        return a.invoke();
    }

    // .....................................................................

    public WnObj mediaAdd(WnObj oT, String fnm, Object src, String dupp, boolean overwrite) {
        return this.fileAdd("media", oT, fnm, src, dupp, overwrite);
    }

    public List<WnObj> mediaDelete(WnObj oT, String... fnms) {
        return this.fileDelete("media", oT, fnms);
    }

    public WnObj mediaGet(WnObj oT, String fnm, boolean quiet) {
        return this.fileGet("media", oT, fnm, quiet);
    }

    public List<WnObj> mediaQuery(WnObj oT, NutMap sort) {
        return this.fileQuery("media", oT, sort);
    }

    public WnHttpResponse mediaRead(WnObj oT,
                                    String fnm,
                                    String etag,
                                    String range,
                                    String userAgent,
                                    boolean quiet) {
        return this.fileRead("media", oT, fnm, etag, range, userAgent, quiet);
    }

    public WnObj mediaUpdateCount(WnObj oT) {
        return this.fileUpdateCount("media", oT);
    }

    // .....................................................................

    public WnObj attachmentAdd(WnObj oT, String fnm, Object src, String dupp, boolean overwrite) {
        return this.fileAdd("attachment", oT, fnm, src, dupp, overwrite);
    }

    public List<WnObj> attachmentDelete(WnObj oT, String... fnms) {
        return this.fileDelete("attachment", oT, fnms);
    }

    public WnObj attachmentGet(WnObj oT, String fnm, boolean quiet) {
        return this.fileGet("attachment", oT, fnm, quiet);
    }

    public List<WnObj> attachmentQuery(WnObj oT, NutMap sort) {
        return this.fileQuery("attachment", oT, sort);
    }

    public WnHttpResponse attachmentRead(WnObj oT,
                                         String fnm,
                                         String etag,
                                         String range,
                                         String userAgent,
                                         boolean quiet) {
        return this.fileRead("attachment", oT, fnm, etag, range, userAgent, quiet);
    }

    public WnObj attachmentUpdateCount(WnObj oT) {
        return this.fileUpdateCount("attachment", oT);
    }

    // .....................................................................

    public WnObj getThing(String id, boolean isFull) {
        return this.getThing(id, isFull, null, false);
    }

    public WnObj getThing(String id, boolean isFull, String sortKey, boolean isAsc) {
        GetThingAction a = _A(new GetThingAction()).setFull(isFull).setId(id);
        a.setSortKey(sortKey).setAsc(isAsc);
        return a.invoke();
    }

    public WnObj checkThing(String id, boolean isFull) {
        return this.checkThing(id, isFull, null, false);
    }

    public WnObj checkThing(String id, boolean isFull, String sortKey, boolean isAsc) {
        WnObj oT = this.getThing(id, isFull, sortKey, isAsc);
        if (null == oT)
            throw Er.create("e.thing.noexists", id);
        return oT;
    }

    public WnObj fetchThing(String th_nm, boolean isFull) {
        ThQuery tq = new ThQuery();
        tq.autoObj = true;
        tq.wp = new WnPager();
        tq.wp.limit = 1;
        tq.qStr = Json.toJson(Lang.map("th_nm", th_nm));
        List<WnObj> list = this.queryList(tq);
        return null != list && list.size() > 0 ? list.get(0) : null;
    }

    public WnObj getOne(ThQuery tq) {
        if (null == tq.wp) {
            tq.wp = new WnPager();
        }
        tq.wp.set(1, 0);
        List<WnObj> list = this.queryList(tq);
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    public WnObj createThing(NutMap meta) {
        return createThing(meta, null);
    }

    public List<WnObj> createThings(List<NutMap> metaList) {
        return createThings(metaList, null);
    }

    public WnObj createThing(NutMap meta, String uniqueKey) {
        return createThing(meta, uniqueKey, null);
    }

    public WnObj createThing(NutMap meta, String uniqueKey, WnExecutable executor) {
        CreateThingAction a = _A(_action_create());
        a.addMeta(meta).setUniqueKey(uniqueKey);
        a.setConf(this.checkConf());
        a.setExecutor(executor);
        return a.invoke().get(0);
    }

    public List<WnObj> createThings(List<NutMap> metaList, String uniqueKey) {
        return createThings(metaList, uniqueKey, null, null, null, null, null);
    }

    public List<WnObj> createThings(List<NutMap> metaList,
                                    String uniqueKey,
                                    NutMap fixedMeta,
                                    WnOutputable out,
                                    String process,
                                    WnExecutable executor,
                                    String cmdTmpl) {
        CreateThingAction a = _A(_action_create());
        a.addAllMeta(metaList);
        a.setUniqueKey(uniqueKey);
        a.setProcess(out, process);
        a.setFixedMeta(fixedMeta);
        a.setExecutor(executor, cmdTmpl);
        a.setConf(this.checkConf());
        return a.invoke();
    }

    public ThQr queryThing(ThQuery tq) {
        QueryThingAction a = _A(_action_query()).setQuery(tq);
        return a.invoke();
    }

    @SuppressWarnings("unchecked")
    public List<WnObj> queryList(ThQuery tq) {
        tq.autoObj = false;
        ThQr qr = this.queryThing(tq);
        if (null != qr.data)
            return (List<WnObj>) qr.data;
        return null;
    }

    public List<WnObj> deleteThing(boolean hard, Collection<String> ids) {
        DeleteThingAction a = _A(_action_delete()).setHard(hard).setIds(ids);
        return a.invoke();
    }

    public List<WnObj> deleteThing(boolean hard, String... ids) {
        DeleteThingAction a = _A(_action_delete()).setHard(hard).setIds(Lang.list(ids));
        return a.invoke();
    }

    public WnObj updateThing(String id, NutMap meta) {
        UpdateThingAction a = _A(_action_update()).setId(id).setMeta(meta);
        a.setConf(this.checkConf());
        return a.invoke();
    }

    public WnObj createTmpFile(String fnm, String du) {
        CreateTmpFileAction a = _A(new CreateTmpFileAction());
        a.fileName = fnm;
        a.duration = du;
        return a.invoke();
    }

    public List<WnObj> cleanTmpFile(int limit) {
        CleanTmpFileAction a = _A(new CleanTmpFileAction());
        a.limit = limit;
        return a.invoke();
    }

    protected CreateThingAction _action_create() {
        String by = this.oTs.getString("thing-by", "wntree");
        switch (by) {
        case "sql":
            return new SqlCreateThingAction();
        default :
        case "wntree":
            return new CreateThingAction();
        }
    }

    protected QueryThingAction _action_query() {
        String by = this.oTs.getString("thing-by", "wntree");
        switch (by) {
        case "sql":
            return new SqlQueryThingAction();
        default :
        case "wntree":
            return new QueryThingAction();
        }
    }

    protected DeleteThingAction _action_delete() {
        String by = this.oTs.getString("thing-by", "wntree");
        switch (by) {
        case "sql":
            return new SqlDeleteThingAction();
        default :
        case "wntree":
            return new DeleteThingAction();
        }
    }

    protected UpdateThingAction _action_update() {
        String by = this.oTs.getString("thing-by", "wntree");
        switch (by) {
        case "sql":
            return new SqlUpdateThingAction();
        default :
        case "wntree":
            return new UpdateThingAction();
        }
    }
}
