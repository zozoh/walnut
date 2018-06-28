package org.nutz.walnut.ext.thing;

import java.util.Collection;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.impl.CreateTmpFileAction;
import org.nutz.walnut.ext.thing.impl.CleanTmpFileAction;
import org.nutz.walnut.ext.thing.impl.CreateThingAction;
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
import org.nutz.walnut.ext.thing.util.ThQr;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.walnut.util.WnPager;

public class WnThingService {

    private WnIo io;

    private WnObj oTs;

    public WnThingService(WnIo io, WnObj oTs) {
        this.io = io;
        this.oTs = oTs;
    }

    // .....................................................................

    private <T extends ThingAction<?>> T _A(T a) {
        a.setIo(io).setThingSet(oTs);
        return a;
    }

    private <T extends ThingDataAction<?>> T _AD(T a, String dirName, WnObj oT) {
        a.setIo(io).setThingSet(oTs);
        a.setDirName(dirName).setThing(oT);
        return a;
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
        GetThingAction a = _A(new GetThingAction()).setFull(isFull).setId(id);
        a.setQuiet(true);
        return a.invoke();
    }

    public WnObj checkThing(String id, boolean isFull) {
        GetThingAction a = _A(new GetThingAction()).setFull(isFull).setId(id);
        return a.invoke();
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
        CreateThingAction a = _A(new CreateThingAction());
        a.addMeta(meta).setUniqueKey(uniqueKey);
        return a.invoke().get(0);
    }

    public List<WnObj> createThings(List<NutMap> metaList, String uniqueKey) {
        return createThings(metaList, uniqueKey, null, null, null, null);
    }

    public List<WnObj> createThings(List<NutMap> metaList,
                                    String uniqueKey,
                                    WnOutputable out,
                                    String process,
                                    WnExecutable executor,
                                    String cmdTmpl) {
        CreateThingAction a = _A(new CreateThingAction());
        a.addAllMeta(metaList).setUniqueKey(uniqueKey).setProcess(out, process);
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

    public List<WnObj> deleteThing(boolean quiet, Collection<String> ids) {
        DeleteThingAction a = _A(new DeleteThingAction()).setQuiet(quiet).setIds(ids);
        return a.invoke();
    }

    public List<WnObj> deleteThing(boolean quiet, String... ids) {
        DeleteThingAction a = _A(new DeleteThingAction()).setQuiet(quiet).setIds(Lang.list(ids));
        return a.invoke();
    }

    public WnObj updateThing(String id, NutMap meta) {
        UpdateThingAction a = _A(new UpdateThingAction()).setId(id).setMeta(meta);
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
