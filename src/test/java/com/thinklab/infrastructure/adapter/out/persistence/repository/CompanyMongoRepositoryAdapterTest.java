package com.thinklab.infrastructure.adapter.out.persistence.repository;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.model.Company.ContactRole;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument;
import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyMongoRepositoryAdapterTest {

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDatabase mongoDatabase;

    @Mock
    private MongoCollection<CompanyDocument> mongoCollection;

    private CompanyMongoRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        when(mongoClient.getDatabase("company_db")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("companies", CompanyDocument.class)).thenReturn(mongoCollection);
        repositoryAdapter = new CompanyMongoRepositoryAdapter(mongoClient);
    }

    @Test
    @DisplayName("Should successfully insert a new company aggregate")
    void testCreateSuccess() {
        UUID id = UUID.randomUUID();
        Company company = Company.createNew(id, "Thinklab Inc", "Thinklab", "11.222.333/0001-44", null);
        InsertOneResult insertResult = InsertOneResult.acknowledged(new BsonObjectId(new ObjectId()));

        when(mongoCollection.insertOne(any(CompanyDocument.class))).thenReturn(Mono.just(insertResult));

        StepVerifier.create(repositoryAdapter.create(company))
                .expectNextMatches(saved -> saved.getId().equals(id) && saved.getCorporateName().equals("Thinklab Inc"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully find company by ID")
    void testFindByIdSuccess() {
        UUID id = UUID.randomUUID();
        CompanyDocument doc = new CompanyDocument();
        doc.setId(id);
        doc.setCorporateName("Thinklab Inc");
        doc.setTradeName("Thinklab");
        doc.setTaxIdentifier("11.222.333/0001-44");
        doc.setStatus("ACTIVE");

        FindPublisher<CompanyDocument> findPublisher = mock(FindPublisher.class);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findPublisher);
        when(findPublisher.first()).thenReturn(Mono.just(doc));

        StepVerifier.create(repositoryAdapter.findById(id))
                .expectNextMatches(found -> found.getId().equals(id) && found.getCorporateName().equals("Thinklab Inc"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when finding non-existent company by ID")
    void testFindByIdNotFound() {
        UUID id = UUID.randomUUID();

        FindPublisher<CompanyDocument> findPublisher = mock(FindPublisher.class);
        when(mongoCollection.find(any(Bson.class))).thenReturn(findPublisher);
        when(findPublisher.first()).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findById(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully list all companies")
    void testFindAllSuccess() {
        CompanyDocument doc1 = new CompanyDocument();
        doc1.setId(UUID.randomUUID());
        doc1.setCorporateName("Company 1");
        doc1.setTradeName("Trade 1");
        doc1.setTaxIdentifier("11.111.111/0001-11");
        doc1.setStatus("ACTIVE");

        CompanyDocument doc2 = new CompanyDocument();
        doc2.setId(UUID.randomUUID());
        doc2.setCorporateName("Company 2");
        doc2.setTradeName("Trade 2");
        doc2.setTaxIdentifier("22.222.222/0001-22");
        doc2.setStatus("PENDING_ACTIVATION");

        FindPublisher<CompanyDocument> findPublisher = mock(FindPublisher.class);
        when(mongoCollection.find()).thenReturn(findPublisher);
        org.mockito.Mockito.doAnswer(invocation -> {
            org.reactivestreams.Subscriber<CompanyDocument> s = invocation.getArgument(0);
            Flux.just(doc1, doc2).subscribe(s);
            return null;
        }).when(findPublisher).subscribe(any());

        StepVerifier.create(repositoryAdapter.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update basic info atomically")
    void testUpdateBasicInfoSuccess() {
        UUID id = UUID.randomUUID();
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.updateBasicInfo(id, "New Name", "New Trade", "99.888.777/0001-66"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should error with NoSuchElementException when updateBasicInfo matches 0 documents")
    void testUpdateBasicInfoNotFound() {
        UUID id = UUID.randomUUID();
        UpdateResult updateResult = UpdateResult.acknowledged(0, 0L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.updateBasicInfo(id, "New Name", "New Trade", "99.888.777/0001-66"))
                .expectError(NoSuchElementException.class)
                .verify();
    }

    @Test
    @DisplayName("Should update status atomically")
    void testUpdateStatusSuccess() {
        UUID id = UUID.randomUUID();
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.updateStatus(id, CompanyStatus.SUSPENDED))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should add branch atomically via $push")
    void testAddBranchSuccess() {
        UUID id = UUID.randomUUID();
        Branch branch = new Branch(UUID.randomUUID(), "Branch Tech", "Street 1", "City", "Country", "123");
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.addBranch(id, branch))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should add contact atomically via $push")
    void testAddContactSuccess() {
        UUID id = UUID.randomUUID();
        Contact contact = new Contact(UUID.randomUUID(), "John", "john@thinklab.com", "123", ContactRole.ADMIN);
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.addContact(id, contact))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update billing atomically via $set")
    void testUpdateBillingSuccess() {
        UUID id = UUID.randomUUID();
        Billing billing = new Billing("bill@thinklab.com", "USD", "STANDARD");
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);

        when(mongoCollection.updateOne(any(Bson.class), any(Bson.class))).thenReturn(Mono.just(updateResult));

        StepVerifier.create(repositoryAdapter.updateBilling(id, billing))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete company by ID successfully")
    void testDeleteByIdSuccess() {
        UUID id = UUID.randomUUID();
        DeleteResult deleteResult = DeleteResult.acknowledged(1);

        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(Mono.just(deleteResult));

        StepVerifier.create(repositoryAdapter.deleteById(id))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should error with NoSuchElementException when deleteById matches 0 documents")
    void testDeleteByIdNotFound() {
        UUID id = UUID.randomUUID();
        DeleteResult deleteResult = DeleteResult.acknowledged(0);

        when(mongoCollection.deleteOne(any(Bson.class))).thenReturn(Mono.just(deleteResult));

        StepVerifier.create(repositoryAdapter.deleteById(id))
                .expectError(NoSuchElementException.class)
                .verify();
    }
}
