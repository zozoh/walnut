package com.site0.walnut.ext.media.ooml.hdl;

import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class ooml_sheet extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(medias|rows)");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        String sheetId = params.getString("id");
        String sheetName = params.getString("name");
        int index = params.val_int(0, 0);

        // 指定了 ID
        if (!Ws.isBlank(sheetId)) {
            fc.sheet = fc.workbook.getSheetById(sheetId);
        }
        // 指定了名称
        else if (!Ws.isBlank(sheetName)) {
            fc.sheet = fc.workbook.getSheetByName(sheetName);
        }
        // 默认获取第一个
        else {
            fc.sheet = fc.workbook.getSheet(index);
        }
    }

}
