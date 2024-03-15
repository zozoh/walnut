package com.site0.walnut.core.indexer.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.nutz.dao.entity.EntityField;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.impl.entity.NutEntityIndex;
import org.nutz.dao.impl.entity.field.NutMappingField;
import org.nutz.dao.jdbc.JdbcExpert;
import org.nutz.dao.jdbc.ValueAdaptor;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.dao.obj.WnObjEjecting;
import com.site0.walnut.core.indexer.dao.obj.WnObjInjecting;
import com.site0.walnut.core.indexer.dao.obj.id.WnObjIdEjecting;
import com.site0.walnut.core.indexer.dao.obj.id.WnObjIdInjecting;
import com.site0.walnut.core.indexer.dao.obj.json.WnObjJsonEjecting;
import com.site0.walnut.core.indexer.dao.obj.json.WnObjJsonInjecting;
import com.site0.walnut.core.indexer.dao.obj.race.WnObjRaceEjecting;
import com.site0.walnut.core.indexer.dao.obj.race.WnObjRaceInjecting;
import com.site0.walnut.core.indexer.dao.obj.sarray.WnObjSArrayEjecting;
import com.site0.walnut.core.indexer.dao.obj.sarray.WnObjSArrayInjecting;
import com.site0.walnut.ext.sys.sql.WnDaoMappingConfig;
import com.site0.walnut.util.Ws;
import com.site0.walnut.core.indexer.dao.obj.metas.WnObjMetasEjecting;
import com.site0.walnut.core.indexer.dao.obj.metas.WnObjMetasInjecting;

public class WnObjEntityGenerating {

    /**
     * 内置字段表。键均为<code>WnObj</code>标准字段名
     * 
     * <pre>
     * 构成对象树的关键
     *  - id | pid | nm
     * 
     * 权限
     *  - c | m | g | md
     * 
     * 内容相关
     * - race | ln | tp | mime | sha1 | mnt 
     * - len  | d0 | d1 | lbls
     * 
     * 时间戳
     * - ct | lm | st | expi
     * 
     * </pre>
     */
    private static final Map<String, NutMap> builtIns = new HashMap<>();

    private WnObj root;

    private JdbcExpert expert;

    private WnDaoMappingConfig conf;

    /**
     * conf.objKeys 的反转映射，以便通过 field.name 获取标准字段名
     */
    // private Map<String, String> revKeys;

    /**
     * 记录一个对象的主键的索引集合。这个会在开始生成实体时填充
     */
    private HashSet<String> pks;

    /**
     * 保存一下字段已经定义的字段名，除了这些字段，其他字段都算表外字段
     * <p>
     * 如果声明了 ".."，则会自动收缩进一个 JSON 字符串
     */
    private HashSet<String> fieldNames;

    public WnObjEntityGenerating(WnObj root, WnDaoMappingConfig config, JdbcExpert expert) {
        this.root = root;
        this.conf = config;
        this.expert = expert;
        this.fieldNames = new HashSet<>();
        this.pks = new HashSet<>();

        // 读取默认的内置字段
        if (builtIns.isEmpty()) {
            synchronized (WnObjEntityGenerating.class) {
                if (builtIns.isEmpty()) {
                    loadBuiltInFields();
                }
            }
        }
    }

    private void loadBuiltInFields() {
        String json = Files.read("com/site0/walnut/core/indexer/dao/built-fields.json");
        List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
        for (NutMap map : list) {
            // 转换
            String nm = map.getString("name");

            // 记入
            builtIns.put(nm, map);
        }
    }

    NutMappingField mapToField(NutMap map) {
        // 填充默认值
        __fill_field_default_value(map);

        // 转换
        NutMappingField mf = Wlang.map2Object(map, NutMappingField.class);

        // 得到 WnObj 标准字段名，如果 revKeys 没给，那么自然是初始化默认字段
        // 就直接用字段名称即可
        String stdName = mf.getName();

        // 字段值适配器
        ValueAdaptor va = expert.getAdaptor(mf);
        mf.setAdaptor(va);

        // 对于 id 字段的输入输出，需要考虑两段式 ID
        if ("id".equals(stdName)) {
            mf.setInjecting(new WnObjIdInjecting(stdName, root));
            mf.setEjecting(new WnObjIdEjecting(stdName));
        }
        // 对于 race 字段的输入输出，需要在 string 和 int 之间转换
        else if ("race".equals(stdName)) {
            mf.setInjecting(new WnObjRaceInjecting(stdName));
            mf.setEjecting(new WnObjRaceEjecting(stdName));
        }
        // 如果是自动收集字段，也需要特殊设置取值器
        else if ("..".equals(stdName)) {
            mf.setInjecting(new WnObjMetasInjecting(fieldNames));
            mf.setEjecting(new WnObjMetasEjecting(fieldNames));
        }
        // 根据类型搞一下
        else {
            Class<?> typeClass = mf.getTypeClass();
            if (typeClass.equals(Object.class)) {
                mf.setInjecting(new WnObjJsonInjecting(stdName));
                mf.setEjecting(new WnObjJsonEjecting(stdName));
            }
            // 如果是 SArray 字段
            else if (typeClass.equals(String[].class)) {
                mf.setInjecting(new WnObjSArrayInjecting(stdName, false));
                mf.setEjecting(new WnObjSArrayEjecting(stdName));
            }
            // 如果是 SArray 字段
            else if (typeClass.equals(List.class)) {
                mf.setInjecting(new WnObjSArrayInjecting(stdName, true));
                mf.setEjecting(new WnObjSArrayEjecting(stdName));
            }
            // 其他输入输出输入输出
            else {
                mf.setInjecting(new WnObjInjecting(stdName));
                mf.setEjecting(new WnObjEjecting(stdName));
            }
        }

        // 搞定
        return mf;
    }

    public WnObjEntity generate() {
        WnObjEntity en = new WnObjEntity();

        // ----------------------------------------
        // 默认标准主键
        __setup_primary_keys();

        // ----------------------------------------
        // 默认内置标准字段
        if (!conf.hasObjKeys()) {
            conf.setObjKeys(Wlang.array("id", "pid", "nm", "race", ".."));
        }

        // ----------------------------------------
        // 获得表名以及视图名称
        en.setTableName(conf.getTableName());
        en.setViewName(conf.getTableName());

        // ----------------------------------------
        // 设置默认字段
        __auto_set_default_fields(en);

        // ----------------------------------------
        // 循环处理字段
        if (null != conf.getFields() || conf.getFields().isEmpty()) {
            for (NutMap map : conf.getFields()) {
                // 转换成字段
                NutMappingField mf = this.mapToField(map);

                // 主键
                if (pks.contains(mf.getName())) {
                    mf.setAsName();
                    mf.setAsNotNull();
                }

                // 记入实体
                en.addMappingField(mf);
            }
        }

        // ----------------------------------------
        // 最后，编制一下实体所有字段的索引，以便得到表外字段
        this.fieldNames.clear();
        for (MappingField mf : en.getMappingFields()) {
            String stdName = mf.getName();
            if (!"..".equals(stdName)) {
                this.fieldNames.add(stdName);
            }
        }

        // ----------------------------------------
        // 搜索主键
        en.checkCompositeFields(conf.getPks());
        // ----------------------------------------
        // 配置默认索引
        __setup_default_indexes(en);
        // 设置索引
        if (conf.hasIndexes()) {
            __add_indexes_to_entity(en);
        }
        // ----------------------------------------
        // 搞定返回
        en.setComplete(true);
        return en;
    }

    /**
     * 检查下面的字段，如果没有则补齐
     * 
     * <pre>
     * 构成对象树的关键
     *  - id | pid | nm
     * 
     * 权限
     *  - c | m | g | md
     * 
     * 内容相关
     * - race | ln | tp | mime | sha1 | mnt 
     * - len  | d0 | d1 | lbls
     * 
     * 时间戳
     * - ct | lm | st | expi
     * 
     * </pre>
     * 
     * @param pks
     */
    private void __auto_set_default_fields(WnObjEntity en) {
        // 查找所有标准字段，并确保实体包括所有标准字段
        for (String stdName : conf.getObjKeys()) {

            // 如果没有
            if (null == en.getField(stdName)) {
                // 从内置的里面搞一个
                NutMap map = builtIns.get(stdName);
                NutMappingField mf = this.mapToField(map);
                if (null == mf) {
                    throw Er.create("e.io.dao.entity.LackStdField", stdName);
                }

                // 主键
                if (pks.contains(mf.getName())) {
                    mf.setAsName();
                    mf.setAsNotNull();
                }

                // 记入
                en.addMappingField(mf);
            }
        }

    }

    private void __setup_default_indexes(WnObjEntity en) {
        List<NutMap> indexes = conf.getIndexes();
        if (null == indexes || indexes.isEmpty()) {
            indexes = new ArrayList<NutMap>(2);

            // 有 ID
            if (null != en.getField("id")) {
                indexes.add(Wlang.map("unique:true")
                                .setv("name", "obj_id")
                                .setv("fields", Wlang.list("id")));
            }

            // 有 PID 和 NM
            if (null != en.getField("pid") && null != en.getField("nm")) {
                indexes.add(Wlang.map("unique:true")
                                .setv("name", "obj_pid_nm")
                                .setv("fields", Wlang.list("pid", "nm")));
            }

            // 记入
            conf.setIndexes(indexes);
        }
    }

    private void __add_indexes_to_entity(WnObjEntity en) {
        for (NutMap map : conf.getIndexes()) {
            NutEntityIndex ei = new NutEntityIndex();
            ei.setUnique(map.getBoolean("unique"));
            ei.setName(map.getString("name"));
            List<String> eiFields = map.getAsList("fields", String.class);
            List<EntityField> fields = new ArrayList<>(eiFields.size());
            for (String fnm : eiFields) {
                EntityField ef = en.getField(fnm);
                if (null == ef) {
                    throw Er.create("e.io.dao.entity.index.FieldNotDefined",
                                    fnm + "@" + ei.getName());
                }
                fields.add(ef);
            }
            ei.setFields(fields);

            // 记入
            en.addIndex(ei);
        }
    }

    private HashSet<String> __setup_primary_keys() {
        if (null == conf.getPks()) {
            conf.setPks(Wlang.array("id"));
        }

        // 编制主键表
        pks.clear();
        for (String pk : conf.getPks()) {
            pks.add(pk);
        }
        // 主键必须是 ID 啦，其他的情况我想起来头疼，暂时不支持
        if (pks.size() != 1 || !pks.contains("id")) {
            throw Er.create("e.io.dao.InvalidPK", Json.toJson(pks));
        }

        return pks;
    }

    private void __fill_field_default_value(NutMap fld) {
        fld.putDefault("columnType", "AUTO");
        fld.putDefault("isVersion", false);
        fld.putDefault("readonly", false);
        fld.putDefault("notNull", false);
        fld.putDefault("unsigned", false);
        fld.putDefault("autoIncreasement", false);
        fld.putDefault("casesensitive", true);
        fld.putDefault("insert", true);
        fld.putDefault("update", true);

        // 必须有名称
        String fldName = fld.getString("name");
        if (Ws.isBlank(fldName)) {
            throw Er.createf("e.io.dao.field.WithoutName : [%s] : %s", fldName, Json.toJson(fld));
        }

        // 数据库字段与默认同名
        fld.putDefault("columnName", fldName);

        String colName = fld.getString("columnName");
        boolean wrapName = fld.getBoolean("wrapName", false);
        String colNameSql = expert.wrapKeyword(colName, wrapName);
        fld.put("columnNameInSql", colNameSql);

        // 处理字段类型
        Object type = fld.get("type", "String");
        if (type instanceof Class<?>) {
            // 嗯已经转换过了，啥也不用做
        }
        // 转换成 Java 类型
        else {
            // 填充类型
            Class<?> javaType = javaTypes.get(type);
            if (null == javaType) {
                throw Er.create("e.io.dao.field.UnSupportType", fldName + ":" + type);
            }
            fld.put("type", javaType);
        }

    }

    /**
     * 字段的 type 对应的 Java 类型
     */
    private static final Map<String, Class<?>> javaTypes = new HashMap<>();
    static {
        javaTypes.put("String", String.class);
        javaTypes.put("Integer", Integer.class);
        javaTypes.put("Long", Long.class);
        javaTypes.put("Float", Float.class);
        javaTypes.put("Double", Double.class);
        javaTypes.put("Boolean", Boolean.class);
        javaTypes.put("Object", Object.class);
        javaTypes.put("SArray", String[].class);
        javaTypes.put("List", List.class);
        javaTypes.put("JSON", Object.class);
    }

}
