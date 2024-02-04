package org.nutz.walnut.ext.geo.lbs.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;

/**
 * 封装了对于中国的省市区县的全部加载和读取的操作
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class LbsChina {

    private static final Log log = Wlog.getCMD();

    private static LbsChina _me = null;

    public static LbsChina getInstance() {
        if (null == _me) {
            synchronized (LbsChina.class) {
                if (null == _me) {
                    _me = new LbsChina();
                }
            }
        }
        return _me;
    }

    /**
     * 记录所有顶级的省或者直辖市
     */
    private List<LbsChinaAddr> tops;

    /**
     * 记录所有的区县下属街道地址列表
     */
    private Map<String, List<LbsChinaAddr>> towns;

    /**
     * 缓存所有城市直至街道的地址列表
     */
    private Map<String, LbsChinaAddr> map;

    public LbsChina() {
        File fData = Files.findFile("org/nutz/walnut/ext/geo/lbs/data/cn/data.properties");
        String content = Files.read(fData);

        // 准备表
        map = new HashMap<>();
        tops = new ArrayList<>(40);
        towns = new HashMap<>();

        // 逐行读取
        String[] lines = Strings.splitIgnoreBlank(content, "\r?\n");
        for (String line : lines) {
            // 空行或者注释行无视
            if (Strings.isBlank(line) || line.startsWith("#")) {
                continue;
            }
            // 解析
            LbsChinaAddr lca = new LbsChinaAddr(line);

            // 计入表
            map.put(lca.getCode(), lca);

            // 如果为直辖市，那么必须搞一个市辖区和市辖县出来
            if (lca.isMunicipalities()) {
                // 市辖区
                LbsChinaAddr lca1 = lca.getMyVirtualAddr(1);
                map.put(lca1.getCode(), lca1);

                // 重庆还有市辖县
                if (lca.getCode().equals("500000")) {
                    LbsChinaAddr lca2 = lca.getMyVirtualAddr(2);
                    map.put(lca2.getCode(), lca2);
                }
            }

            // 自治县
            if (lca.isAutonomousRegion()) {
                LbsChinaAddr lca9 = lca.getMyVirtualAddr(9);
                map.put(lca9.getCode(), lca9);
            }

            // 如果为街道，则加入索引
            if (lca.isLevelTown()) {
                String areaCode = lca.getAreaCode();
                List<LbsChinaAddr> townList = towns.get(areaCode);
                if (null == townList) {
                    townList = new LinkedList<>();
                    towns.put(areaCode, townList);
                }
                townList.add(lca);
            }
        }

        // 逐个填充数据
        for (Map.Entry<String, LbsChinaAddr> en : map.entrySet()) {
            LbsChinaAddr lca = en.getValue();
            try {
                lca.completeInfo(map);
            }
            catch (Exception e) {
                log.warnf("Fail to complteInfo : %s", lca);
            }

            // 计入顶级表
            if (lca.isLevelProvince()) {
                tops.add(lca);
            }

        }

    }

    /**
     * @param code
     *            地址编码，如果是 null 则返回顶级地址
     * @return 地址对象列表
     */
    public List<LbsChinaAddr> getAddressList(String code) {
        List<LbsChinaAddr> list;

        // 顶级地址
        if (Strings.isBlank(code)) {
            return _clone_list(tops);
        }
        
        // 格式化地址
        code = __tidy_code(code);

        // 获取子地址
        LbsChinaAddr lca = map.get(code);

        if (null == lca) {
            return null;
        }

        if (lca.isNoTown() || lca.isLevelTown()) {
            return new ArrayList<>();
        }

        // 地址为区，则直接查询
        if (lca.isLevelArea()) {
            List<LbsChinaAddr> addrList = towns.get(lca.getCode());
            return _clone_list(addrList);
        }

        // 那么就搞 省-市-区咯
        // 得到地址编码数组
        String[] addrCodes = lca.toAreaCodeArray();
        int index = lca.getLevel();
        list = new ArrayList<>(100);

        // 循环读取地址
        for (int i = 1; i < 100; i++) {
            addrCodes[index] = Strings.alignRight(i, 2, '0');
            String acode = Strings.join("", addrCodes);
            LbsChinaAddr lcaSub = map.get(acode);
            if (null != lcaSub) {
                list.add(lcaSub);
            }
        }

        // 搞定
        return list;
    }

    /**
     * @param code
     *            地址码
     * @return 地址对象
     */
    public LbsChinaAddr getAddress(String code) {
        code = __tidy_code(code);
        LbsChinaAddr lca = map.get(code);
        return lca == null ? lca : lca.clone();
    }

    private String __tidy_code(String code) {
        // 整理为12位地址编码
        if (code.length() > 6) {
            return Strings.alignLeft(code, 12, '0');
        }
        // 不到 6 位的整理为 6 位
        return Strings.alignLeft(code, 6, '0');
    }

    private List<LbsChinaAddr> _clone_list(List<LbsChinaAddr> addrList) {
        if (null == addrList)
            return null;
        List<LbsChinaAddr> list;
        list = new ArrayList<>(addrList.size());
        for (LbsChinaAddr lca : addrList) {
            list.add(lca.clone());
        }
        return list;
    }
}
