package org.nutz.walnut.ext.wooz.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value="cqn", regex="^(write)$")
public class wooz_seq implements JvmHdl {

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备好参数
        // 赛事id
        String compId = hc.params.val_check(0);
        WnObj comp = sys.io.checkById(compId);
        if (comp.containsKey("sp_comp_id")) {
            comp = sys.io.checkById(comp.getString("sp_comp_id"));
            compId = comp.id();
        }
        // 赛项
        String pj = hc.params.val_check(1);
        // 模板
        String tmplStr = hc.params.val_check(2);
        Tmpl tmpl = Cmds.parse_tmpl(tmplStr);
        
        int start = hc.params.getInt("start", 1);
        int step = hc.params.getInt("step", 1);
        String sex = hc.params.getString("sex");
        String valsStr = hc.params.getString("vars");
        String ug = hc.params.get("ug");
        boolean do_write = hc.params.is("write");
        String sort = hc.params.get("sort", "ct:1");
        
        // 参数准备好了, 现在把目录找出来
        String path = "/home/" + comp.creator() + "/comp/data/" + comp.id() + "/signup";
        WnObj signup_dir = sys.io.check(null, path);
        
        WnQuery query = new WnQuery();
        query.setv("pid", signup_dir.id());
        query.setv("u_pj", pj);
        query.setvIfNoBlank("sex", sex);
        query.sort(Lang.map(sort));
        
        NutMap vars = Lang.map(Strings.sBlank(valsStr, "{}"));
        
        List<WnObj> list = sys.io.query(query);
        List<SeqR> result = new ArrayList<>(list.size());
        
        // 是不是团体赛呢?
        if (Strings.isBlank(ug)) {
            int current = start;
            for (WnObj wobj : list) {
                vars.put("seq", current);
                vars.put("obj", wobj);
                String value = tmpl.render(vars);
                result.add(new SeqR(wobj, value.toUpperCase()));
                current += step;
            }
        }
        else {
            // 首先,分组
            NutMap groups = new NutMap();
            String group_key = "cg_inv_code";
            list.forEach((wobj)-> {
               groups.addv2(wobj.getString(group_key, "default"), wobj); 
            });
            // 然后算编号
            int current = start;
            for (Entry<String, Object> en : groups.entrySet()) {
                List<WnObj> members = (List<WnObj>) en.getValue();
                vars.put("seq", current);
                vars.put("obj", members.get(0));// 拿第一个选手作为渲染上下文
                String value = tmpl.render(vars);
                // TODO 根据UG的值, 选用不同的后缀模式
                char cur = 'A';
                for (WnObj wobj : members) {
                    String code = value + cur;
                    result.add(new SeqR(wobj, code.toUpperCase()));
                    cur += 1;
                }
                current += step;
            }
        }
        // 打印结果
        NutMap metas = new NutMap();
        for (SeqR seqr : result) {
            sys.out.printlnf("%-8s : %s", seqr.code, Strings.sBlank(seqr.wobj.get("u_aa"), seqr.wobj.name()));
            // 如果要打印的话,就输出呗
            if (do_write) {
                metas.put("u_code", seqr.code);
                sys.io.appendMeta(seqr.wobj, metas);
                String trkplayer = path = "/home/" + comp.creator() + "/comp/data/" + comp.id() + "/trkplayer/" + seqr.wobj.getString("u_pj") + "_" + seqr.wobj.getString("u_id");
                WnObj wobj2 = sys.io.fetch(null, trkplayer);
                if (wobj2 != null)
                    sys.io.appendMeta(wobj2, metas);
            }
        }
    }

    
    static class SeqR {
        public WnObj wobj;
        public String code;
        public SeqR() {
        }
        public SeqR(WnObj wobj, String code) {
            super();
            this.wobj = wobj;
            this.code = code;
        }
    }
}