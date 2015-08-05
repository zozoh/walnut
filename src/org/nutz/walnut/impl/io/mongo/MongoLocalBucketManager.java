package org.nutz.walnut.impl.io.mongo;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoCo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnBucketManager;
import org.nutz.walnut.api.io.WnTree;

public class MongoLocalBucketManager implements WnBucketManager {

    private File home;

    private ZMoCo co;

    public MongoLocalBucketManager(File home, ZMoCo co) {
        this.home = home;
        this.co = co;
    }

    @Override
    public void _clean_for_unit_test() {
        Files.clearDir(home);
        co.remove(ZMoDoc.NEW());
    }

    @Override
    public WnBucket alloc(int blockSize) {
        // 得到 ID
        String id = R.UU32();

        // 创建目录
        String ph = __id2path(id);
        File dir = Files.getFile(home, ph);
        dir.mkdirs();

        // 创建桶对象
        MongoLocalBucket bu = new MongoLocalBucket().co(co).dir(dir);
        bu.setId(id);
        long nowms = System.currentTimeMillis();
        bu.setCreateTime(nowms);
        bu.setLastModified(nowms);
        bu.setLastOpened(nowms);
        bu.setBlockSize(blockSize);
        bu.setBlockNumber(0);

        // 保存桶对象
        bu.update();

        // 返回
        return bu;
    }

    @Override
    public int mergeSame(WnTree tree, int n) {
        throw Lang.noImplement();
    }

    @Override
    public WnBucket getById(String buid) {
        ZMoDoc doc = co.findOne(WnMongos.qID(buid));
        if (null != doc) {
            MongoLocalBucket bu = ZMo.me().fromDocToObj(doc, MongoLocalBucket.class);
            return __setup_bucket(bu);
        }
        return null;
    }

    @Override
    public WnBucket checkById(String buid) {
        WnBucket bu = getById(buid);
        if (null == bu)
            throw Er.create("e.io.bucket.noid", buid);
        return bu;
    }

    @Override
    public WnBucket getBySha1(String sha1) {
        ZMoDoc doc = co.findOne(ZMoDoc.NEW("sha1", sha1));
        if (null != doc) {
            MongoLocalBucket bu = ZMo.me().fromDocToObj(doc, MongoLocalBucket.class);
            return __setup_bucket(bu);
        }
        return null;
    }

    @Override
    public WnBucket checkBySha1(String sha1) {
        WnBucket bu = getBySha1(sha1);
        if (null == bu)
            throw Er.create("e.io.bucket.nosha1", sha1);
        return bu;
    }

    private String __id2path(String id) {
        return id.substring(0, 2) + "/" + id.substring(2);
    }

    private MongoLocalBucket __setup_bucket(MongoLocalBucket bu) {
        File dir = Files.getFile(home, __id2path(bu.getId()));
        bu.co(co).dir(dir);

        String pbid = bu.getParentBucketId();
        if (!Strings.isBlank(pbid)) {
            bu.setParentBucket(this.checkById(pbid));
        }
        return bu;
    }
}
