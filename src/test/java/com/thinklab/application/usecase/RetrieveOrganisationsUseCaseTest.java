package com.thinklab.application.usecase;

import com.thinklab.domain.model.Organisation;
import com.thinklab.domain.repository.OrganisationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
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
    @DisplayName("Should return a Flux of all organisation aggregates projected as responses")
    void testRetrieveOrganisationsSuccess() {
        Organisation o1 = Organisation.createNew(UUID.randomUUID(), "Organisation A", "A", "11.111.111/0001-11", null);
        Organisation o2 = Organisation.createNew(UUID.randomUUID(), "Organisation B", "B", "22.222.222/0001-22", null);

        when(organisationRepository.findAll()).thenReturn(Flux.just(o1, o2));

        StepVerifier.create(retrieveOrganisationsUseCase.execute())
                .expectNextMatches(res -> res.corporateName().equals("Organisation A"))
                .expectNextMatches(res -> res.corporateName().equals("Organisation B"))
                .verifyComplete();

        verify(organisationRepository, times(1)).findAll();
    }
}
