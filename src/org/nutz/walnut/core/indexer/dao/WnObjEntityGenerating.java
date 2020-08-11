package org.nutz.walnut.core.indexer.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.dao.impl.entity.field.NutMappingField;
import org.nutz.dao.jdbc.JdbcExpert;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.sql.WnDaoConfig;

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
    private static final Map<String, NutMappingField> builtIns = new HashMap<>();

    private JdbcExpert expert;

    private WnDaoConfig conf;

    WnObjEntityGenerating(WnDaoConfig config, JdbcExpert expert) {
        this.conf = config;
        this.expert = expert;

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
        String json = Files.read("org/nutz/walnut/core/indexer/dao/built-fields.json");
        List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
        for (NutMap map : list) {
            NutMappingField mf = this.mapToField(map);
            builtIns.put(mf.getName(), mf);
        }
    }

    NutMappingField mapToField(NutMap map) {
        NutMappingField mf = Lang.map2Object(map, NutMappingField.class);
        return mf;
    }

    WnObjEntity generate() {
        WnObjEntity en = new WnObjEntity();

        // ----------------------------------------
        // 标准字段表的默认值
        __setup_default_objKeys();

        // ----------------------------------------
        // 默认标准主键
        Map<String, Boolean> pks = __setup_primary_keys(en);

        // ----------------------------------------
        // 字段总不能为空吧
        if (null == conf.getFields() || conf.getFields().isEmpty()) {
            throw Er.create("e.io.dao.NoFields", conf.getTableName());
        }
        // 循环处理字段
        for (NutMap map : conf.getFields()) {
            // 填充默认值
            __fill_field_default_value(map);

            // 转换成字段
            NutMappingField mf = this.mapToField(map);

            // 主键
            if (pks.get(mf.getName())) {
                mf.setAsName();
                mf.setAsNotNull();
            }

            // 记入实体
            en.addMappingField(mf);
        }
        // ----------------------------------------
        // 设置默认字段
        en.autoSetDefaultFields(builtIns, conf);
        // ----------------------------------------
        // 搜索主键
        en.checkCompositeFields(conf.getPks());
        // ----------------------------------------
        // 搞定返回
        en.setComplete(true);
        return en;
    }

    private Map<String, Boolean> __setup_primary_keys(WnObjEntity en) {
        String pkName = conf.getFieldName("id");
        if (null == conf.getPks()) {
            conf.setPks(Lang.array(pkName));
        }

        // 编制主键表
        Map<String, Boolean> pks = new HashMap<>();
        for (String pk : conf.getPks()) {
            pks.put(pk, true);
        }
        // 主键必须是 ID 啦，其他的情况我想起来头疼，暂时不支持
        if (pks.size() != 1 || !pks.containsKey(pkName)) {
            throw Er.create("e.io.dao.InvalidPK", Json.toJson(pks));
        }

        // ----------------------------------------
        // 获得表名以及视图名称
        en.setTableName(conf.getTableName());
        en.setViewName(conf.getTableName());

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
        if (Strings.isBlank(fldName)) {
            throw Er.create("e.io.dao.field.WithoutName", Json.toJson(fld));
        }

        // 数据库字段与默认同名
        fld.putDefault("columnName", fldName);

        String colName = fld.getString("columnName");
        boolean wrapName = fld.getBoolean("wrapName", false);
        String colNameSql = expert.wrapKeyword(colName, wrapName);
        fld.put("columnNameInSql", colNameSql);

        // 处理字段类型
        String type = fld.getString("type", "String");
        // 填充类型
        Class<?> javaType = javaTypes.get(type);
        if (null == javaType) {
            throw Er.create("e.io.dao.field.UnSupportType", fldName + ":" + type);
        }
        fld.put("type", javaType);

    }

    private void __setup_default_objKeys() {
        if (null == conf.getObjKeys()) {
            conf.setObjKeys(new NutMap());
        }
        NutMap objKeys = conf.getObjKeys();
        // 构成对象树的关键
        objKeys.putDefault("id", "id");
        objKeys.putDefault("pid", "pid");
        objKeys.putDefault("nm", "nm");
        // 权限
        objKeys.putDefault("c", "c");
        objKeys.putDefault("m", "m");
        objKeys.putDefault("g", "g");
        objKeys.putDefault("md", "md");
        // 内容相关
        objKeys.putDefault("race", "race");
        objKeys.putDefault("ln", "ln");
        objKeys.putDefault("tp", "tp");
        objKeys.putDefault("mime", "mime");
        objKeys.putDefault("sha1", "sha1");
        objKeys.putDefault("mnt", "mnt");
        objKeys.putDefault("len", "len");
        objKeys.putDefault("d0", "d0");
        objKeys.putDefault("d1", "d1");
        objKeys.putDefault("lbls", "lbls");
        // 时间戳
        objKeys.putDefault("ct", "ct");
        objKeys.putDefault("lm", "lm");
        objKeys.putDefault("st", "st");
        objKeys.putDefault("expi", "expi");
    }

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
