package io.phasetwo.service.model.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@NamedQueries({
        @NamedQuery(
                name = "getConfigsByRealm",
                query =
                        "SELECT config FROM OrganizationsConfigEntity config WHERE config.realmId = :realmId"
        )
})
@Entity
@Table(name = "ORGANIZATIONS_CONFIG",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"REALM_ID"})})
public class OrganizationsConfigEntity {
    @Id
    @Column(name = "ID", length = 36)
    @Access(AccessType.PROPERTY)
    protected String id;

    @Column(name = "CREATE_ADMIN_USER")
    protected boolean createAdminUser;

    @Column(name = "SHARED_IDPS")
    protected boolean sharedIdps;

    @Column(name = "REALM_ID", nullable = false)
    protected String realmId;

    public boolean isCreateAdminUser() {
        return createAdminUser;
    }

    public void setCreateAdminUser(boolean createAdminUser) {
        this.createAdminUser = createAdminUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSharedIdps() {
        return sharedIdps;
    }

    public void setSharedIdps(boolean sharedIdps) {
        this.sharedIdps = sharedIdps;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationsConfigEntity that = (OrganizationsConfigEntity) o;
        return createAdminUser == that.createAdminUser && sharedIdps == that.sharedIdps && Objects.equals(id, that.id) && Objects.equals(realmId, that.realmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createAdminUser, sharedIdps, realmId);
    }
}
