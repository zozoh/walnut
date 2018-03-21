package org.nutz.walnut.ext.thing.impexp;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 没地方放，暂时放一下，稍后删掉
 * 
 * @author pw
 *
 */
public class HuromRecipesThingFill implements ThingFill {

    @Override
    public boolean doImport(WnSystem sys, WnObj thingSet, WnObj tarObj) {
        NutMap impInfo = NutMap.NEW();
        List<String> imgIds = new ArrayList<>();
        List<String> imgNms = new ArrayList<>();
        sys.io.each(Wn.Q.pid(tarObj.id()).sortBy("nm", 1), new Each<WnObj>() {
            @Override
            public void invoke(int index, WnObj ele, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                if (ele.isType("jpg")) {
                    if (ele.name().equals("01.jpg")) {
                        impInfo.setv("thumb", ele.id());
                    }
                    imgIds.add(ele.id());
                    imgNms.add(ele.name());
                } else if (ele.isType("txt")) {
                    String tnm = ele.name().replaceAll(".txt", "");
                    String content = sys.io.readText(ele);
                    content = content.replace("食材", "# 食材");
                    impInfo.setv("nm", tnm);
                    impInfo.setv("content", content);
                }
            }
        });
        String tsetId = thingSet.id();
        // 新建thing
        String createThing = String.format("thing %s create '%s'", tsetId, impInfo.getString("nm"));
        String createResult = sys.exec2(createThing);
        NutMap thingObj = NutMap.WRAP(createResult);
        String thingId = thingObj.getString("id");
        // 添加内容
        String addContent = String.format("thing %s detail %s -content '%s'",
                                          tsetId,
                                          thingId,
                                          impInfo.getString("content"));
        sys.exec2(addContent);
        // 添加缩率图
        WnObj thingIndex = sys.io.fetch(thingSet, "index/" + thingId);
        WnObj thumbSrc = sys.io.get(impInfo.getString("thumb"));
        WnObj thumbObj = sys.io.createIfNoExists(thingSet,
                                                 "data/" + thingId + "/thumb.jpg",
                                                 WnRace.FILE);
        sys.io.copyData(thumbSrc, thumbObj);
        sys.io.appendMeta(thingIndex, NutMap.NEW().setv("thumb", "id:" + thumbObj.id()));
        // 添加media
        for (int i = 0; i < imgIds.size(); i++) {
            String addMedia = String.format("thing %s media %s -add %s -read id:%s",
                                            tsetId,
                                            thingId,
                                            imgNms.get(i),
                                            imgIds.get(i));
            sys.exec2(addMedia);
        }
        return true;
    }

    @Override
    public boolean isTarObj(WnSystem sys, WnObj curObj) {
        // 目录，下面有 01.jpg 和 xxx.txt
        if (curObj.isDIR()) {
            if (sys.io.exists(curObj, "01.jpg")) {
                if (sys.io.count(Wn.Q.pid(curObj.id()).setv("tp", "txt")) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
