package com.studentmanagement.repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.util.Json;
import java.util.*;
import org.bson.Document;
import org.bson.conversions.Bson;

/** The only generic persistence implementation. Mongo documents never leave this layer. */
public class MongoRepository<T> {
  public List<String> distinctText(String field) {
    return collection.distinct(field, String.class).into(new ArrayList<>());
  }

  protected final MongoCollection<Document> collection;
  private final Class<T> type;

  public MongoRepository(MongoDatabase db, String name, Class<T> type) {
    collection = db.getCollection(name);
    this.type = type;
    collection.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
  }

  public void unique(String... fields) {
    collection.createIndex(Indexes.ascending(fields), new IndexOptions().unique(true));
  }

  public void index(String... fields) {
    collection.createIndex(Indexes.ascending(fields));
  }

  private T decode(Document doc) {
    return doc == null ? null : Json.GSON.fromJson(doc.toJson(), type);
  }

  public T get(String id) {
    return decode(collection.find(Filters.eq("id", id)).first());
  }

  public T first(Bson filter) {
    return decode(collection.find(filter).first());
  }

  public long count(Bson filter) {
    return collection.countDocuments(filter);
  }

  public List<T> find(Bson filter, Bson sort, int skip, int limit) {
    List<T> items = new ArrayList<>();
    collection.find(filter).sort(sort).skip(skip).limit(limit).forEach(d -> items.add(decode(d)));
    return items;
  }

  public List<T> all() {
    return find(new Document(), Sorts.descending("createdAt"), 0, 0);
  }

  public void save(String id, T model, boolean create) {
    Document doc = Document.parse(Json.GSON.toJson(model));
    try {
      if (create) collection.insertOne(doc);
      else collection.replaceOne(Filters.eq("id", id), doc);
    } catch (MongoWriteException e) {
      if (e.getError().getCode() == 11000)
        throw new AppException(409, "A record with these unique details already exists.");
      throw e;
    }
  }

  public void delete(String id) {
    collection.deleteOne(Filters.eq("id", id));
  }
}
