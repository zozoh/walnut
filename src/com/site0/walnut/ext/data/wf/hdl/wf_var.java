package com.site0.walnut.ext.data.wf.hdl;

import com.site0.walnut.ext.data.wf.WfContext;
import com.site0.walnut.ext.data.wf.WfFilter;
import com.site0.walnut.ext.data.wf.vars.WfObjContentVarLoader;
import com.site0.walnut.ext.data.wf.vars.WfObjMetaVarLoader;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class wf_var extends WfFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(select)$");
    }

    @Override
    protected void process(WnSystem sys, WfContext fc, ZParams params) {
        // 分析参数
        String varName = params.val(0);
        String fPath = params.getString("f");
        String oPath = params.getString("o");
        String pick = params.get("pick");
        boolean isSelect = params.is("select");

        // 变量过滤器
        WnMatch picker = AutoMatch.parse(pick, true);

        // 加载参数变量
        for (int i = 1; i < params.vals.length; i++) {
            String json = params.val(i);
            fc.addStaticVarLoader(varName, json, picker);
        }

        // 其次加载对象内容
        if (!Ws.isBlank(fPath)) {
            String aph = Wn.normalizeFullPath(fPath, sys);
            WfObjContentVarLoader lo = new WfObjContentVarLoader(varName, sys.io, aph);
            lo.setKeyPicker(picker);
            fc.addVarLoader(lo);
        }

        // 再次，加载对象元数据
        else if (!Ws.isBlank(oPath)) {
            String aph = Wn.normalizeFullPath(oPath, sys);
            WfObjMetaVarLoader lo = new WfObjMetaVarLoader(varName, sys.io, aph);
            lo.setKeyPicker(picker);
            fc.addVarLoader(lo);
        }
        
        if(isSelect) {
            
        }
    }

}
