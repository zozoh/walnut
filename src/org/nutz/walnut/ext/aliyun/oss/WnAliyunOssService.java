package org.nutz.walnut.ext.aliyun.oss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;

public class WnAliyunOssService {

	public String endpoint;
	public String accessKeyId;
	public String accessKeySecret;
	public String bucketName;

	public WnIo io;
	
	public WnAliyunOssService(WnIo io) {
		this.io = io;
	}
	
	public WnAliyunOssService(WnIo io, NutMap conf) {
		this(io);
		endpoint = conf.getString("endpoint");
		accessKeyId = conf.getString("accessKeyId");
		accessKeySecret = conf.getString("accessKeySecret");
		bucketName = conf.getString("bucketName");
	}
	
	public WnAliyunOssService(WnIo io, WnObj wobj) {
		this(io, io.readJson(wobj, NutMap.class));
	}

	// 上传
	
	public void upload(WnObj wobj, String objectName) throws IOException {
		InputStream ins = null;
		try {
			ins = io.getInputStream(wobj, 0);
			upload(ins, objectName);
		}
		finally {
			Streams.safeClose(ins);
		}
	}

	public void upload(InputStream ins, String objectName) throws IOException {
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		try {
			ossClient.putObject(bucketName, objectName, ins);
		}
		finally {
			ossClient.shutdown();
		}
	}
	
	// 下载
	
	public void downlaod(WnObj wobj, String objectName) {
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		OSSObject ossObject = ossClient.getObject(bucketName, objectName);
		if (ossObject != null) {
			io.writeAndClose(wobj, ossObject.getObjectContent());
		}
		ossClient.shutdown();
	}

	public void downlaod(OutputStream out, String objectName) throws IOException {
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		OSSObject ossObject = ossClient.getObject(bucketName, objectName);
		if (ossObject != null) {
			try (InputStream ins = ossObject.getObjectContent()) {
				Streams.write(out, ins);
			}
		}
		ossClient.shutdown();
	}
	
	public void remove(String objectName) throws IOException {
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		ossClient.deleteObject(objectName, objectName);
		ossClient.shutdown();
	}
	
	public boolean exist(String objectName) {
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		boolean re = ossClient.doesObjectExist(bucketName, objectName);
		ossClient.shutdown();
		return re;
	}
}
















