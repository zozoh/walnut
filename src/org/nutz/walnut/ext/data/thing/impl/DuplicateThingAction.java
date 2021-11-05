package org.nutz.walnut.ext.data.thing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.thing.ThingAction;
import org.nutz.walnut.ext.data.thing.util.ThingConf;
import org.nutz.walnut.ext.data.thing.util.ThingDuplicateOptions;
import org.nutz.walnut.ext.data.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class DuplicateThingAction extends ThingAction<List<WnObj>> {

    protected String srcId;

    protected ThingConf conf;

    protected WnExecutable executor;

    protected ThingDuplicateOptions options;

    @Override
    public List<WnObj> invoke() {
        // 得到对应对 Thing
        WnObj oIndex = this.checkDirTsIndex();
        WnObj oT = io.getIn(oIndex, srcId);

        // 准备唯一键映射表
        Map<String, ThingUniqueKey> uniqKeys = conf.getUniqueKeyNameMap();

        // 准备返回列表
        int N;
        if (options.hasToIds()) {
            N = options.toIds.size();
        } else {
            N = options.dupCount;
        }
        List<WnObj> reList = new ArrayList<>(N);

        // 防守: 这个 Thing 必须是有效的
        if (N <= 0 || null == oT || oT.getInt("th_live") == Things.TH_DEAD) {
            return reList;
        }

        // 准备一个判读是否是标准字段的匹配器
        WnMatch isStd = Wobj.explainObjKeyMatcher("!#STD", false);

        // 自定义字段过滤器
        WnMatch isFldMatch = AutoMatch.parse(options.fieldFilter, true);

        // 准备要复制的字段列表
        // 1. 普通字段
        NutBean thMeta = new NutMap();
        // 2. 引用字段： key : "id:xxx"
        Map<String, String> fReferMeta = new LinkedHashMap<>();
        // 3. 批量引用字段: key : ["id:xxx",...]
        Map<String, List<String>> fReferIds = new LinkedHashMap<>();
        // 4. 内置冗余文件表: th_(media|attachment...)_ids
        // {"media":[ID1, ID2, ID3 ...]}
        Map<String, List<String>> thReferDataIds = new LinkedHashMap<>();
        // {"th_media_ids":[ID1, ID2, ID3 ...], "th_media_nb": 5 ...}
        NutMap thReferMeta = new NutMap();

        // 最后归纳出整体要 copy 的文件表
        List<WnObj> fCopyList = new LinkedList<>();
        //
        // 循环查找对象的元数据
        for (Map.Entry<String, Object> en : oT.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null == val) {
                continue;
            }
            // 对象的标准字段，则无视之
            if (isStd.match(key)) {
                continue;
            }
            // 如果是唯一性字段，无法COPY，也无视
            if (uniqKeys.containsKey(key)) {
                continue;
            }
            // 看看是否匹配了字段
            if (!isFldMatch.match(key)) {
                continue;
            }
            // 1.2 内置文件列表: fReferList
            // ---- 这里需要考虑 "^(th_" + countKey + "_(nb|ids|map|list))$"
            Pattern _P2 = Pattern.compile("^th_([0-9a-zA-Z_-]+)_(nb|ids|map|list)$");
            Matcher m = _P2.matcher(key);
            if (m.find()) {
                // 记录在案先，以便 shallow 模式下直接复制
                thReferMeta.put(key, val);
                // 首先记录所有引用的 ID
                if (key.endsWith("_ids") && val instanceof List<?>) {
                    List<?> _ids = (List<?>) val;
                    String dirName = m.group(1);
                    List<String> rIds = new ArrayList<>(_ids.size());
                    for (Object _id : _ids) {
                        if (null == _id) {
                            continue;
                        }
                        String fid = _id.toString();
                        WnObj oFile = io.get(fid);
                        if (null != oFile) {
                            rIds.add(fid);
                            fCopyList.add(oFile);
                        }
                    }
                    if (!rIds.isEmpty()) {
                        thReferDataIds.put(dirName, rIds);
                    }
                }
                // 其他的冗余映射，后面会强行重设，所有现在无视
                continue;
            }
            // 字符串字段，则看看是否是文件对象引用
            if (val instanceof String) {
                String str = (String) val;
                if (str.startsWith("id:")) {
                    WnObj oFile = io.fetch(null, str);
                    if (null != oFile) {
                        fReferMeta.put(key, str);
                        fCopyList.add(oFile);
                        continue;
                    }
                }
            }
            // 集合字段，依次看看元素是否是文件引用
            if (val instanceof Collection<?>) {
                // 就看第一个就好
                Collection<?> cols = (Collection<?>) val;
                if (!cols.isEmpty()) {
                    Iterator<?> ite = cols.iterator();
                    List<String> ids = new ArrayList<>(cols.size());
                    while (ite.hasNext()) {
                        Object vo = ite.next();
                        if (null != vo && (vo instanceof String)) {
                            String s = (String) vo;
                            if (s.startsWith("id:")) {
                                WnObj oFile = io.fetch(null, s);
                                if (null != oFile) {
                                    ids.add(s);
                                    fCopyList.add(oFile);
                                }
                            }
                        }
                    }
                    if (!ids.isEmpty()) {
                        fReferIds.put(key, ids);
                        continue;
                    }
                }
            }
            // 1.3 普通非标准字段元数据集: thMeta
            thMeta.put(key, val);
        }

        // 最后覆盖固定元数据
        if (null != this.options.fixedMeta) {
            Wn.explainMetaMacro(this.options.fixedMeta);
            thMeta.putAll(this.options.fixedMeta);
        }

        // 准备目标列表
        List<WnObj> oTargetList;

        // 更新已存在对象
        if (this.options.hasToIds()) {
            WnQuery q = Wn.Q.pid(oIndex);
            String toKey = Ws.sBlank(options.toKey, "id");
            q.setAll(Wlang.map(toKey, options.toIds));
            oTargetList = io.query(q);
        }
        // 生成新的目标对象
        else {
            List<NutMap> newMetas = new ArrayList<>(options.dupCount);
            for (int i = 0; i < options.dupCount; i++) {
                newMetas.add(new NutMap());
            }
            oTargetList = this.service.createThings(newMetas);
        }

        // 浅层 Copy 直接引用字段，因此将几个元数据合并就是了
        if (options.shallow) {
            thMeta.putAll(fReferMeta);
            thMeta.putAll(fReferIds);
            // 内置冗余文件表
            if (!thReferDataIds.isEmpty()) {
                thMeta.putAll(thReferMeta);
            }
        }
        // 循环处理一下目标对象
        String dirDataPath = Wn.appendPath(oTs.path(), "data");
        String dirThDataPath = Wn.appendPath(dirDataPath, oT.id()) + "/";
        // 循环
        for (WnObj oTarget : oTargetList) {
            // 非浅层，则需要 copy 文件对象
            if (!options.shallow && !fCopyList.isEmpty()) {
                // 1. 如果非 shallow Copy，则会生成新的 file ID，那么需要一个 ID 映射表
                Map<String, WnObj> copyIdMappings = new HashMap<>();
                for (WnObj oFile : fCopyList) {
                    String fph = oFile.path();
                    // 得到一个相对路径
                    String rph = Wpath.getRelativePath(dirThDataPath, fph);
                    // 创建目标对象
                    String taFilePh = Wn.appendPath("data", oTarget.id(), rph);
                    WnObj oTaFile = io.createIfNoExists(oTs, taFilePh, WnRace.FILE);
                    // 执行复制
                    io.copyData(oFile, oTaFile);
                    // 记录 ID 映射
                    copyIdMappings.put(oFile.id(), oTaFile);
                }
                // 处理 ID 映射表
                if (!copyIdMappings.isEmpty()) {
                    NutMap taReferMeta = new NutMap();
                    // 2. 引用字段： key : "id:xxx"
                    __map_refer_file_path(fReferMeta, copyIdMappings, taReferMeta);
                    // 3. 批量引用字段: key : ["id:xxx",...]
                    __map_refer_file_path_list(fReferIds, copyIdMappings, taReferMeta);
                    // 4. 内置冗余文件表: th_(media|attachment...)_ids
                    __map_refer_th_data(thReferDataIds, copyIdMappings, taReferMeta);
                    // 更新过去
                    if (!taReferMeta.isEmpty()) {
                        io.appendMeta(oTarget, taReferMeta);
                    }
                }
            }
            // 3. 更新目标对象基础元数据
            io.appendMeta(oTarget, thMeta);
        }

        // 搞定
        return oTargetList;
    }

    private void __map_refer_th_data(Map<String, List<String>> thReferDataIds,
                                     Map<String, WnObj> copyIdMappings,
                                     NutMap taFileMeta) {
        for (Map.Entry<String, List<String>> en : thReferDataIds.entrySet()) {
            String dirName = en.getKey();
            List<String> rIds = en.getValue();
            // 准备一个新的值表
            List<String> newIds = new ArrayList<>(rIds.size());
            List<NutBean> newOList = new ArrayList<>(rIds.size());
            // 来吧
            for (String rId : rIds) {
                WnObj fo2 = copyIdMappings.get(rId);
                if (null == fo2) {
                    throw Er.createf("e.cmd.thing_copy.FileMappingId",
                                     "dirName=%s,val=%s",
                                     dirName,
                                     rId);
                }
                newIds.add(fo2.id());
                NutBean foMeta = fo2.pick("id",
                                          "nm",
                                          "thumb",
                                          "mime",
                                          "sha1",
                                          "tp",
                                          "len",
                                          "duration",
                                          "width",
                                          "height",
                                          "video_frame_count",
                                          "video_frame_rate",
                                          "lm",
                                          "ct");
                newOList.add(foMeta);
            }
            taFileMeta.put(String.format("th_%s_ids", dirName), newIds);
            taFileMeta.put(String.format("th_%s_list", dirName), newOList);
            taFileMeta.put(String.format("th_%s_nb", dirName), newIds.size());
        }
    }

    private void __map_refer_file_path_list(Map<String, List<String>> fReferIds,
                                            Map<String, WnObj> copyIdMappings,
                                            NutMap taFileMeta) {
        for (Map.Entry<String, List<String>> en : fReferIds.entrySet()) {
            String key = en.getKey();
            List<String> val = en.getValue();
            List<String> vs2 = new ArrayList<>(val.size());
            for (String v : val) {
                String fid = v.substring(3);
                WnObj fo2 = copyIdMappings.get(fid);
                if (null == fo2) {
                    throw Er.createf("e.cmd.thing_copy.FileMappingId", "key=%s,val=%s", key, val);
                }
                vs2.add("id:" + fo2.id());
            }
            taFileMeta.put(key, vs2);
        }
    }

    private void __map_refer_file_path(Map<String, String> fReferMeta,
                                       Map<String, WnObj> copyIdMappings,
                                       NutMap taFileMeta) {
        for (Map.Entry<String, String> en : fReferMeta.entrySet()) {
            String key = en.getKey();
            String val = en.getValue();
            String fid = val.substring(3);
            WnObj fo2 = copyIdMappings.get(fid);
            if (null == fo2) {
                throw Er.createf("e.cmd.thing_copy.FileMappingId", "key=%s,val=%s", key, val);
            }
            taFileMeta.put(key, "id:" + fo2.id());
        }
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public ThingConf getConf() {
        return conf;
    }

    public void setConf(ThingConf conf) {
        this.conf = conf;
    }

    public WnExecutable getExecutor() {
        return executor;
    }

    public void setExecutor(WnExecutable executor) {
        this.executor = executor;
    }

    public ThingDuplicateOptions getOptions() {
        return options;
    }

    public void setOptions(ThingDuplicateOptions options) {
        this.options = options;
    }

}
