package org.nutz.walnut.ext.titanium.creation;

import java.util.Map;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.util.WnObjCachedFactory;
import org.nutz.walnut.ext.titanium.util.WnObjDataLoading;

@IocBean
public class TiCreationService {

    @Inject("refer:io")
    private WnIo io;

    private WnObjCachedFactory<TiCreation> creations;

    private WnObjCachedFactory<TiTypes> types;

    public TiCreationService() {
        creations = new WnObjCachedFactory<>();
        types = new WnObjCachedFactory<>();
    }

    public TiTypes getTypes(WnObj oTypes) {
        return types.get(oTypes, (o) -> {
            TiTypes tt = io.readJson(o, TiTypes.class);
            // 循环解析自己的 help段引用
            for (Map.Entry<String, TiTypeInfo> en : tt.entrySet()) {
                String typeName = en.getKey();
                TiTypeInfo tpio = en.getValue();
                if (tpio.isHelpReferToFile()) {
                    WnObj oHelp = io.check(oTypes, tpio.getHelp());
                    String help = io.readText(oHelp);
                    tpio.setHelp(help);
                }
                tpio.setName(typeName);
                if (!tpio.hasMime()) {
                    String mime = io.mimes().getMime(typeName);
                    tpio.setMime(mime);
                }
            }
            // 返回
            return tt;
        });
    }

    public TiCreation getCreation(WnObj oCreation) {
        if (null == oCreation)
            return null;

        // 输入文件的读取逻辑
        WnObjDataLoading<TiCreation> loading = new WnObjDataLoading<TiCreation>() {
            public TiCreation load(WnObj o) {
                // 读取输入
                TiCreationInput input = io.readJson(o, TiCreationInput.class);

                // 准备输出
                TiCreation tic = new TiCreation();

                // 读取依赖
                if (input.hasIncludes()) {
                    for (String aph : input.getIncludes()) {
                        WnObj oInclude = io.check(oCreation, aph);
                        TiCreation pic = getCreation(oInclude);
                        tic.mergeWith(pic);
                    }
                }

                // 设置自己的 mapping
                if (input.hasMapping())
                    tic.addMapping(input.getMapping());

                // 解析 types
                if (input.hasTypes()) {
                    for (Map.Entry<String, String> en : input.getTypes().entrySet()) {
                        String lang = en.getKey();
                        String path = en.getValue();
                        WnObj oTypes = io.check(oCreation, path);
                        TiTypes types = getTypes(oTypes);
                        tic.addTypes(lang, types);
                    }
                }

                // 输出
                return tic;
            }
        };
        // 读取输入文件
        return creations.get(oCreation, loading);
    }
}
