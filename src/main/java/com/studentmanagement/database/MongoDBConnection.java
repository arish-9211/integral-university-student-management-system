package com.studentmanagement.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import java.util.concurrent.TimeUnit;
import org.bson.Document;

public class MongoDBConnection implements AutoCloseable {
  private final MongoClient client;
  private final MongoDatabase database;

  public MongoDBConnection() {
    String uri = System.getenv().getOrDefault("MONGODB_URI", "mongodb://127.0.0.1:27017");
    client =
        MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToClusterSettings(b -> b.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build());
    database =
        client.getDatabase(
            System.getenv().getOrDefault("MONGODB_DATABASE", "student_management_system"));
    database.runCommand(new Document("ping", 1));
  }

  public MongoDatabase getDatabase() {
    return database;
  }

  public void close() {
    client.close();
  }
}
