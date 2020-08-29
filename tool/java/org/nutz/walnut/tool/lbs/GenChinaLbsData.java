package org.nutz.walnut.tool.lbs;

import java.io.File;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.lbs.bean.LbsChinaAddr;

public class GenChinaLbsData {

    public static void main(String[] args) {
        String home = args[0];
        File dHome = Files.findFile(home);
        File fData = Files.getFile(dHome, "data.json");
        File fTown = Files.getFile(dHome, "town.json");
        File fCNFT = Files.getFile(dHome, "cnft.json");

        String sData = Files.read(fData);
        String sTown = Files.read(fTown);
        String sCNFT = Files.read(fCNFT);

        List<NutMap> addrs = Json.fromJsonAsList(NutMap.class, sData);
        List<NutMap> towns = Json.fromJsonAsList(NutMap.class, sTown);
        List<NutMap> cnfts = Json.fromJsonAsList(NutMap.class, sCNFT);

        // 归纳一个没有下级街道的城市编码映射表
        NutMap cityWithoutTown = new NutMap();
        for (NutMap cnft : cnfts) {
            String code = cnft.getString("code");
            cityWithoutTown.put(code, true);
        }

        // 准备输出结果
        String NWL = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        // 循环输出地址
        // 每行的结构为 [CODE]=[NAME][!]
        // 譬如： 110108=海淀区
        // 譬如： 110108003000=羊坊店街道
        // 譬如： 659010=胡杨河市! <- 叹号结尾表示无下级街道的城市
        for (NutMap addr : addrs) {
            String code = addr.getString("code");
            String name = addr.getString("name");
            boolean noTown = cityWithoutTown.getBoolean(code);

            // 解析地址
            LbsChinaAddr lca = new LbsChinaAddr(code, name, noTown);

            // 记入输出结果
            sb.append(lca.toString()).append(NWL);
        }

        // 准备输出乡镇
        sb.append("#------------------------------------").append(NWL);
        sb.append("#").append(NWL);
        sb.append("# Towns").append(NWL);
        sb.append("#").append(NWL);
        sb.append("#------------------------------------").append(NWL);

        for (NutMap addr : towns) {
            String code = addr.getString("code");
            String name = addr.getString("name");

            // 解析地址
            LbsChinaAddr lca = new LbsChinaAddr(code, name, true);

            // 记入输出结果
            sb.append(lca.toString()).append(NWL);
        }

        // 打印出来
        System.out.println(sb.toString());
    }

}
