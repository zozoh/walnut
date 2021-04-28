package org.nutz.walnut.ext.media.ffmpeg;

/**
 * 视频信息
 * @author wendal
 *
 */
public class VideoInfo {

	/**
	 * 视频时长
	 */
	private double length;
	private int width;
	private int height;
	private int frameCount;
	private int frameRate;
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getFrameCount() {
		return frameCount;
	}
	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}
	public int getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
}
