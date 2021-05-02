package org.nutz.walnut.ext.sys.cron;

import java.util.Date;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;

public interface WnSysCronApi {

    WnObj addCron(WnSysCron cron);

    WnSysCron getCronById(String id);

    WnSysCron checkCronById(String id);

    WnSysCron getCron(String name);

    WnSysCron checkCron(String name);

    void removeCron(WnSysCron cron);

    void removeCronObj(WnObj oCron);

    List<WnObj> listCronObj(WnSysCronQuery query, boolean loadContent);

    List<WnSysCron> listCron(WnSysCronQuery query, boolean loadContent);

    /**
     * 从给定的定期任务对象列表，根据每个对象的cron表达式，将自己展开到一个二维数组。
     * <p>
     * 这个矩阵格式是：
     * 
     * <pre>
     * MATRIX[分钟槽下标][任务下标】
     * </pre>
     * 
     * 譬如:
     * 
     * <pre>
     * 0:  [TASK][TASK][TASK]
     * 1:  null
     * 2:  null
     * 3:  [TASK]
     * ...
     * </pre>
     * 
     * 也就是说，如果<code>MATRIX[2][0]</code> 表示访问当天的第3分钟的第0个任务
     * <p>
     * <b>!!!注意</b>，分钟槽的内容可能是 NULL 或者空数组，都表示这个分钟槽没有任何任务。
     * <p>
     * 切记！切记！否则出了<code>NPE</code>我可不背锅。
     * 
     * @param list
     *            输入的定期任务
     * @param today
     *            今天几号？不是今天的任务统统会被无视。
     * @param slotN
     *            一天划分为多少个时间槽，如果精确到小时，是
     *            <code>24</code>，如果精确到分钟，是<code>1440</code>
     * @return 一个叠加好的定期任务对象矩阵
     */
    WnSysCron[][] previewCron(List<WnSysCron> list, Date today, int slotN);

}