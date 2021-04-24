package org.nutz.walnut.ext.ooml.hdl;

import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.xlsx.XlsxMedia;
import org.nutz.walnut.ooml.xlsx.XlsxRow;
import org.nutz.walnut.ooml.xlsx.XlsxSheet;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class ooml_sheet extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(medias|rows)");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        String sheetId = params.val(0, "1");

        XlsxSheet sheet = fc.workbook.getSheet(sheetId);

        // 按照 JSON 输出
        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json;

        // 输出表格行内容
        if (params.is("rows")) {
            List<XlsxRow> rows = sheet.getRows();
            json = Json.toJson(rows, jfmt);
        }
        // 输出媒体内容
        else if (params.is("medias")) {
            List<XlsxMedia> medias = sheet.getMedias();
            json = Json.toJson(medias, jfmt);
        }
        // 默认输出对象
        else {
            // 默认第一行作为标题行
            int headIndex = Integer.parseInt(params.val(1, "0"));

            // 转换对象
            Map<String, String> header = sheet.getHeaderMapping(headIndex);
            List<NutBean> beans = sheet.toBeans(header, headIndex + 1);

            json = Json.toJson(beans, jfmt);
        }

        // 打印
        sys.out.println(json);
    }

}
