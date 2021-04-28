package org.nutz.walnut.ext.net.xapi.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.xapi.bean.ThirdXExpert;
import org.nutz.walnut.util.Wn;

public class WnThridXExpertManager extends AbstractThirdXExpertManager {

    private DefaultThirdXExpertManager DFT;

    private WnIo io;

    private WnObj oHome;

    public WnThridXExpertManager(WnIo io, NutBean vars) {
        super();

        this.io = io;
        this.DFT = DefaultThirdXExpertManager.getInstance();

        // 找到自己的配置目录
        String phHome = Wn.normalizeFullPath("~/.xapi/experts/", vars);
        this.oHome = io.fetch(null, phHome);
    }

    @Override
    public ThirdXExpert getExpert(String apiName) {
        // 看看缓冲里有木有呢
        ThirdXExpert ex = super.getExpert(apiName);

        // 是否在本域的自定义配置里呢？
        if (null == ex && null != oHome) {
            WnObj oEx = io.fetch(oHome, apiName + ".expert.json");
            if (null != oEx) {
                ex = io.readJson(oEx, ThirdXExpert.class);
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
    public Map<String, ThirdXExpert> getExperts() {
        Map<String, ThirdXExpert> re = new HashMap<>();

        // 默认的
        re.putAll(DFT.getExperts());

        // 添加一个父（缓冲里的内容）
        re.putAll(super.getExperts());

        // 动态的
        if (null != oHome) {
            List<WnObj> oExList = io.getChildren(oHome, "^([\\d\\w_-]+)[.]expert[.]json");
            for (WnObj oEx : oExList) {
                ThirdXExpert ex = io.readJson(oEx, ThirdXExpert.class);
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
