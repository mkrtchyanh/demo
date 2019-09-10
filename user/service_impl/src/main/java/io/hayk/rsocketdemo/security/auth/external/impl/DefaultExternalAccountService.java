package io.hayk.rsocketdemo.security.auth.external.impl;

import io.hayk.rsocketdemo.security.auth.external.ExternalAccount;
import io.hayk.rsocketdemo.security.auth.external.ExternalAccountProvider;
import io.hayk.rsocketdemo.impl.ExternalAccountService;
import io.hayk.rsocketdemo.security.auth.external.repository.ExternalAccountProviderRepository;
import io.hayk.rsocketdemo.security.auth.external.repository.ExternalAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
class DefaultExternalAccountService implements ExternalAccountService {

    private final ExternalAccountProviderRepository externalAccountProviderRepository;
    private final ExternalAccountRepository externalAccountRepository;

    DefaultExternalAccountService(final ExternalAccountProviderRepository externalAccountProviderRepository,
                                  final ExternalAccountRepository externalAccountRepository) {
        this.externalAccountProviderRepository = externalAccountProviderRepository;
        this.externalAccountRepository = externalAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalAccountProvider> lookupExternalAccountProvider(final String name) {
        assertValidProviderName(name);
        return externalAccountProviderRepository.findByName(name);
    }

    @Override
    @Transactional
    public ExternalAccountProvider registerExternalAccountProvider(final String name) {
        assertValidProviderName(name);
        return externalAccountProviderRepository.save(new ExternalAccountProvider(name));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalAccount> getExternalAccountByUidAndProviderName(final String externalAccountUid, final String providerName) {
        return externalAccountRepository.findByUidAndProviderName(externalAccountUid,providerName);
    }

    private void assertValidProviderName(final String name) {
        Assert.hasText(name, "Null or empty text was provided as an argument for parameter 'name'.");
    }
}
