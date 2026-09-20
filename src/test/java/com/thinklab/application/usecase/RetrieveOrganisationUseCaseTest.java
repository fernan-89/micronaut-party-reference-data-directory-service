package com.thinklab.application.usecase;

import com.thinklab.domain.exception.OrganisationNotFoundException;
import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.repository.OrganisationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrieveOrganisationUseCaseTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private RetrieveOrganisationUseCase retrieveOrganisationUseCase;

    @Test
    @DisplayName("Should return organisation when ID exists")
    void testRetrieveOrganisationSuccess() {
        UUID id = UUID.randomUUID();
        Organisation organisation = Organisation.createNew(id, "Alpha Corp", "Alpha", "11.222.333/0001-44", null);
        when(organisationRepository.findById(id)).thenReturn(Mono.just(organisation));

        StepVerifier.create(retrieveOrganisationUseCase.execute(id))
                .expectNextMatches(found -> found.id().equals(id) && found.corporateName().equals("Alpha Corp"))
                .verifyComplete();

        verify(organisationRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should throw OrganisationNotFoundException when organisation is not found")
    void testRetrieveOrganisationNotFound() {
        UUID id = UUID.randomUUID();
        when(organisationRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(retrieveOrganisationUseCase.execute(id))
                .expectError(OrganisationNotFoundException.class)
                .verify();

        verify(organisationRepository, times(1)).findById(id);
    }
}
