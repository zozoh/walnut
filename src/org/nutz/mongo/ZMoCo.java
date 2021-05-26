package org.nutz.mongo;

import java.util.List;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.EstimatedDocumentCountOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

/**
 * 对于集合类的薄封装
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZMoCo {

    private MongoCollection<Document> dbc;

    public ZMoCo(MongoCollection<Document> c) {
        this.dbc = c;
    }

    public MongoNamespace getNamespace() {
        return dbc.getNamespace();
    }

    public Class<Document> getDocumentClass() {
        return dbc.getDocumentClass();
    }

    public CodecRegistry getCodecRegistry() {
        return dbc.getCodecRegistry();
    }

    public ReadPreference getReadPreference() {
        return dbc.getReadPreference();
    }

    public WriteConcern getWriteConcern() {
        return dbc.getWriteConcern();
    }

    public ReadConcern getReadConcern() {
        return dbc.getReadConcern();
    }

    public <NewTDocument> MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> clazz) {
        return dbc.withDocumentClass(clazz);
    }

    public MongoCollection<Document> withCodecRegistry(CodecRegistry codecRegistry) {
        return dbc.withCodecRegistry(codecRegistry);
    }

    public MongoCollection<Document> withReadPreference(ReadPreference readPreference) {
        return dbc.withReadPreference(readPreference);
    }

    public MongoCollection<Document> withWriteConcern(WriteConcern writeConcern) {
        return dbc.withWriteConcern(writeConcern);
    }

    public MongoCollection<Document> withReadConcern(ReadConcern readConcern) {
        return dbc.withReadConcern(readConcern);
    }

    public long countDocuments() {
        return dbc.countDocuments();
    }

    public long countDocuments(Bson filter) {
        return dbc.countDocuments(filter);
    }

    public long countDocuments(Bson filter, CountOptions options) {
        return dbc.countDocuments(filter, options);
    }

    public long countDocuments(ClientSession clientSession) {
        return dbc.countDocuments(clientSession);
    }

    public long countDocuments(ClientSession clientSession, Bson filter) {
        return dbc.countDocuments(clientSession, filter);
    }

    public long countDocuments(ClientSession clientSession, Bson filter, CountOptions options) {
        return dbc.countDocuments(clientSession, filter, options);
    }

    public long estimatedDocumentCount() {
        return dbc.estimatedDocumentCount();
    }

    public long estimatedDocumentCount(EstimatedDocumentCountOptions options) {
        return dbc.estimatedDocumentCount(options);
    }

    public <TResult> DistinctIterable<TResult> distinct(String fieldName,
                                                        Class<TResult> resultClass) {
        return dbc.distinct(fieldName, resultClass);
    }

    public <TResult> DistinctIterable<TResult> distinct(String fieldName,
                                                        Bson filter,
                                                        Class<TResult> resultClass) {
        return dbc.distinct(fieldName, filter, resultClass);
    }

    public <TResult> DistinctIterable<TResult> distinct(ClientSession clientSession,
                                                        String fieldName,
                                                        Class<TResult> resultClass) {
        return dbc.distinct(clientSession, fieldName, resultClass);
    }

    public <TResult> DistinctIterable<TResult> distinct(ClientSession clientSession,
                                                        String fieldName,
                                                        Bson filter,
                                                        Class<TResult> resultClass) {
        return dbc.distinct(clientSession, fieldName, filter, resultClass);
    }

    public FindIterable<Document> find() {
        return dbc.find();
    }

    public <TResult> FindIterable<TResult> find(Class<TResult> resultClass) {
        return dbc.find(resultClass);
    }

    public FindIterable<Document> find(Bson filter) {
        return dbc.find(filter);
    }

    public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> resultClass) {
        return dbc.find(filter, resultClass);
    }

    public FindIterable<Document> find(ClientSession clientSession) {
        return dbc.find(clientSession);
    }

    public <TResult> FindIterable<TResult> find(ClientSession clientSession,
                                                Class<TResult> resultClass) {
        return dbc.find(clientSession, resultClass);
    }

    public FindIterable<Document> find(ClientSession clientSession, Bson filter) {
        return dbc.find(clientSession, filter);
    }

    public <TResult> FindIterable<TResult> find(ClientSession clientSession,
                                                Bson filter,
                                                Class<TResult> resultClass) {
        return dbc.find(clientSession, filter, resultClass);
    }

    public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
        return dbc.aggregate(pipeline);
    }

    public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline,
                                                          Class<TResult> resultClass) {
        return dbc.aggregate(pipeline, resultClass);
    }

    public AggregateIterable<Document> aggregate(ClientSession clientSession,
                                                 List<? extends Bson> pipeline) {
        return dbc.aggregate(clientSession, pipeline);
    }

    public <TResult> AggregateIterable<TResult> aggregate(ClientSession clientSession,
                                                          List<? extends Bson> pipeline,
                                                          Class<TResult> resultClass) {
        return dbc.aggregate(clientSession, pipeline, resultClass);
    }

    public ChangeStreamIterable<Document> watch() {
        return dbc.watch();
    }

    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
        return dbc.watch(resultClass);
    }

    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return dbc.watch(pipeline);
    }

    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline,
                                                         Class<TResult> resultClass) {
        return dbc.watch(pipeline, resultClass);
    }

    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return dbc.watch(clientSession);
    }

    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession,
                                                         Class<TResult> resultClass) {
        return dbc.watch(clientSession, resultClass);
    }

    public ChangeStreamIterable<Document> watch(ClientSession clientSession,
                                                List<? extends Bson> pipeline) {
        return dbc.watch(clientSession, pipeline);
    }

    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession,
                                                         List<? extends Bson> pipeline,
                                                         Class<TResult> resultClass) {
        return dbc.watch(clientSession, pipeline, resultClass);
    }

    public MapReduceIterable<Document> mapReduce(String mapFunction, String reduceFunction) {
        return dbc.mapReduce(mapFunction, reduceFunction);
    }

    public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction,
                                                          String reduceFunction,
                                                          Class<TResult> resultClass) {
        return dbc.mapReduce(mapFunction, reduceFunction, resultClass);
    }

    public MapReduceIterable<Document> mapReduce(ClientSession clientSession,
                                                 String mapFunction,
                                                 String reduceFunction) {
        return dbc.mapReduce(clientSession, mapFunction, reduceFunction);
    }

    public <TResult> MapReduceIterable<TResult> mapReduce(ClientSession clientSession,
                                                          String mapFunction,
                                                          String reduceFunction,
                                                          Class<TResult> resultClass) {
        return dbc.mapReduce(clientSession, mapFunction, reduceFunction, resultClass);
    }

    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests) {
        return dbc.bulkWrite(requests);
    }

    public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests,
                                     BulkWriteOptions options) {
        return dbc.bulkWrite(requests, options);
    }

    public BulkWriteResult bulkWrite(ClientSession clientSession,
                                     List<? extends WriteModel<? extends Document>> requests) {
        return dbc.bulkWrite(clientSession, requests);
    }

    public BulkWriteResult bulkWrite(ClientSession clientSession,
                                     List<? extends WriteModel<? extends Document>> requests,
                                     BulkWriteOptions options) {
        return dbc.bulkWrite(clientSession, requests, options);
    }

    public InsertOneResult insertOne(Document document) {
        return dbc.insertOne(document);
    }

    public InsertOneResult insertOne(Document document, InsertOneOptions options) {
        return dbc.insertOne(document, options);
    }

    public InsertOneResult insertOne(ClientSession clientSession, Document document) {
        return dbc.insertOne(clientSession, document);
    }

    public InsertOneResult insertOne(ClientSession clientSession,
                                     Document document,
                                     InsertOneOptions options) {
        return dbc.insertOne(clientSession, document, options);
    }

    public InsertManyResult insertMany(List<? extends Document> documents) {
        return dbc.insertMany(documents);
    }

    public InsertManyResult insertMany(List<? extends Document> documents,
                                       InsertManyOptions options) {
        return dbc.insertMany(documents, options);
    }

    public InsertManyResult insertMany(ClientSession clientSession,
                                       List<? extends Document> documents) {
        return dbc.insertMany(clientSession, documents);
    }

    public InsertManyResult insertMany(ClientSession clientSession,
                                       List<? extends Document> documents,
                                       InsertManyOptions options) {
        return dbc.insertMany(clientSession, documents, options);
    }

    public DeleteResult deleteOne(Bson filter) {
        return dbc.deleteOne(filter);
    }

    public DeleteResult deleteOne(Bson filter, DeleteOptions options) {
        return dbc.deleteOne(filter, options);
    }

    public DeleteResult deleteOne(ClientSession clientSession, Bson filter) {
        return dbc.deleteOne(clientSession, filter);
    }

    public DeleteResult deleteOne(ClientSession clientSession, Bson filter, DeleteOptions options) {
        return dbc.deleteOne(clientSession, filter, options);
    }

    public DeleteResult deleteMany(Bson filter) {
        return dbc.deleteMany(filter);
    }

    public DeleteResult deleteMany(Bson filter, DeleteOptions options) {
        return dbc.deleteMany(filter, options);
    }

    public DeleteResult deleteMany(ClientSession clientSession, Bson filter) {
        return dbc.deleteMany(clientSession, filter);
    }

    public DeleteResult deleteMany(ClientSession clientSession,
                                   Bson filter,
                                   DeleteOptions options) {
        return dbc.deleteMany(clientSession, filter, options);
    }

    public UpdateResult replaceOne(Bson filter, Document replacement) {
        return dbc.replaceOne(filter, replacement);
    }

    public UpdateResult replaceOne(Bson filter,
                                   Document replacement,
                                   ReplaceOptions replaceOptions) {
        return dbc.replaceOne(filter, replacement, replaceOptions);
    }

    public UpdateResult replaceOne(ClientSession clientSession, Bson filter, Document replacement) {
        return dbc.replaceOne(clientSession, filter, replacement);
    }

    public UpdateResult replaceOne(ClientSession clientSession,
                                   Bson filter,
                                   Document replacement,
                                   ReplaceOptions replaceOptions) {
        return dbc.replaceOne(clientSession, filter, replacement, replaceOptions);
    }

    public UpdateResult updateOne(Bson filter, Bson update) {
        return dbc.updateOne(filter, update);
    }

    public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
        return dbc.updateOne(filter, update, updateOptions);
    }

    public UpdateResult updateOne(ClientSession clientSession, Bson filter, Bson update) {
        return dbc.updateOne(clientSession, filter, update);
    }

    public UpdateResult updateOne(ClientSession clientSession,
                                  Bson filter,
                                  Bson update,
                                  UpdateOptions updateOptions) {
        return dbc.updateOne(clientSession, filter, update, updateOptions);
    }

    public UpdateResult updateOne(Bson filter, List<? extends Bson> update) {
        return dbc.updateOne(filter, update);
    }

    public UpdateResult updateOne(Bson filter,
                                  List<? extends Bson> update,
                                  UpdateOptions updateOptions) {
        return dbc.updateOne(filter, update, updateOptions);
    }

    public UpdateResult updateOne(ClientSession clientSession,
                                  Bson filter,
                                  List<? extends Bson> update) {
        return dbc.updateOne(clientSession, filter, update);
    }

    public UpdateResult updateOne(ClientSession clientSession,
                                  Bson filter,
                                  List<? extends Bson> update,
                                  UpdateOptions updateOptions) {
        return dbc.updateOne(clientSession, filter, update, updateOptions);
    }

    public UpdateResult updateMany(Bson filter, Bson update) {
        return dbc.updateMany(filter, update);
    }

    public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
        return dbc.updateMany(filter, update, updateOptions);
    }

    public UpdateResult updateMany(ClientSession clientSession, Bson filter, Bson update) {
        return dbc.updateMany(clientSession, filter, update);
    }

    public UpdateResult updateMany(ClientSession clientSession,
                                   Bson filter,
                                   Bson update,
                                   UpdateOptions updateOptions) {
        return dbc.updateMany(clientSession, filter, update, updateOptions);
    }

    public UpdateResult updateMany(Bson filter, List<? extends Bson> update) {
        return dbc.updateMany(filter, update);
    }

    public UpdateResult updateMany(Bson filter,
                                   List<? extends Bson> update,
                                   UpdateOptions updateOptions) {
        return dbc.updateMany(filter, update, updateOptions);
    }

    public UpdateResult updateMany(ClientSession clientSession,
                                   Bson filter,
                                   List<? extends Bson> update) {
        return dbc.updateMany(clientSession, filter, update);
    }

    public UpdateResult updateMany(ClientSession clientSession,
                                   Bson filter,
                                   List<? extends Bson> update,
                                   UpdateOptions updateOptions) {
        return dbc.updateMany(clientSession, filter, update, updateOptions);
    }

    public Document findOneAndDelete(Bson filter) {
        return dbc.findOneAndDelete(filter);
    }

    public Document findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
        return dbc.findOneAndDelete(filter, options);
    }

    public Document findOneAndDelete(ClientSession clientSession, Bson filter) {
        return dbc.findOneAndDelete(clientSession, filter);
    }

    public Document findOneAndDelete(ClientSession clientSession,
                                     Bson filter,
                                     FindOneAndDeleteOptions options) {
        return dbc.findOneAndDelete(clientSession, filter, options);
    }

    public Document findOneAndReplace(Bson filter, Document replacement) {
        return dbc.findOneAndReplace(filter, replacement);
    }

    public Document findOneAndReplace(Bson filter,
                                      Document replacement,
                                      FindOneAndReplaceOptions options) {
        return dbc.findOneAndReplace(filter, replacement, options);
    }

    public Document findOneAndReplace(ClientSession clientSession,
                                      Bson filter,
                                      Document replacement) {
        return dbc.findOneAndReplace(clientSession, filter, replacement);
    }

    public Document findOneAndReplace(ClientSession clientSession,
                                      Bson filter,
                                      Document replacement,
                                      FindOneAndReplaceOptions options) {
        return dbc.findOneAndReplace(clientSession, filter, replacement, options);
    }

    public Document findOneAndUpdate(Bson filter, Bson update) {
        return dbc.findOneAndUpdate(filter, update);
    }

    public Document findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
        return dbc.findOneAndUpdate(filter, update, options);
    }

    public Document findOneAndUpdate(ClientSession clientSession, Bson filter, Bson update) {
        return dbc.findOneAndUpdate(clientSession, filter, update);
    }

    public Document findOneAndUpdate(ClientSession clientSession,
                                     Bson filter,
                                     Bson update,
                                     FindOneAndUpdateOptions options) {
        return dbc.findOneAndUpdate(clientSession, filter, update, options);
    }

    public Document findOneAndUpdate(Bson filter, List<? extends Bson> update) {
        return dbc.findOneAndUpdate(filter, update);
    }

    public Document findOneAndUpdate(Bson filter,
                                     List<? extends Bson> update,
                                     FindOneAndUpdateOptions options) {
        return dbc.findOneAndUpdate(filter, update, options);
    }

    public Document findOneAndUpdate(ClientSession clientSession,
                                     Bson filter,
                                     List<? extends Bson> update) {
        return dbc.findOneAndUpdate(clientSession, filter, update);
    }

    public Document findOneAndUpdate(ClientSession clientSession,
                                     Bson filter,
                                     List<? extends Bson> update,
                                     FindOneAndUpdateOptions options) {
        return dbc.findOneAndUpdate(clientSession, filter, update, options);
    }

    public void drop() {
        dbc.drop();
    }

    public void drop(ClientSession clientSession) {
        dbc.drop(clientSession);
    }

    public String createIndex(Bson keys) {
        return dbc.createIndex(keys);
    }

    public String createIndex(Bson keys, IndexOptions indexOptions) {
        return dbc.createIndex(keys, indexOptions);
    }

    public String createIndex(ClientSession clientSession, Bson keys) {
        return dbc.createIndex(clientSession, keys);
    }

    public String createIndex(ClientSession clientSession, Bson keys, IndexOptions indexOptions) {
        return dbc.createIndex(clientSession, keys, indexOptions);
    }

    public List<String> createIndexes(List<IndexModel> indexes) {
        return dbc.createIndexes(indexes);
    }

    public List<String> createIndexes(List<IndexModel> indexes,
                                      CreateIndexOptions createIndexOptions) {
        return dbc.createIndexes(indexes, createIndexOptions);
    }

    public List<String> createIndexes(ClientSession clientSession, List<IndexModel> indexes) {
        return dbc.createIndexes(clientSession, indexes);
    }

    public List<String> createIndexes(ClientSession clientSession,
                                      List<IndexModel> indexes,
                                      CreateIndexOptions createIndexOptions) {
        return dbc.createIndexes(clientSession, indexes, createIndexOptions);
    }

    public ListIndexesIterable<Document> listIndexes() {
        return dbc.listIndexes();
    }

    public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> resultClass) {
        return dbc.listIndexes(resultClass);
    }

    public ListIndexesIterable<Document> listIndexes(ClientSession clientSession) {
        return dbc.listIndexes(clientSession);
    }

    public <TResult> ListIndexesIterable<TResult> listIndexes(ClientSession clientSession,
                                                              Class<TResult> resultClass) {
        return dbc.listIndexes(clientSession, resultClass);
    }

    public void dropIndex(String indexName) {
        dbc.dropIndex(indexName);
    }

    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions) {
        dbc.dropIndex(indexName, dropIndexOptions);
    }

    public void dropIndex(Bson keys) {
        dbc.dropIndex(keys);
    }

    public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions) {
        dbc.dropIndex(keys, dropIndexOptions);
    }

    public void dropIndex(ClientSession clientSession, String indexName) {
        dbc.dropIndex(clientSession, indexName);
    }

    public void dropIndex(ClientSession clientSession, Bson keys) {
        dbc.dropIndex(clientSession, keys);
    }

    public void dropIndex(ClientSession clientSession,
                          String indexName,
                          DropIndexOptions dropIndexOptions) {
        dbc.dropIndex(clientSession, indexName, dropIndexOptions);
    }

    public void dropIndex(ClientSession clientSession,
                          Bson keys,
                          DropIndexOptions dropIndexOptions) {
        dbc.dropIndex(clientSession, keys, dropIndexOptions);
    }

    public void dropIndexes() {
        dbc.dropIndexes();
    }

    public void dropIndexes(ClientSession clientSession) {
        dbc.dropIndexes(clientSession);
    }

    public void dropIndexes(DropIndexOptions dropIndexOptions) {
        dbc.dropIndexes(dropIndexOptions);
    }

    public void dropIndexes(ClientSession clientSession, DropIndexOptions dropIndexOptions) {
        dbc.dropIndexes(clientSession, dropIndexOptions);
    }

    public void renameCollection(MongoNamespace newCollectionNamespace) {
        dbc.renameCollection(newCollectionNamespace);
    }

    public void renameCollection(MongoNamespace newCollectionNamespace,
                                 RenameCollectionOptions renameCollectionOptions) {
        dbc.renameCollection(newCollectionNamespace, renameCollectionOptions);
    }

    public void renameCollection(ClientSession clientSession,
                                 MongoNamespace newCollectionNamespace) {
        dbc.renameCollection(clientSession, newCollectionNamespace);
    }

    public void renameCollection(ClientSession clientSession,
                                 MongoNamespace newCollectionNamespace,
                                 RenameCollectionOptions renameCollectionOptions) {
        dbc.renameCollection(clientSession, newCollectionNamespace, renameCollectionOptions);
    }

}
