package org.nutz.mongo.adaptor;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.nutz.lang.Mirror;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoAdaptor;
import org.nutz.mongo.ZMoDoc;
import org.nutz.mongo.entity.ZMoEntity;
import org.nutz.mongo.entity.ZMoField;
import com.site0.walnut.util.Wlang;

public class ZMoArrayAdaptor implements ZMoAdaptor {

    ZMoArrayAdaptor() {}

    @Override
    public Object toJava(ZMoField fld, Object obj) {
        // 获取元素的实体
        ZMoEntity[] en = new ZMoEntity[1];

        // 开始循环数组
        List<Object> list = new LinkedList<>();
        Wlang.eachEvenMap(obj, (index, eleMongo, src) -> {
            Object elePojo;
            // 如果元素是个 Mongo 类型
            if (eleMongo instanceof Document) {
                // 确保已经获得过实体过了，这里这个代码考虑到效率
                // 就是说一个集合或者数组，映射方式总是一样的
                // 如果有不一样的，那么就完蛋了
                if (null == en[0]) {
                    en[0] = ZMo.me().getEntity(eleMongo.getClass());
                }
                // 转换
                ZMoDoc doc = ZMoDoc.WRAP((Document) eleMongo);
                elePojo = ZMo.me().fromDoc(doc, en[0]);
            }
            // 如果 fld 有 adaptor
            else if (null != fld && null != fld.getEleAdaptor()) {
                elePojo = fld.getEleAdaptor().toJava(fld, eleMongo);
            }
            // 其他情况，直接上 smart 咯
            else {
                elePojo = ZMoAs.smart().toJava(null, eleMongo);
            }
            // 加入到数组中
            list.add(elePojo);
        });

        // 创建数组
        Object arr = null;
        if (fld == null) {
            arr = Array.newInstance(Object.class, list.size());
        }
        // 让 fld 的 Borning 来创建
        else {
            arr = fld.getBorning().born(list.size());
        }

        list.toArray((Object[]) arr);
        return arr;
    }

    @Override
    public Object toMongo(ZMoField fld, Object obj) {
        List<Object> list = new LinkedList<>();
        Wlang.eachEvenMap(obj, (index, objPojo, src) -> {
            Object objMongo;
            if (null == objPojo || (objPojo instanceof ObjectId) || (objPojo instanceof Document)) {
                objMongo = objPojo;
            }
            // 判断一下
            else {
                Mirror<?> mi = Mirror.me(objPojo);
                // Map 或者 POJO
                if (mi.isMap() || mi.isPojo()) {
                    objMongo = ZMo.me().toDoc(objPojo);
                }
                // 其他类型用 smart 转一下咯
                else {
                    objMongo = ZMoAs.smart().toMongo(null, objPojo);
                }
            }
            list.add(objMongo);
        });
        return list;
    }

}
