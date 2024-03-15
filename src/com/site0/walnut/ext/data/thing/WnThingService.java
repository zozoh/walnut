package com.site0.walnut.ext.data.thing;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.thing.impl.CleanTmpFileAction;
import com.site0.walnut.ext.data.thing.impl.CreateThingAction;
import com.site0.walnut.ext.data.thing.impl.CreateTmpFileAction;
import com.site0.walnut.ext.data.thing.impl.DeleteThingAction;
import com.site0.walnut.ext.data.thing.impl.DuplicateThingAction;
import com.site0.walnut.ext.data.thing.impl.FileAddAction;
import com.site0.walnut.ext.data.thing.impl.FileDeleteAction;
import com.site0.walnut.ext.data.thing.impl.FileGetAction;
import com.site0.walnut.ext.data.thing.impl.FileQueryAction;
import com.site0.walnut.ext.data.thing.impl.FileReadAsHttpAction;
import com.site0.walnut.ext.data.thing.impl.FileUpdateCountAction;
import com.site0.walnut.ext.data.thing.impl.FileUploadAction;
import com.site0.walnut.ext.data.thing.impl.GetThingAction;
import com.site0.walnut.ext.data.thing.impl.QueryThingAction;
import com.site0.walnut.ext.data.thing.impl.UpdateThingAction;
import com.site0.walnut.ext.data.thing.options.ThCreateOptions;
import com.site0.walnut.ext.data.thing.options.ThUpdateOptions;
import com.site0.walnut.ext.data.thing.util.ThQr;
import com.site0.walnut.ext.data.thing.util.ThQuery;
import com.site0.walnut.ext.data.thing.util.ThingConf;
import com.site0.walnut.ext.data.thing.util.ThingDuplicateOptions;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.ext.data.thing.util.WnPathNormalizing;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnHttpResponseWriter;
import com.site0.walnut.util.WnPager;

public class WnThingService {

    protected WnIo io;

    protected WnObj oTs;

    protected WnPathNormalizing pathNormalizing;

    public WnThingService(WnIo io, WnObj oTs) {
        this(io, oTs, Wn.getObjHomePath(oTs));
    }

    public WnThingService(WnIo io, WnObj oTs, String homePath) {
        this.io = io;
        this.oTs = oTs;
        NutMap vars = new NutMap();
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

    public List<WnObj> fileUpload(String dirName,
                                  WnObj oT,
                                  String fnm,
                                  InputStream ins,
                                  String boundary,
                                  String fieldName,
                                  String dupp,
                                  boolean overwrite) {
        FileUploadAction a = _AD(new FileUploadAction(), dirName, oT);
        a.fnm = fnm;
        a.dupp = dupp;
        a.overwrite = overwrite;
        a.ins = ins;
        a.boundary = boundary;
        a.fieldName = fieldName;
        return a.invoke();
    }

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

    public WnHttpResponseWriter fileReadAsHttp(String dirName,
                                               WnObj oT,
                                               String fnm,
                                               String etag,
                                               String range,
                                               String userAgent,
                                               boolean quiet) {
        FileReadAsHttpAction a = _AD(new FileReadAsHttpAction(), dirName, oT);
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

    public WnHttpResponseWriter mediaRead(WnObj oT,
                                          String fnm,
                                          String etag,
                                          String range,
                                          String userAgent,
                                          boolean quiet) {
        return this.fileReadAsHttp("media", oT, fnm, etag, range, userAgent, quiet);
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

    public WnHttpResponseWriter attachmentRead(WnObj oT,
                                               String fnm,
                                               String etag,
                                               String range,
                                               String userAgent,
                                               boolean quiet) {
        return this.fileReadAsHttp("attachment", oT, fnm, etag, range, userAgent, quiet);
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
        tq.qStr = Json.toJson(Wlang.map("th_nm", th_nm));
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
        ThCreateOptions opt = ThCreateOptions.create(uniqueKey, null, executor, null);
        return this.createOneThing(meta, opt);
    }

    public WnObj createThing(NutMap meta,
                             String uniqueKey,
                             NutMap fixedMeta,
                             WnExecutable executor,
                             String cmdTmpl) {
        ThCreateOptions opt = ThCreateOptions.create(uniqueKey, fixedMeta, executor, cmdTmpl);
        return this.createOneThing(meta, opt);
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
        ThCreateOptions opt = ThCreateOptions.create(uniqueKey,
                                                     fixedMeta,
                                                     out,
                                                     process,
                                                     executor,
                                                     cmdTmpl);
        return this.createManyThings(metaList, opt);
    }

    public WnObj createOneThing(NutMap meta, ThCreateOptions opt) {
        CreateThingAction a = _A(new CreateThingAction());
        a.addMeta(meta).setUniqueKey(opt.uniqueKey);
        a.setConf(this.checkConf());
        a.setExecutor(opt.executor);
        a.setWithoutHook(opt.withoutHook);
        return a.invoke().get(0);
    }

    public List<WnObj> createManyThings(List<NutMap> metaList, ThCreateOptions opt) {
        CreateThingAction a = _A(new CreateThingAction());
        a.addAllMeta(metaList).setUniqueKey(opt.uniqueKey);
        a.setConf(this.checkConf());
        a.setProcess(opt.out, opt.process);
        a.setFixedMeta(opt.fixedMeta);
        a.setExecutor(opt.executor, opt.cmdTmpl);
        a.setWithoutHook(opt.withoutHook);
        return a.invoke();
    }

    public ThQr queryThing(ThQuery tq) {
        QueryThingAction a = _A(new QueryThingAction()).setQuery(tq);
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

    public List<WnObj> duplicateThing(WnExecutable executor,
                                      String id,
                                      ThingDuplicateOptions options) {
        DuplicateThingAction a = _A(new DuplicateThingAction());
        a.setExecutor(executor);
        a.setSrcId(id);
        a.setConf(this.checkConf());
        a.setOptions(options);
        return a.invoke();
    }

    public List<WnObj> deleteThing(WnExecutable executor,
                                   Object match,
                                   boolean hard,
                                   Collection<String> ids) {
        DeleteThingAction a = _A(new DeleteThingAction()).setHard(hard).setIds(ids);
        a.setConf(this.checkConf());
        a.setExecutor(executor);
        a.setMatch(match);
        return a.invoke();
    }

    public List<WnObj> deleteThing(WnExecutable executor,
                                   Object match,
                                   boolean hard,
                                   String... ids) {
        DeleteThingAction a = _A(new DeleteThingAction()).setHard(hard).setIds(Wlang.list(ids));
        a.setConf(this.checkConf());
        a.setExecutor(executor);
        a.setMatch(match);
        return a.invoke();
    }

    public List<WnObj> deleteThing(WnExecutable executor,
                                   WnQuery query,
                                   int maxSafe,
                                   Object match,
                                   boolean hard) {
        DeleteThingAction a = _A(new DeleteThingAction()).setHard(hard);
        a.setConf(this.checkConf());
        a.setExecutor(executor);
        a.setMatch(match);
        a.setQuery(query);
        a.setMaxSafeCount(maxSafe);
        return a.invoke();
    }

    public WnObj updateThing(String id, NutMap meta, WnExecutable executor, Object match) {
        ThUpdateOptions opt = ThUpdateOptions.create(meta, executor, match);
        return this.updateOneThing(id, opt);
    }

    public List<WnObj> updateThings(String[] ids,
                                    NutMap meta,
                                    WnExecutable executor,
                                    Object match) {
        ThUpdateOptions opt = ThUpdateOptions.create(meta, executor, match);
        return this.updateManyThings(ids, opt);
    }

    public WnObj updateOneThing(String id, ThUpdateOptions opt) {
        UpdateThingAction a = _A(new UpdateThingAction()).addIds(id).setMeta(opt.meta);
        a.setConf(this.checkConf());
        a.setExecutor(opt.executor);
        a.setMatch(opt.match);
        a.setWithoutHook(opt.withoutHook);
        List<WnObj> objs = a.invoke();
        if (null == objs || objs.size() == 0) {
            return null;
        }
        return objs.get(0);
    }

    public List<WnObj> updateManyThings(String[] ids, ThUpdateOptions opt) {
        UpdateThingAction a = _A(new UpdateThingAction()).addIds(ids).setMeta(opt.meta);
        a.setConf(this.checkConf());
        a.setExecutor(opt.executor);
        a.setMatch(opt.match);
        a.setWithoutHook(opt.withoutHook);
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
}
