package com.thinklab.application.usecase;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.repository.CompanyRepository;
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
class ListCompaniesUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ListCompaniesUseCase listCompaniesUseCase;

    @Test
    @DisplayName("Should return a Flux of all company aggregates")
    void testListCompaniesSuccess() {
        Company c1 = Company.createNew(UUID.randomUUID(), "Company A", "A", "11.111.111/0001-11", null);
        Company c2 = Company.createNew(UUID.randomUUID(), "Company B", "B", "22.222.222/0001-22", null);

        when(companyRepository.findAll()).thenReturn(Flux.just(c1, c2));

        StepVerifier.create(listCompaniesUseCase.execute())
                .expectNext(c1)
                .expectNext(c2)
                .verifyComplete();

        verify(companyRepository, times(1)).findAll();
    }
}
