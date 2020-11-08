package org.nutz.walnut.ext.abb.hdl;

public class AbbCheckListAItem {
	/**
	 * id : ID // 全局唯一ID nm : "c00" // 全局唯一检查项编号【人类】 title : "Cable check" // 标题
	 * p_ck_nm : "a01" // 父检查项目名称 enabled : true // 是否启用
	 */
	public String nm;
	public String title;
	public String p_ck_nm;
	public boolean enabled = true;
	public double workhour;
}