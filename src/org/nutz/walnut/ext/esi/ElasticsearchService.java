package org.nutz.walnut.ext.esi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.repo.cache.simple.LRUCache;
import org.nutz.walnut.api.io.WnObj;

@IocBean(create = "init", depose = "close", name = "esi")
public class ElasticsearchService implements BulkProcessor.Listener {

    private static final Log log = Logs.get();

    @Inject
    protected PropertiesProxy conf;

    protected PreBuiltTransportClient client;

    protected BulkProcessor bulk;
    
    protected boolean enable;
    
    protected LRUCache<String, EsiConf> esiConfs = new LRUCache<>(256);
    
    protected static final String _DOC = "_doc"; // 固定的type名称, 兼容5.x/6.x/7.x

    public void init() throws UnknownHostException {
        if (!conf.getBoolean("esi.enable", false)) {
            log.info("elasticsearch is disabled");
            return;
        }
        enable = true;
        log.info("loading elasticsearch ...");
        Settings.Builder builder = Settings.builder();

        // 设置ES实例的名称
        //builder.put("cluster.name", conf.get("esi.cluster.name", "walnut"));
        // 自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
        //builder.put("client.transport.sniff", false);

        client = new PreBuiltTransportClient(builder.build());
        String host = conf.get("esi.host", "127.0.0.1");
        int port = conf.getInt("esi.port", 9300);
        log.infof("elasticsearch %s:%s", host, port);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));

        bulk = createBulk();
    }

    public void close() {
        if (bulk != null) {
            bulk.flush();
            bulk.close();
        }
        if (client != null)
            client.close();
    }

    public BulkProcessor getBulk() {
        return bulk;
    }

    public BulkProcessor createBulk() {
        BulkProcessor.Builder bulkBuilder = BulkProcessor.builder(client, this);
        bulkBuilder.setFlushInterval(TimeValue.timeValueMillis(conf.getLong("esi.bulk.flushInterval", 3000)));
        bulkBuilder.setBulkActions(conf.getInt("esi.bulk.bulkActions", 100));
        bulkBuilder.setConcurrentRequests(conf.getInt("esi.bulk.threads", 8));
        return bulkBuilder.build();
    }
    
    public EsiConf getEsiConf(WnObj wobj) {
        NutMap _conf = wobj.getAs("esi_conf", NutMap.class);
        if (_conf == null)
            return null;
        String key = wobj.id() + "_" + wobj.lastModified();
        EsiConf conf = esiConfs.get(key);
        if (conf == null) {
            conf = Lang.map2Object(_conf, EsiConf.class);
            esiConfs.put(key, conf);
        }
        return conf;
    }

    //-------------------------------------------------------
    //                     数据维护
    //-------------------------------------------------------
    
    /**
     * 添加或更新元数据字段
     * @param conf 配置对象
     * @param wobj 数据对象
     */
    public void addOrUpdateMeta(EsiConf conf, WnObj wobj) {
        if (conf.getMapping() == null)
            return;
        NutMap data = new NutMap();
        for (Map.Entry<String, EsiMappingField> en : conf.getMapping().entrySet()) {
            if ("data".equals(en.getKey()))
                continue;
            EsiMappingField field = en.getValue();
            if (field.fields == null || field.fields.length == 0)
                data.put(en.getKey(), wobj.get(en.getKey()));
            else {
                StringBuilder sb = new StringBuilder();
                for (String fieldName : field.fields) {
                    sb.append(wobj.get(fieldName)).append(',');
                }
                sb.substring(0, sb.length() - 1);
                data.put(en.getKey(), sb.toString());
            }
        }
        UpdateRequest request = new UpdateRequest(wobj.creator() + "_" + conf.getName(), "_doc",  wobj.id());
        request.doc(data);
        request.docAsUpsert(true);
        bulk.add(request);
    }
    
    public void addOrUpdateMeta(String user, EsiConf conf, String id, NutMap data) {
        UpdateRequest request = new UpdateRequest(user + "_" + conf.getName(), "_doc", id);
        request.doc(data);
        request.docAsUpsert(true);
        client.update(request).actionGet();
    }

    /**
     * 更新文本数据
     * @param conf 配置对象
     * @param wobj 数据对象
     * @param data 数据
     */
    public void updateData(EsiConf conf, WnObj wobj, String data) {
        if (conf.getMapping() == null || conf.getMapping().isEmpty())
            return;
        if (!conf.getMapping().containsKey("data"))
            return;
        UpdateRequest request = new UpdateRequest(wobj.creator() + "_" + conf.getName(), "_doc",  wobj.id());
        request.doc(new NutMap("data", data));
        bulk.add(request);
    }
    
    /**
     * 删除记录
     * @param conf 配置对象
     * @param wobj 数据对象
     */
    public void delete(String user, EsiConf conf, String id) {
        DeleteRequest request = new DeleteRequest(user+"_"+conf.getName(), "_doc", id);
        bulk.add(request);
    }
    
    //-------------------------------------------------------
    //                     查询API
    //-------------------------------------------------------
    
    //-------------------------------------------------------
    //                     管理API
    //-------------------------------------------------------
    
    public void putMapping(String user, EsiConf conf) {
        String realname = user + "_" + conf.getName();
        AdminClient admin = client.admin();
        // 首先, 判断是否有索引
        IndicesExistsRequest uiq = new IndicesExistsRequest(realname);
        boolean isExists = admin.indices().exists(uiq).actionGet().isExists();
        if (isExists) { // 如果存在就用PutMapping
            PutMappingRequest request = new PutMappingRequest(realname);
            NutMap source = new NutMap();
            source.put("properties", conf.getMapping());
            String _source = Json.toJson(source,JsonFormat.full().setIgnoreNull(true));
            log.info("mapping:" + _source);
            request.source(_source, XContentType.JSON);
            request.type("_doc");
            admin.indices().putMapping(request).actionGet();
        }
        else { // 否则就用CreateIndexRequest
            CreateIndexRequest request = new CreateIndexRequest(realname);
            NutMap source = new NutMap();
            source.put("mappings", new NutMap("_doc", new NutMap("properties", conf.getMapping())));
            source.put("settting", new NutMap("number_of_shards", 5));
            String _source = Json.toJson(source,JsonFormat.full().setIgnoreNull(true));
            log.info("mapping:" + _source);
            request.source(_source, XContentType.JSON);
            admin.indices().create(request).actionGet();
        }
    }
    
    public void drop(String user, EsiConf conf) {
        String realname = user + "_" + conf.getName();
        DeleteIndexRequest request = Requests.deleteIndexRequest(realname);
        client.admin().indices().delete(request).actionGet();
    }
    
    //-------------------------------------------------------
    //                     其他API
    //-------------------------------------------------------
    
    public boolean isEnable() {
        return enable;
    }

    public PreBuiltTransportClient getClient() {
        return client;
    }

    public void beforeBulk(long executionId, BulkRequest request) {
        // nop
    }

    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debugf("id=%s request count=%s, resp hasFailures=%s", executionId, request.numberOfActions(), response.hasFailures());
    }

    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.debugf("id=%s request=%s", executionId, request, failure);
    }

}
