package org.nutz.walnut.ext.mt90.hdl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs("cqn")
public class mt90_parse implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = sys.in.readAll();
        BufferedReader br = new BufferedReader(new StringReader(text));
        List<Mt90Raw> list = new ArrayList<>();
        while (br.ready()) {
            String line = br.readLine();
            if (line == null)
                break;
            if (Strings.isBlank(line))
                continue;
            Mt90Raw raw = Mt90Raw.mapping(line);
            if (raw != null)
                list.add(raw);
        }
        sys.out.writeJson(list, Cmds.gen_json_format(hc.params));
    }
}
