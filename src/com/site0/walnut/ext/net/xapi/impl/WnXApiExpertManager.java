package com.site0.walnut.ext.net.xapi.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.xapi.bean.XApiExpert;
import com.site0.walnut.util.Wn;

public class WnXApiExpertManager extends AbstractXApiExpertManager {

    private DefaultXApiExpertManager DFT;

    private WnIo io;

    private WnObj oHome;

    public WnXApiExpertManager(WnIo io, NutBean vars) {
        super();

        this.io = io;
        this.DFT = DefaultXApiExpertManager.getInstance();

        // 找到自己的配置目录
        String phHome = Wn.normalizeFullPath("~/.xapi/experts/", vars);
        this.oHome = io.fetch(null, phHome);
    }

    @Override
    public XApiExpert getExpert(String apiName) {
        // 看看缓冲里有木有呢
        XApiExpert ex = super.getExpert(apiName);

        // 是否在本域的自定义配置里呢？
        if (null == ex && null != oHome) {
            WnObj oEx = io.fetch(oHome, apiName + ".expert.json");
            if (null != oEx) {
                ex = io.readJson(oEx, XApiExpert.class);
                ex.setName(apiName);
                // 计入缓存
                this.addExpert(apiName, ex);
            }
        }

        // 有木有默认的呢？
        if (null == ex) {
            ex = DFT.getExpert(apiName);
        }

        // 返回
        return ex;
    }

    @Override
    public Map<String, XApiExpert> getExperts() {
        Map<String, XApiExpert> re = new HashMap<>();

        // 默认的
        re.putAll(DFT.getExperts());

        // 添加一个父（缓冲里的内容）
        re.putAll(super.getExperts());

        // 动态的
        if (null != oHome) {
            List<WnObj> oExList = io.getChildren(oHome, "^([\\d\\w_-]+)[.]expert[.]json");
            for (WnObj oEx : oExList) {
                XApiExpert ex = io.readJson(oEx, XApiExpert.class);
                String nm = oEx.name();
                String apiName = nm.substring(0, nm.length() - ".expert.json".length());
                ex.setName(apiName);
                // 顺便计入缓存
                this.addExpert(nm, ex);
                // 计入结果
                re.put(apiName, ex);
            }
        }

        return re;
    }

}
