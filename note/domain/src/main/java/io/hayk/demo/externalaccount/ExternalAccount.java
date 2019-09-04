package io.hayk.demo.externalaccount;

import io.hayk.demo.AbstractDomainEntity;
import io.hayk.demo.user.User;

import javax.persistence.*;

@Entity
@Table(name = "external_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_external_account_owner_provider",
                        columnNames = {"owner_id", "provider_id"}),
                @UniqueConstraint(name = "UQ_external_account_uid_provider_id",
                        columnNames = {"uid", "provider_id"})
        })
@Access(AccessType.FIELD)
public class ExternalAccount extends AbstractDomainEntity {

    @Column(name = "uid", nullable = false)
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "owner_id", updatable = false, nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "provider_id", updatable = false, nullable = false)
    private ExternalAccountProvider provider;

    /**
     * Accessible by reflection.
     * <p>
     * Will be used by frameworks depending on default constructor
     */
    ExternalAccount() {
        super();
    }

    public ExternalAccount(final String uid, final User owner, final ExternalAccountProvider provider) {
        this.owner = owner;
        this.provider = provider;
        this.uid = uid;
    }

    /**
     * Checks the identity of external account
     */
    public boolean isSameExternalAccount(final String uid, final String providerName) {
        return uid.equals(getUid()) && providerName.equals(getProvider().getName());
    }

    public String getUid() {
        return uid;
    }

    public User getOwner() {
        return owner;
    }

    public ExternalAccountProvider getProvider() {
        return provider;
    }
}
