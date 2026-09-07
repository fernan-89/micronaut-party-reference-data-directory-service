package com.thinklab.infrastructure.adapter.out.persistence.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.thinklab.domain.model.Company;
import com.thinklab.domain.model.Company.Billing;
import com.thinklab.domain.model.Company.Branch;
import com.thinklab.domain.model.Company.CompanyStatus;
import com.thinklab.domain.model.Company.Contact;
import com.thinklab.domain.repository.CompanyRepository;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument.BillingDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument.BranchDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument.CompanyPersistenceMapper;
import com.thinklab.infrastructure.adapter.out.persistence.entity.CompanyDocument.ContactDocument;
import jakarta.inject.Singleton;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * MongoDB Reactive Repository Adapter.
 * Implements pure Domain Ports using low-level Reactive Streams MongoDB Driver.
 * Strictly enforces Partial State Mutations for performance and consistency.
 */
@Singleton
public class CompanyMongoRepositoryAdapter implements CompanyRepository {

    private static final Logger log = LoggerFactory.getLogger(CompanyMongoRepositoryAdapter.class);

    private static final String DATABASE_NAME = "company_db";
    private static final String COLLECTION_NAME = "companies";
    private static final String FIELD_ID = "_id";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private final MongoClient mongoClient;

    public CompanyMongoRepositoryAdapter(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    private MongoCollection<CompanyDocument> getCollection() {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME, CompanyDocument.class);
    }

    @Override
    public Mono<Company> create(Company company) {
        log.debug("[PERSISTENCE] Monolithic create for Company Aggregate: {}", company.getId());

        CompanyDocument document = CompanyPersistenceMapper.toDocument(company);

        return Mono.from(getCollection().insertOne(document))
                .doOnSuccess(result -> log.debug("[PERSISTENCE] Aggregate successfully created in MongoDB"))
                .map(result -> company);
    }

    @Override
    public Mono<Company> findById(UUID id) {
        log.debug("[PERSISTENCE] Fetching Company Aggregate by ID: {}", id);

        return Mono.from(getCollection().find(Filters.eq(FIELD_ID, id)).first())
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Company> findAll() {
        log.debug("[PERSISTENCE] Fetching all Company Aggregates");

        return Flux.from(getCollection().find())
                .map(CompanyPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> updateBasicInfo(UUID id, String corporateName, String tradeName, String taxIdentifier) {
        log.debug("[PERSISTENCE] Partial Mutation: updateBasicInfo for ID: {}", id);

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.set("corporateName", corporateName),
                Updates.set("tradeName", tradeName),
                Updates.set("taxIdentifier", taxIdentifier),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(filter, update);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, CompanyStatus status) {
        log.debug("[PERSISTENCE] Partial Mutation: updateStatus for ID: {}", id);

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.set("status", status.name()),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(filter, update);
    }

    @Override
    public Mono<Void> addBranch(UUID id, Branch branch) {
        log.debug("[PERSISTENCE] Partial Mutation: addBranch for ID: {}", id);

        BranchDocument branchDoc = new BranchDocument(
                branch.branchId(), branch.branchName(), branch.address(),
                branch.city(), branch.country(), branch.zipCode()
        );

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.push("branches", branchDoc),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(filter, update);
    }

    @Override
    public Mono<Void> addContact(UUID id, Contact contact) {
        log.debug("[PERSISTENCE] Partial Mutation: addContact for ID: {}", id);

        ContactDocument contactDoc = new ContactDocument(
                contact.contactId(), contact.fullName(), contact.email(),
                contact.phoneNumber(), contact.role().name()
        );

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.push("contacts", contactDoc),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(filter, update);
    }

    @Override
    public Mono<Void> updateBilling(UUID id, Billing billing) {
        log.debug("[PERSISTENCE] Partial Mutation: updateBilling for ID: {}", id);

        BillingDocument billingDoc = new BillingDocument(
                billing.billingEmail(), billing.currency(), billing.taxRegime()
        );

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.set("billing", billingDoc),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(filter, update);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        log.debug("[PERSISTENCE] Deleting Company Aggregate by ID: {}", id);

        return Mono.from(getCollection().deleteOne(Filters.eq(FIELD_ID, id)))
                .flatMap(result -> {
                    if (result.getDeletedCount() == 0) {
                        return Mono.error(new NoSuchElementException("Company not found with ID: " + id));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Helper method to execute partial updates and handle the reactive signals.
     */
    private Mono<Void> executeUpdate(Bson filter, Bson update) {
        return Mono.from(getCollection().updateOne(filter, update))
                .flatMap(result -> {
                    if (result.getMatchedCount() == 0) {
                        return Mono.error(new NoSuchElementException("Company not found for update."));
                    }
                    return Mono.empty();
                });
    }
}
