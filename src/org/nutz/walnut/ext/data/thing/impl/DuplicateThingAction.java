package org.nutz.walnut.ext.data.thing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

public class DuplicateThingAction extends ThingAction<List<WnObj>> {

    protected String srcId;

    protected ThingConf conf;

    protected WnExecutable executor;

    protected ThingDuplicateOptions options;

    @Override
    public List<WnObj> invoke() {
        // 得到对应对 Thing
        String aphTs = oTs.path();
        WnObj oIndex = this.checkDirTsIndex();
        WnObj oTsrc = io.getIn(oIndex, srcId);
        WnObj dirTsrcData = io.fetch(oTs, "data/" + oTsrc.id());

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
        if (N <= 0 || null == oTsrc || oTsrc.getInt("th_live") == Things.TH_DEAD) {
            return reList;
        }

        // 准备一个判读是否是标准字段的匹配器
        WnMatch isStd = Wobj.explainObjKeyMatcher("!#STD", false);

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
        Map<String, WnObj> fCopyList = new HashMap<String, WnObj>();

        // 固定要Copy 的文件
        if (null != dirTsrcData && null != options.copyFiles) {
            for (String copyPath : options.copyFiles) {
                WnObj oCopy = io.fetch(dirTsrcData, copyPath);
                if (null != oCopy) {
                    fCopyList.put(oCopy.id(), oCopy);
                }
            }
        }

        // 循环查找对象的元数据
        for (Map.Entry<String, Object> en : oTsrc.entrySet()) {
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
            if (!options.fieldFilter.match(key)) {
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
                            fCopyList.put(oFile.id(), oFile);
                        }
                    }
                    if (!rIds.isEmpty()) {
                        thReferDataIds.put(dirName, rIds);
                    }
                }
                // 其他的冗余映射，后面会强行重设，所以现在无视
                continue;
            }
            // 字符串字段，则看看是否是文件对象引用
            if (val instanceof String) {
                String str = (String) val;
                if (str.startsWith("id:")) {
                    WnObj oFile = io.fetch(null, str);
                    if (null != oFile) {
                        fReferMeta.put(key, str);
                        fCopyList.put(oFile.id(), oFile);
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
                                    fCopyList.put(oFile.id(), oFile);
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
        // > 这样拼合路径是为了防止， data 目录对象为 null
        String dirDataPath = Wn.appendPath(oTs.path(), "data");
        String dirThDataPath = Wn.appendPath(dirDataPath, oTsrc.id()) + "/";
        // 循环
        for (WnObj oTarget : oTargetList) {
            // 非浅层，则需要 copy 文件对象
            if (!options.shallow && !fCopyList.isEmpty()) {
                // 1. 如果非 shallow Copy，则会生成新的 file ID，那么需要一个 ID 映射表
                Map<String, WnObj> copyIdMappings = new HashMap<>();
                for (WnObj oFile : fCopyList.values()) {
                    // 必须为文件
                    if (!oFile.isFILE()) {
                        continue;
                    }
                    String fph = oFile.path();

                    // 准备输出的目标文件
                    WnObj oTaFile;

                    // 准备渲染上下文
                    NutMap c = new NutMap();
                    c.put("target", oTarget);
                    c.put("file", oFile);

                    // 处理集合外用文档
                    if (!fph.startsWith(aphTs)) {
                        oTaFile = genOuterFile(oTarget, oFile, c);
                    }
                    // 处理集合内引用
                    else {
                        oTaFile = genInnerFile(oTarget, dirThDataPath, fph);
                    }

                    // 稳一手，确保这两个文件不相等
                    if (null != oTaFile && !oTaFile.isSameId(oFile)) {
                        // 执行复制
                        Wn.Io.copyFile(io, oFile, oTaFile);

                        // 执行字段复制
                        __try_copy_meta_to_refer_file(isStd, oFile, oTaFile, c);

                        // 记录 ID 映射
                        copyIdMappings.put(oFile.id(), oTaFile);
                    }
                }
                //
                // 处理 ID 映射表
                //
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

    private void __try_copy_meta_to_refer_file(WnMatch isStd,
                                               WnObj oFile,
                                               WnObj oTaFile,
                                               NutMap c) {
        // 防守
        if (null == this.options.fFieldMatch) {
            return;
        }
        // 收集要 copy 的字段
        NutMap fMeta = new NutMap();
        for (Map.Entry<String, Object> en : oFile.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null == val) {
                continue;
            }
            if (isStd.match(key)) {
                continue;
            }
            if (!options.fFieldMatch.match(key)) {
                continue;
            }
            fMeta.put(key, val);
        }

        // 强制复制的字段
        if (null != options.fmeta) {
            NutMap fm2 = (NutMap) Wn.explainObj(c, options.fmeta);
            fMeta.putAll(fm2);
        }

        // 复制字段
        if (!fMeta.isEmpty()) {
            io.appendMeta(oTaFile, fMeta);
        }
    }

    /**
     * 处理集合外引用文件
     * 
     * @param oTarget
     * @param oFile
     * @param c
     * @return 新创建的文件对象
     */
    private WnObj genOuterFile(WnObj oTarget, WnObj oFile, NutMap c) {
        // 防守
        if (null == options.fOutside) {
            return null;
        }
        String fnm = oFile.name();
        c.put("input", fnm);
        Matcher m = options.fOutside.matcher(fnm);
        boolean found = m.find();
        if (found && options.fMatchOnly) {
            return null;
        }

        // 那么需要准备上下文
        if (found) {
            int gc = m.groupCount();
            for (int i = 0; i <= gc; i++) {
                c.put("g" + i, m.group(i));
            }
        }

        // 渲染上下文变量
        if (null != options.fvars) {
            NutMap vars = (NutMap) Wn.explainObj(c, options.fvars);
            c.putAll(vars);
        }

        // 输出新名称
        String newName = options.fNewname.render(c);

        // 创建对应文件对象
        WnObj oP = oFile.parent();
        WnObj oNew = io.createIfNoExists(oP, newName, WnRace.FILE);
        return oNew;
    }

    /**
     * 处理集合内引用文件
     * 
     * @param oTarget
     * @param dirThDataPath
     * @param fph
     * @return 新创建的文件对象
     */
    private WnObj genInnerFile(WnObj oTarget, String dirThDataPath, String fph) {
        // 得到一个相对路径
        String rph = Wpath.getRelativePath(dirThDataPath, fph);

        /**
         * <pre>
        # 在某些时候，出现了下面一种引用
        # 一个数据
        ~/cases/index/{ID1}
         |--[abc] -> ~/cases/data/{ID2}/attachment/abc.pdf
        
        # 我们为其生成一个复制的数据记录
        newId = {ID3}
        
        # 此时，我们当前的两个路径是这样的
        dirDataPath = ~/cases/data/
        dirThDataPath = ~/cases/data/{ID1}/
        
        # 而待 Copy 的 oFile 对象路径是这样的
        ~/cases/data/{ID2}/attachment/abc.pdf
        
        # 显然，它属于当前数据集的另外一个对象
        # 与 `dirThDataPath` 计算相对路径
        rph = ../{ID2}/attachment/abc.pdf
        # 最终的目标路径是
        taFilePath = data/{ID3}/../{ID2}/attachment/abc.pdf
        # 整理后路径为
        taFilePath = data/{ID2}/attachment/abc.pdf
        #
        # 显然，这个目标对象，在数据集的另外一个记录内
        # 而我们希望生成一个数据记录的完整复刻
        # 因此，我们需要将这种特征的对象也要做一下处理
        #  - rph 以 ../ 开头
        #  - 它的第二层目录是一个 ID
        # 这种路径，我们应该将 rph 直接指定为 attachment/abc.pdf
        # 这样就能生成一个完整的数据记录复刻了
         * </pre>
         */
        if (rph.startsWith("../")) {
            String[] phSS = rph.split("/");
            if (phSS.length >= 3 && Wn.isFullObjId(phSS[1])) {
                rph = Ws.join(phSS, "/", 2);
            }
        }

        // 创建目标对象
        String taFilePh = Wn.appendPath("data", oTarget.id(), rph);
        return io.createIfNoExists(oTs, taFilePh, WnRace.FILE);
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
