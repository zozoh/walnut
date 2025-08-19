package com.site0.walnut.ext.xo.impl;

import java.io.IOException;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request.Builder;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3XoServiceTest extends AbstractXoServiceTest {

    @Override
    protected String getConfCateName() {
        return "s3";
    }

    @Override
    protected void update_config(String prefix, String[] allowActions, NutMap conf) {
        conf.put("secretId", setup.getConifg("s3-secret-id"));
        conf.put("secretKey", setup.getConifg("s3-secret-key"));
        conf.put("bucket", setup.getConifg("s3-bucket"));
        conf.put("region", setup.getConifg("s3-region"));
        if (!Ws.isBlank(prefix)) {
            conf.put("prefix", prefix);
        }
        if (null != allowActions && allowActions.length > 0) {
            conf.put("allowActions", allowActions);
        }
    }

    @Override
    protected XoService create_service(String confName) {
        return new S3XoService(io, oMyHome, confName);
    }

    // @org.junit.Test
    public void raw_example() throws IOException {
        String accessKey = setup.getConifg("s3-secret-id");
        String secretKey = setup.getConifg("s3-secret-key");
        String bucketName = setup.getConifg("s3-bucket");
        Region region = Region.of(setup.getConifg("s3-region"));

        // Create S3 client
        AwsBasicCredentials cred = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider provider = StaticCredentialsProvider.create(cred);
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(provider).build();

        // List objects in the bucket
        Builder bd = ListObjectsV2Request.builder();
        bd.bucket(bucketName);
        bd.prefix("a");
        ListObjectsV2Request listReq = bd.build();

        ListObjectsV2Response listRes = s3.listObjectsV2(listReq);

        System.out.println("Files in bucket:");
        for (S3Object obj : listRes.contents()) {
            System.out.println(" - " + obj.key() + " (" + obj.size() + " bytes)");
        }
        s3.close();
    }
}
