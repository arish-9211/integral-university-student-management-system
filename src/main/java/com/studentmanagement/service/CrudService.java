package com.studentmanagement.service;

import com.google.gson.*;
import com.mongodb.client.model.*;
import com.studentmanagement.exception.AppException;
import com.studentmanagement.repository.MongoRepository;
import com.studentmanagement.util.Json;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;

/** Shared CRUD mechanics; module-specific business rules live in subclasses. */
public abstract class CrudService<T> {
  protected final MongoRepository<T> repository;
  protected final Class<T> type;
  protected final Object writeLock;
  private final List<String> searchFields, filterFields, sortFields;

  protected CrudService(
      MongoRepository<T> repo,
      Class<T> type,
      Object lock,
      List<String> search,
      List<String> filters,
      List<String> sorts) {
    repository = repo;
    this.type = type;
    writeLock = lock;
    searchFields = search;
    filterFields = filters;
    sortFields = sorts;
  }

  protected abstract void validate(JsonObject data);

  protected void beforeDelete(String id) {}

  protected JsonObject decorate(T item) {
    return Json.GSON.toJsonTree(item).getAsJsonObject();
  }

  public T require(String id) {
    T item = repository.get(id);
    if (item == null) throw new AppException(404, "Record not found.");
    return item;
  }

  public JsonObject detail(String id) {
    return decorate(require(id));
  }

  public Map<String, Object> list(Map<String, String> params) {
    List<Bson> clauses = new ArrayList<>();
    String query = params.getOrDefault("q", "").trim();
    if (query.length() > 100) throw new AppException(400, "Search must be at most 100 characters.");
    if (!query.isEmpty() && !searchFields.isEmpty()) {
      // Each word may match a different field, so a full first + last name works.
      for (String word : query.split("\\s+")) {
        clauses.add(
            Filters.or(
                searchFields.stream()
                    .map(f -> Filters.regex(f, Pattern.quote(word), "i"))
                    .toList()));
      }
    }
    for (String field : filterFields) {
      String value = params.getOrDefault(field, "");
      if (!value.isBlank())
        clauses.add(Filters.eq(field, field.equals("semester") ? parse(value, 1, 12) : value));
    }
    Bson filter = clauses.isEmpty() ? new Document() : Filters.and(clauses);
    int page = parse(params.getOrDefault("page", "1"), 1, 100000);
    int size = parse(params.getOrDefault("size", "10"), 1, 100);
    String sort = params.getOrDefault("sort", "createdAt");
    if (!sortFields.contains(sort)) sort = "createdAt";
    Bson order =
        params.getOrDefault("direction", "desc").equals("asc")
            ? Sorts.ascending(sort, "id")
            : Sorts.descending(sort, "id");
    long total = repository.count(filter);
    return Map.of(
        "items",
        repository.find(filter, order, (page - 1) * size, size).stream()
            .map(this::decorate)
            .toList(),
        "total",
        total,
        "page",
        page,
        "pages",
        Math.max(1, (total + size - 1) / size));
  }

  private int parse(String text, int min, int max) {
    try {
      int value = Integer.parseInt(text);
      if (value < min || value > max) throw new NumberFormatException();
      return value;
    } catch (NumberFormatException e) {
      throw new AppException(400, "Invalid page, page size or semester.");
    }
  }

  public JsonObject save(String id, JsonObject input) {
    synchronized (writeLock) {
      boolean create = id == null;
      T old = create ? null : require(id);
      validate(input);
      String now = Instant.now().toString();
      if (create) id = UUID.randomUUID().toString();
      input.addProperty("id", id);
      input.addProperty("updatedAt", now);
      input.addProperty(
          "createdAt",
          create
              ? now
              : Json.GSON.toJsonTree(old).getAsJsonObject().get("createdAt").getAsString());
      T model = Json.GSON.fromJson(input, type);
      repository.save(id, model, create);
      return decorate(model);
    }
  }

  public void delete(String id) {
    synchronized (writeLock) {
      require(id);
      beforeDelete(id);
      repository.delete(id);
    }
  }
}
