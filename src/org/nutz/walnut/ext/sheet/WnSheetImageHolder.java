package org.nutz.walnut.ext.sheet;

import java.awt.image.BufferedImage;

import org.nutz.walnut.api.io.WnIo;

public class WnSheetImageHolder implements SheetImageHolder {

	protected WnIo io;
	protected String path;
	
	
	
	public BufferedImage getImage() {
		return io.readImage(io.check(null, path));
	}



	public WnSheetImageHolder(WnIo io, String path) {
		super();
		this.io = io;
		this.path = path;
	}
}
