package org.nutz.walnut.ext.media.sheet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.nutz.img.Images;
import org.nutz.walnut.api.io.WnIo;

public class WnSheetImageHolder implements SheetImageHolder {

	protected WnIo io;
	protected String path;
	
	
	
	public byte[] getImage(int w, int h) {
		BufferedImage image = io.readImage(io.check(null, path));
		image = Images.clipScale(image, w, h);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Images.writeJpeg(image, out, 0.8f);
		return out.toByteArray();
	}



	public WnSheetImageHolder(WnIo io, String path) {
		super();
		this.io = io;
		this.path = path;
	}
}
