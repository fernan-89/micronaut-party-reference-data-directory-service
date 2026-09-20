package com.thinklab.application.usecase;

import com.thinklab.domain.model.Company;
import com.thinklab.domain.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCompanyByIdUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private GetCompanyByIdUseCase getCompanyByIdUseCase;

    @Test
    @DisplayName("Should return company when ID exists")
    void testGetCompanyByIdSuccess() {
        UUID id = UUID.randomUUID();
        Company company = Company.createNew(id, "Alpha Corp", "Alpha", "11.222.333/0001-44", null);
        when(companyRepository.findById(id)).thenReturn(Mono.just(company));

        StepVerifier.create(getCompanyByIdUseCase.execute(id))
                .expectNextMatches(found -> found.getId().equals(id) && found.getCorporateName().equals("Alpha Corp"))
                .verifyComplete();

        verify(companyRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when company is not found")
    void testGetCompanyByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(getCompanyByIdUseCase.execute(id))
                .expectError(NoSuchElementException.class)
                .verify();

        verify(companyRepository, times(1)).findById(id);
    }
}
