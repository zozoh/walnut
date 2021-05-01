package org.nutz.walnut.ext.data.pvg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

public class BizPvgService {

    private NutMap matrix;

    public BizPvgService(NutMap matrix) {
        this.matrix = matrix;
    }

    /**
     * 检查角色是否拥有全部指定动作的权限
     * 
     * @param role
     *            要检查的角色
     * @param actions
     *            动作名列表
     * @return
     *         <ul>
     *         <li><code>true</code> : 当前角色满足权限
     *         <li><code>false</code> : 当前角色不满足权限
     *         </ul>
     */
    public boolean canAll(String role, String... actions) {
        return can(null, false, role, actions);
    }

    /**
     * 检查角色是否至少拥有一个指定动作的权限
     * 
     * @param role
     *            要检查的角色
     * @param actions
     *            动作名列表
     * @return
     *         <ul>
     *         <li><code>true</code> : 当前角色满足权限
     *         <li><code>false</code> : 当前角色不满足权限
     *         </ul>
     */
    public boolean canOne(String role, String... actions) {
        return can(null, true, role, actions);
    }

    /**
     * 检查角色是否拥有指定动作的权限
     * 
     * @param isOr
     *            true 表示只要有一个满足即可。false 表示必须全部满足
     * @param role
     *            要检查的角色
     * @param actions
     *            动作名列表
     * @return
     *         <ul>
     *         <li><code>true</code> : 当前角色满足权限
     *         <li><code>false</code> : 当前角色不满足权限
     *         </ul>
     */
    public boolean can(boolean isOr, String role, String... actions) {
        return can(null, isOr, role, actions);
    }

    /**
     * 检查角色是否拥有指定动作的权限
     * 
     * @param data
     *            输出的每个动作权限检查结果,可以为 null
     * @param isOr
     *            true 表示只要有一个满足即可。false 表示必须全部满足
     * @param role
     *            要检查的角色
     * @param actions
     *            动作名列表
     * @return
     *         <ul>
     *         <li><code>true</code> : 当前角色满足权限
     *         <li><code>false</code> : 当前角色不满足权限
     *         </ul>
     */
    public boolean can(Map<String, Boolean> data, boolean isOr, String role, String... actions) {
        // 权限表为空，就不用看了
        NutMap map = matrix.getAs(role, NutMap.class);
        if (null == map || map.isEmpty()) {
            return false;
        }

        // 权限表为空，就不用看了
        if (null == map || map.isEmpty()) {
            return false;
        }

        // 初始化数据区
        if (null == data) {
            data = new HashMap<>();
        }

        // 首先检查一遍权限
        for (String anm : actions) {
            data.put(anm, map.getBoolean(anm));
        }

        // 或操作
        if (isOr) {
            for (String anm : data.keySet()) {
                if (data.get(anm)) {
                    return true;
                }
            }
        }
        // 默认是与
        for (String anm : data.keySet()) {
            if (!data.get(anm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return 权限矩阵表对象
     */
    public NutMap getMatrix() {
        return this.matrix;
    }

    /**
     * @return 权限矩阵的角色列表
     */
    public List<String> getRoleNames() {
        List<String> list = new ArrayList<>(matrix.size());
        list.addAll(matrix.keySet());
        return list;
    }

    /**
     * @return 权限矩阵的全部动作列表
     */
    public List<String> getActionNames() {
        NutMap map = new NutMap();
        for (String role : matrix.keySet()) {
            NutMap actions = matrix.getAs(role, NutMap.class);
            for (String anm : actions.keySet()) {
                map.put(anm, true);
            }
        }

        List<String> list = new ArrayList<>(matrix.size());
        list.addAll(map.keySet());
        return list;
    }
    
    

}
