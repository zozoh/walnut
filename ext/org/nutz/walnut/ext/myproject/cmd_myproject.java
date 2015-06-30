package org.nutz.walnut.ext.myproject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.nutz.walnut.util.Wn;

public class cmd_myproject extends JvmExecutor {

    @Override
    @SuppressWarnings("rawtypes")
    public void exec(final WnSystem sys, String[] args) throws Exception {
        String projectPath = "~/.project";
        final List<NutMap> re = new ArrayList<NutMap>();
        WnObj projectObj = Wn.checkObj(sys, projectPath);
        sys.io.eachChildren(projectObj, null, new Each<WnObj>() {
            @Override
            public void invoke(int index, WnObj project, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                final List<Map> tasks_overdue = new ArrayList<Map>();
                final List<Map> tasks_7day = new ArrayList<Map>();
                final List<Map> tasks_7day_after = new ArrayList<Map>();
                final List<Map> tasks_done = new ArrayList<Map>();
                // 遍历所有任务
                sys.io.eachChildren(project, null, new Each<WnObj>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void invoke(int index, WnObj task, int length)
                            throws ExitLoop, ContinueLoop, LoopException {
                        String tstr = sys.io.readText(task);
                        Map tjson = null;
                        int ldays = 0;
                        int progress = 0;
                        try {
                            tjson = Json.fromJsonAsMap(Object.class, tstr);
                            if (tjson == null) {
                                return;
                            }
                            tjson.put("name", task.name());
                            if (tjson.containsKey("progress")) {
                                progress = (int) tjson.get("progress");
                            }
                            if (tjson.containsKey("deadline")) {
                                String dl = (String) tjson.get("deadline");
                                Date dldate = new SimpleDateFormat("yyyy-MM-dd").parse(dl);
                                Date today = new Date();
                                if (dldate.getTime() > today.getTime()) {
                                    ldays = (int) ((dldate.getTime() - today.getTime()) / 86400000);
                                } else {
                                    ldays = -(int) ((today.getTime() - dldate.getTime())
                                                    / 86400000);
                                    if (ldays == 0) {
                                        ldays = -1;
                                    }
                                }
                                tjson.put("leftDays", ldays);
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        // 完成的
                        if (progress == 100) {
                            tasks_done.add(tjson);
                        }
                        // 过期的
                        else if (ldays < 0) {
                            tasks_overdue.add(tjson);
                        }
                        // 7天内
                        else if (ldays >= 0 && ldays <= 7) {
                            tasks_7day.add(tjson);
                        }
                        // 7天以后的
                        else if (ldays > 7) {
                            tasks_7day_after.add(tjson);
                        }
                    }
                });

                NutMap pinfo = NutMap.NEW();
                pinfo.put("name", project.name());
                pinfo.put("tasks_overdue", tasks_overdue);
                pinfo.put("tasks_7day", tasks_7day);
                pinfo.put("tasks_after", tasks_7day_after);
                pinfo.put("tasks_done", tasks_done);
                re.add(pinfo);
            }
        });
        // 返回查询结果
        sys.out.print(Json.toJson(re));
    }
}
