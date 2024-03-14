package com.site0.walnut.ext.data.thing.hdl;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.Mirror;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.impexp.ThingFill;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

/**
 * 根据自定义规则导入数据到thing中
 * 
 * @author pw
 *
 */
public class thing_import implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        ZParams params = ZParams.parse(hc.args, null);
        // 参数
        String tarRootPath = params.getString("path", null);
        String tarImportClzName = params.getString("clz", "org.nutz.walnut.web.WnSetup");

        WnObj rootPathObj = sys.io.fetch(null, Wn.normalizeFullPath(tarRootPath, sys));
        Class<?> tarClz = Class.forName(tarImportClzName);

        if (rootPathObj == null) {
            sys.err.print("param [path] not exist");
            return;
        }
        if (tarClz == null) {
            sys.err.print("param [clz] not exist");
            return;
        }

        // 判断clz是否为导入接口的实现类
        if (!ThingFill.class.isAssignableFrom(tarClz)) {
            sys.err.printf("clz [%s] is not assignable from ThingFill.class", tarImportClzName);
            return;
        }

        // 开始导入逻辑
        Mirror<?> mirror = Mirror.me(tarClz);
        ThingFill tf = (ThingFill) mirror.born();
        WnObj thingSet = Things.checkThingSet(hc.oRefer);
        traverseRootObj(sys, tf, thingSet, rootPathObj);
    }

    private void traverseRootObj(WnSystem sys, ThingFill tf, WnObj thingSet, WnObj rootObj) {
        if (tf.isTarObj(sys, rootObj)) {
            if (tf.doImport(sys, thingSet, rootObj)) {
                sys.out.printlnf("thing-import-succ: %s[%s]", rootObj.id(), rootObj.path());
            } else {
                sys.err.printlnf("thing-import-fail: %s[%s]", rootObj.id(), rootObj.path());
            }
        } else {
            if ((rootObj.isDIR())) {
                sys.io.each(Wn.Q.pid(rootObj.id()), new Each<WnObj>() {
                    @Override
                    public void invoke(int index, WnObj ele, int length)
                            throws ExitLoop, ContinueLoop, LoopException {
                        traverseRootObj(sys, tf, thingSet, ele);
                    }
                });
            }
        }
    }

}
