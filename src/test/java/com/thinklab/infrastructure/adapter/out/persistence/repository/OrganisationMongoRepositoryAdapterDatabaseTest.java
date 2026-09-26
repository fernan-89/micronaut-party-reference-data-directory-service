package com.thinklab.infrastructure.adapter.out.persistence.repository;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The adapter reads and writes the database named by {@code mongodb.uri}, not a hardcoded one. */
@SuppressWarnings("unchecked")
class OrganisationMongoRepositoryAdapterDatabaseTest {

    private MongoClient clientServing(String database) {
        MongoClient client = mock(MongoClient.class);
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        MongoCollection<OrganisationDocument> collection = mock(MongoCollection.class);
        FindPublisher<OrganisationDocument> find = mock(FindPublisher.class);
        when(client.getDatabase(database)).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("organisations", OrganisationDocument.class)).thenReturn(collection);
        when(collection.withCodecRegistry(any())).thenReturn(collection);
        when(collection.find()).thenReturn(find);
        org.mockito.Mockito.doAnswer(inv -> {
            Flux.<OrganisationDocument>empty().subscribe(inv.<org.reactivestreams.Subscriber<OrganisationDocument>>getArgument(0));
            return null;
        }).when(find).subscribe(any());
        return client;
    }

    @Test
    @DisplayName("the database named in mongodb.uri is the one used")
    void usesTheConfiguredDatabase() {
        MongoClient client = clientServing("tenant_directory");

        new OrganisationMongoRepositoryAdapter(client, "mongodb://mongo:27017/tenant_directory").findAll().collectList().block();

        verify(client).getDatabase("tenant_directory");
    }

    @Test
    @DisplayName("a URI without a database falls back to the default")
    void fallsBackToTheDefaultDatabase() {
        MongoClient client = clientServing(OrganisationMongoRepositoryAdapter.DEFAULT_DATABASE);

        new OrganisationMongoRepositoryAdapter(client, "mongodb://mongo:27017").findAll().collectList().block();

        verify(client).getDatabase(OrganisationMongoRepositoryAdapter.DEFAULT_DATABASE);
    }

    @Test
    @DisplayName("mongodb.uri is mandatory")
    void uriIsMandatory() {
        assertThrows(NullPointerException.class, () -> new OrganisationMongoRepositoryAdapter(mock(MongoClient.class), null));
    }
}
