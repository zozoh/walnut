package org.nutz.walnut.ext.myproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class cmd_myproject extends JvmExecutor {

    @Override
    @SuppressWarnings("rawtypes")
    public void exec(WnSystem sys, String[] args) throws Exception {
        String pwd = sys.se.envs().getString("PWD");
        String projectPath = pwd + "/.project";
        List<NutMap> re = new ArrayList<NutMap>();
        if (sys.io.exists(null, projectPath)) {
            WnObj projectObj = sys.io.fetch(null, projectPath);
            sys.io.eachChildren(projectObj, null, new Each<WnObj>() {
                @Override
                public void invoke(int index, WnObj project, int length) throws ExitLoop,
                        ContinueLoop, LoopException {
                    List<Map> tasks = new ArrayList<Map>();
                    NutMap pinfo = NutMap.NEW();
                    pinfo.put("name", project.name());
                    pinfo.put("tasks", tasks);
                    re.add(pinfo);
                    // 遍历所有任务
                    sys.io.eachChildren(project, null, new Each<WnObj>() {
                        @Override
                        public void invoke(int index, WnObj task, int length) throws ExitLoop,
                                ContinueLoop, LoopException {
                            String tstr = sys.io.readText(task);
                            Map tjson = null;
                            try {
                                tjson = Json.fromJsonAsMap(Object.class, tstr);
                            }
                            catch (Exception e) {
                                tjson = NutMap.NEW();
                            }
                            tasks.add(tjson);
                        }
                    });
                }
            });
        }
        // 返回查询结果
        sys.out.print(Json.toJson(re));
    }
}
