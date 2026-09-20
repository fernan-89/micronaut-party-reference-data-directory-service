package com.thinklab.infrastructure.adapter.out.persistence.repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.model.Organisation.Billing;
import com.thinklab.domain.model.Organisation.Contact;
import com.thinklab.domain.model.Organisation.OrganisationStatus;
import com.thinklab.domain.model.Organisation.OrganisationUnit;
import com.thinklab.domain.repository.OrganisationRepository;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.BillingDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.ContactDocument;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.OrganisationPersistenceMapper;
import com.thinklab.infrastructure.adapter.out.persistence.entity.OrganisationDocument.OrganisationUnitDocument;
import jakarta.inject.Singleton;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB Reactive Repository Adapter.
 * Implements pure Domain Ports using low-level Reactive Streams MongoDB Driver.
 * Strictly enforces Partial State Mutations for performance and consistency.
 */
@Singleton
public class OrganisationMongoRepositoryAdapter implements OrganisationRepository {

    private static final Logger log = LoggerFactory.getLogger(OrganisationMongoRepositoryAdapter.class);

    private static final String DATABASE_NAME = "thinklab_company_db";
    private static final String COLLECTION_NAME = "organisations";
    private static final String FIELD_ID = "_id";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    /**
     * The MongoDB driver's default codec registry has no codec for arbitrary POJOs such as
     * {@link OrganisationDocument} — it only covers BSON primitives. Without registering a
     * {@link PojoCodecProvider}, every read/write against this collection fails with
     * {@code CodecConfigurationException: Can't find a codec for ...}.
     */
    private static final CodecRegistry POJO_CODEC_REGISTRY = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    );

    private final MongoClient mongoClient;

    public OrganisationMongoRepositoryAdapter(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    private MongoCollection<OrganisationDocument> getCollection() {
        return mongoClient.getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME, OrganisationDocument.class)
                .withCodecRegistry(POJO_CODEC_REGISTRY);
    }

    @Override
    public Mono<Organisation> create(Organisation organisation) {
        log.debug("[PERSISTENCE] Monolithic create for Organisation Aggregate: {}", organisation.getId());

        OrganisationDocument document = OrganisationPersistenceMapper.toDocument(organisation);

        return Mono.from(getCollection().insertOne(document))
                .doOnSuccess(result -> log.debug("[PERSISTENCE] Aggregate successfully created in MongoDB"))
                .map(result -> organisation);
    }

    @Override
    public Mono<Organisation> findById(UUID id) {
        log.debug("[PERSISTENCE] Fetching Organisation Aggregate by ID: {}", id);

        return Mono.from(getCollection().find(Filters.eq(FIELD_ID, id)).first())
                .map(OrganisationPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Organisation> findAll() {
        log.debug("[PERSISTENCE] Fetching all Organisation Aggregates");

        return Flux.from(getCollection().find())
                .map(OrganisationPersistenceMapper::toDomain);
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

        return executeUpdate(id, filter, update);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, OrganisationStatus status) {
        log.debug("[PERSISTENCE] Partial Mutation: updateStatus for ID: {}", id);

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.set("status", status.name()),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(id, filter, update);
    }

    @Override
    public Mono<Void> addOrganisationUnit(UUID id, OrganisationUnit unit) {
        log.debug("[PERSISTENCE] Partial Mutation: addOrganisationUnit for ID: {}", id);

        OrganisationUnitDocument unitDoc = new OrganisationUnitDocument(
                unit.unitId(), unit.unitName(), unit.address(),
                unit.city(), unit.country(), unit.zipCode(), unit.status().name()
        );

        Bson filter = Filters.eq(FIELD_ID, id);
        Bson update = Updates.combine(
                Updates.push("organisationUnits", unitDoc),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(id, filter, update);
    }

    @Override
    public Mono<Void> updateOrganisationUnitStatus(UUID id, UUID unitId, OrganisationUnit.OrganisationUnitStatus status) {
        log.debug("[PERSISTENCE] Partial Mutation: updateOrganisationUnitStatus for organisation {} / unit {}", id, unitId);

        Bson filter = Filters.and(Filters.eq(FIELD_ID, id), Filters.eq("organisationUnits.unitId", unitId));
        Bson update = Updates.combine(
                Updates.set("organisationUnits.$.status", status.name()),
                Updates.set(FIELD_UPDATED_AT, Instant.now())
        );

        return executeUpdate(id, filter, update);
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

        return executeUpdate(id, filter, update);
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

        return executeUpdate(id, filter, update);
    }

    /**
     * Helper method to execute partial updates and handle the reactive signals.
     */
    private Mono<Void> executeUpdate(UUID id, Bson filter, Bson update) {
        return Mono.from(getCollection().updateOne(filter, update))
                .flatMap(result -> {
                    if (result.getMatchedCount() == 0) {
                        return Mono.error(new OrganisationNotFoundException(id));
                    }
                    return Mono.empty();
                });
    }
}
