package org.nutz.walnut.ext.myproject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_myproject extends JvmExecutor {

    private long getDLTime(String dl) {
        try {
            return Times.parse("yyyy-MM-dd HH:mm:ss", dl + " 00:00:00").getTime();
        }
        catch (ParseException e) {
            throw Lang.wrapThrow(e);
        }
    }

    private long getTodayTime() {
        String tdstr = Times.sD(new Date());
        try {
            return Times.parse("yyyy-MM-dd HH:mm:ss", tdstr + " 00:00:00").getTime();
        }
        catch (ParseException e) {
            throw Lang.wrapThrow(e);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void exec(final WnSystem sys, String[] args) throws Exception {
        String projectPath = Wn.normalizeFullPath("~/.project", sys);
        sys.io.createIfNoExists(null, projectPath, WnRace.DIR);
        final List<NutMap> re = new ArrayList<NutMap>();
        WnObj projectObj = Wn.checkObj(sys, projectPath);
        sys.io.each(Wn.Q.pid(projectObj.id()), new Each<WnObj>() {
            @Override
            public void invoke(int index, WnObj project, int length)
                    throws ExitLoop, ContinueLoop, LoopException {
                final List<Map> tasks_overdue = new ArrayList<Map>();
                final List<Map> tasks_7day = new ArrayList<Map>();
                final List<Map> tasks_7day_after = new ArrayList<Map>();
                final List<Map> tasks_done = new ArrayList<Map>();
                // 遍历所有任务
                sys.io.each(Wn.Q.pid(project.id()), new Each<WnObj>() {
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
                            tjson.put("id", task.id());
                            tjson.put("name", task.name());
                            if (tjson.containsKey("progress")) {
                                progress = (int) tjson.get("progress");
                            }
                            if (tjson.containsKey("deadline")) {
                                long dlTime = getDLTime((String) tjson.get("deadline"));
                                long toTime = getTodayTime();
                                if (dlTime == toTime) {
                                    ldays = 0;
                                } else {
                                    ldays = (int) ((dlTime - toTime) / 86400000);
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
