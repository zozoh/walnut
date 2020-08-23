package org.nutz.walnut.ext.aliyun.oss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;

public class WnAliyunOssService {
	
	private static final Log log = Logs.get();

	public OSS oss;
	public String bucketName;
	public WnIo io;

	public WnAliyunOssService(WnIo io, OSS oss, String bucketName) {
		this.oss = oss;
		this.bucketName = bucketName;
		this.io = io;
	}

	// 上传
	public void upload(WnObj wobj, String objectName, NutMap meta, boolean force) {
		objectName = _name(objectName);
		try {
			String sha1 = wobj.sha1();
			if (Strings.isBlank(sha1)) {
				try (InputStream ins = io.getInputStream(wobj, 0)) {
					sha1 = Lang.sha1(ins);
				}
			}
			if (!force) {
				try {
					ObjectMetadata origin_meta = oss.getObjectMetadata(bucketName, objectName);
					if (sha1.equals(origin_meta.getUserMetadata().get("walnut-sha1"))) {
						// SHA1相同,再对比一下meta
						boolean flag = false;
						for (Entry<String, Object> en : meta.entrySet()) {
							String myValue = String.valueOf(en.getValue());
							String ossValue = String.valueOf(origin_meta.getRawMetadata().get(en.getKey()));
							if (!myValue.equalsIgnoreCase(ossValue)) {
								flag = true;
								break; // 元数据不同,那就强制上传
							}
						}
						if (!flag) {
							log.debug("sha1 match, meta match, no need to upload :" + objectName);
							return; // 相同,无需上传
						}
					}
				} catch (Exception e) {
					// pass
				}
			}
			InputStream ins = null;
			try {
				ins = io.getInputStream(wobj, 0);
				ObjectMetadata obj_meta = new ObjectMetadata();
				for (Entry<String, Object> en : meta.entrySet()) {
					obj_meta.setHeader(en.getKey(), String.valueOf(en.getValue()));
				}
				obj_meta.addUserMetadata("walnut-sha1", sha1);
				
				// 对mjs特殊处理一下
				if (obj_meta.getContentType() == null && wobj.name().endsWith(".mjs")) {
					obj_meta.setContentType("application/x-javascript");
				}
				
				oss.putObject(bucketName, objectName, ins, obj_meta);
			} finally {
				Streams.safeClose(ins);
			}
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.upload", e);
		}
	}

	protected void upload(InputStream ins, String objectName) {
		objectName = _name(objectName);
		oss.putObject(bucketName, objectName, ins);
	}

	// 下载
	public void downlaod(WnObj wobj, String objectName) {
		objectName = _name(objectName);
		try {
			OSSObject ossObject = oss.getObject(bucketName, objectName);
			if (ossObject != null) {
				io.writeAndClose(wobj, ossObject.getObjectContent());
			}
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.download", e);
		}
	}

	protected void downlaod(OutputStream out, String objectName) throws IOException {
		objectName = _name(objectName);
		OSSObject ossObject = oss.getObject(bucketName, objectName);
		if (ossObject != null) {
			try (InputStream ins = ossObject.getObjectContent()) {
				Streams.write(out, ins);
			}
		}
	}

	public void remove(String objectName) {
		objectName = _name(objectName);
		try {
			oss.deleteObject(bucketName, objectName);
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.remove", e);
		}
	}

	public boolean exist(String objectName) {
		objectName = _name(objectName);
		try {
			return oss.doesObjectExist(bucketName, objectName);
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.exist", e);
		}
	}

	public NutMap getMeta(String objectName) {
		objectName = _name(objectName);
		try {
			OSSObject ossObject = oss.getObject(bucketName, objectName);
			NutMap re = null;
			if (ossObject == null) {
				// nothing
				re = new NutMap();
			} else {
				re = new NutMap(ossObject.getObjectMetadata().getRawMetadata());
			}
			return re;
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.getMeta", e);
		}
	}

	public boolean setMeta(String objectName, NutMap map) {
		objectName = _name(objectName);
		try {
			ObjectMetadata meta = oss.getObjectMetadata(bucketName, objectName);
			boolean flag = false;
			if (meta == null) {
				// nothing
			} else {
				flag = true;
				for (Map.Entry<String, Object> en : map.entrySet()) {
					meta.setHeader(en.getKey(), String.valueOf(en.getValue()));
				}
				CopyObjectRequest request = new CopyObjectRequest(bucketName, objectName, bucketName, objectName);
				request.setNewObjectMetadata(meta);
				oss.copyObject(request);
			}
			return flag;
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.setMeta", e);
		}
	}

	// 如果是/开头的objectName,移除前缀
	public String _name(String objectName) {
		if (objectName.startsWith("/")) {
			if (objectName.length() == 0)
				return "";
			return objectName.substring(1);
		}
		return objectName;
	}

	public List<String> lsdir(String objectName, String marker) {
		objectName = _name(objectName);
		if (objectName.length() > 0 && !objectName.endsWith("/"))
			objectName += "/";
		ListObjectsRequest request = new ListObjectsRequest(bucketName);
		request.setPrefix(objectName);
		request.setDelimiter("/");
		if (marker != null)
			request.setMarker(marker);
		request.setMaxKeys(1000);
		ObjectListing listing = oss.listObjects(request);
		List<String> names = new ArrayList<String>();
		for (String name : listing.getCommonPrefixes()) {
			name = name.substring(objectName.length());
			if (!name.isEmpty())
				names.add(name);
		}
		for (OSSObjectSummary summary : listing.getObjectSummaries()) {
			String name = summary.getKey();
			name = name.substring(objectName.length());
			if (!name.isEmpty())
				names.add(name);
		}
		if (listing.getNextMarker() != null) {
			names.addAll(lsdir(objectName, listing.getNextMarker()));
		}
		return names;
	}

	public void mkdir(String objectName) {
		if (!objectName.endsWith("/"))
			objectName = objectName + "/";
		try {
			upload(new ByteArrayInputStream(new byte[0]), objectName);
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.mkdir", e);
		}
	}
	
	public URL genURL(String objectName, String method, int timeout) {
		try {
			objectName = _name(objectName);
			return oss.generatePresignedUrl(bucketName, objectName, new Date(Wn.now()+timeout));
		} catch (Exception e) {
			throw Err.create("e.aliyun.oss.genurl", e);
		}
	}
}
