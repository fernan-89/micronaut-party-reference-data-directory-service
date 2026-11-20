package com.thinklab.application.usecase;

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
class RetrieveOrganisationsUseCaseTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private RetrieveOrganisationsUseCase retrieveOrganisationsUseCase;

    @Test
    @DisplayName("Should return a Flux of the caller's own organisation projected as a response")
    void testRetrieveOrganisationsSuccess() {
        UUID tenant = UUID.randomUUID();
        Organisation own = Organisation.createNew(tenant, "Organisation A", "A", "11.111.111/0001-11", null);

        when(organisationRepository.findById(tenant)).thenReturn(Mono.just(own));

        StepVerifier.create(retrieveOrganisationsUseCase.execute(tenant))
                .expectNextMatches(res -> res.corporateName().equals("Organisation A"))
                .verifyComplete();

        verify(organisationRepository, times(1)).findById(tenant);
    }
}
