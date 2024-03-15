package org.nutz.filepool;

import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.random.R;

import java.io.File;

public class UU32FilePool implements FilePool {
	
	protected File root;
	
	public UU32FilePool(String path) {
		this.root = Files.createDirIfNoExists(path);
	}

	public File createFile(String suffix) {
		String key = R.UU32();
		File dir = new File(root, key.substring(0, 2));
		Files.createDirIfNoExists(dir);
		return new File(dir, key.substring(2));
	}
	public void clear() {
		Files.deleteDir(root);
		this.root = Files.createDirIfNoExists(root);
	}
	
	//-----------------------------
	// 其他方法一概不实现
	//-----------------------------

	public long current() {
		throw Wlang.noImplement();
	}

	public boolean hasFile(long fId, String suffix) {
		throw Wlang.noImplement();
	}

	@Override
	public File removeFile(long fId, String suffix) {
		throw Wlang.noImplement();
	}

	public long getFileId(File f) {
		throw Wlang.noImplement();
	}

	public File getFile(long fId, String suffix) {
		throw Wlang.noImplement();
	}

	public File returnFile(long fId, String suffix) {
		throw Wlang.noImplement();
	}

	public boolean hasDir(long fId) {
		throw Wlang.noImplement();
	}

	public File removeDir(long fId) {
		throw Wlang.noImplement();
	}

	public File createDir() {
		throw Wlang.noImplement();
	}

	public File getDir(long fId) {
		throw Wlang.noImplement();
	}

	public File returnDir(long fId) {
		throw Wlang.noImplement();
	}


}
