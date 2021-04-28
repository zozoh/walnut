package org.nutz.walnut.ext.media.sheet.impl;

import java.util.List;

import org.nutz.lang.util.NutMap;

public class SheetResult {

	public List<NutMap> list;
	
	public List<SheetImage> images;
	
	public SheetResult() {
	}

	public SheetResult(List<NutMap> list) {
		this.list = list;
	}
}
