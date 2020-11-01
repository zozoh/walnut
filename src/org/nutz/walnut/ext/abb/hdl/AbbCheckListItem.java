package org.nutz.walnut.ext.abb.hdl;

public class AbbCheckListItem {

	public String no;
	public String parentStr;
	public String selfStr;
	public String desc;
	public String rs; // robot || station
	public String sf; // site || F.A.S.T
	public String outfiles;
	public String owner;
	public Integer min; // 耗时
	public Long workhour;

	public AbbCheckListItem() {
		// TODO Auto-generated constructor stub
	}

	public AbbCheckListItem(String no, String parentStr, String selfStr) {
		super();
		this.no = no;
		this.parentStr = parentStr;
		this.selfStr = selfStr;
	}

}
