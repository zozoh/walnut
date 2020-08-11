package org.nutz.walnut.core.indexer.dao;

import java.util.Map;

import org.nutz.dao.impl.entity.NutEntity;
import org.nutz.dao.impl.entity.field.NutMappingField;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.sql.WnDaoConfig;

public class WnObjEntity extends NutEntity<WnIoObj> {

    public WnObjEntity() {
        super(WnIoObj.class);
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
    void autoSetDefaultFields(Map<String, NutMappingField> builtIns,
                              WnDaoConfig conf,
                              Map<String, Boolean> pks) {
        // 查找所有标准字段，并确保实体包括所有标准字段
        NutMap objKeys = conf.getObjKeys();
        for (Map.Entry<String, Object> en : objKeys.entrySet()) {
            // 标准字段名
            String key = en.getKey();
            // 映射字段名（Java）
            String fnm = en.getValue().toString();

            // 如果没有
            if (null == this.getField(fnm)) {
                // 从内置的里面搞一个
                NutMappingField mf = builtIns.get(key);
                if (null == mf) {
                    throw Er.create("e.io.dao.entity.LackStdField", key);
                }
                // 复制一份放入实体中
                NutMappingField mf2 = mf.duplicate(fnm);

                // 主键
                if (pks.containsKey(mf2.getName())) {
                    mf2.setAsName();
                    mf2.setAsNotNull();
                }

                // 记入
                this.addMappingField(mf2);
            }
        }

    }

}
