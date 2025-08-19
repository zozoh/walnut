package com.site0.walnut.ext.xo.impl.s3;

import java.io.IOException;

import org.junit.Test;
import org.nutz.lang.Files;

import com.site0.walnut.BaseSessionTest;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request.Builder;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3XoServiceTest extends BaseSessionTest {

    private static final String conf_json;

    static {
        conf_json = Files.read("com/site0/walnut/ext/xo/impl/conf/s3_test.json");
    }

    @Test
    public void raw_example() throws IOException {
        String accessKey = setup.getConifg("s3-access-key-id");
        String secretKey = setup.getConifg("s3-secret-access-key");
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
