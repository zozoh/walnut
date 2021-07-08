package org.nutz.walnut.alg.sum;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;

public class WnDigesting {

    private static int BUFF_SIZE = 16 * 1024;

    private MessageDigest digester;

    private int bufferSize;

    private List<InputStream> streamList;

    private List<byte[]> dataList;

    public WnDigesting(String algorithm) {
        this(algorithm, BUFF_SIZE);
    }

    public WnDigesting(String algorithm, int bufferSize) {
        this.streamList = new LinkedList<>();
        this.dataList = new LinkedList<>();
        this.bufferSize = bufferSize;
        this.setDigester(algorithm);
    }

    public String getString() {
        byte[] sum = this.getBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sum.length; i++) {
            sb.append(String.format("%02x", sum[i] & 0xff));
        }
        return sb.toString();
    }

    public byte[] getBytes() {
        digester.reset();
        // 更新流
        if (!streamList.isEmpty()) {
            byte[] bs = new byte[bufferSize];
            for (InputStream ins : streamList) {
                int len = 0;
                try {
                    while ((len = ins.read(bs)) != -1) {
                        digester.update(bs, 0, len);
                    }
                }
                catch (Exception e) {
                    throw Er.wrap(e);
                }
                finally {
                    Streams.safeClose(ins);
                }
            }
        }
        // 更新数据
        if (!dataList.isEmpty()) {
            for (byte[] data : dataList) {
                digester.update(data);
            }
        }
        // 得到结果
        return digester.digest();
    }

    public MessageDigest getDigester() {
        return digester;
    }

    public void setDigester(String name) {
        try {
            this.digester = MessageDigest.getInstance(name);
        }
        catch (NoSuchAlgorithmException e) {
            throw Er.wrap(e);
        }
    }

    public void setDigester(MessageDigest digest) {
        this.digester = digest;
    }

    public void addStream(InputStream ins) {
        this.streamList.add(ins);
    }

    public List<InputStream> getStreamList() {
        return streamList;
    }

    public void setStreamList(List<InputStream> inputStreams) {
        this.streamList = inputStreams;
    }

    public void addData(byte[] bs) {
        this.dataList.add(bs);
    }

    public List<byte[]> getDataList() {
        return dataList;
    }

    public void setDataList(List<byte[]> inputBytes) {
        this.dataList = inputBytes;
    }

}
