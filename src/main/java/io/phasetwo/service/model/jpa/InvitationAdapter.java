package io.phasetwo.service.model.jpa;

import com.google.common.collect.Sets;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.JpaModel;

public class InvitationAdapter implements InvitationModel, JpaModel<InvitationEntity> {

  protected final KeycloakSession session;
  protected final InvitationEntity invitation;
  protected final EntityManager em;
  protected final RealmModel realm;

  public InvitationAdapter(
      KeycloakSession session, RealmModel realm, EntityManager em, InvitationEntity invitation) {
    this.session = session;
    this.em = em;
    this.invitation = invitation;
    this.realm = realm;
  }

  @Override
  public InvitationEntity getEntity() {
    return invitation;
  }

  @Override
  public String getId() {
    return invitation.getId();
  }

  @Override
  public OrganizationModel getOrganization() {
    return session
        .getProvider(OrganizationProvider.class)
        .getOrganizationById(realm, invitation.getOrganization().getId());
  }

  @Override
  public String getEmail() {
    return invitation.getEmail();
  }

  @Override
  public void setEmail(String email) {
    invitation.setEmail(email);
  }

  @Override
  public String getUrl() {
    return invitation.getUrl();
  }

  @Override
  public void setUrl(String url) {
    invitation.setUrl(url);
  }

  @Override
  public UserModel getInviter() {
    if (invitation.getInviterId() != null) {
      return session.users().getUserById(realm, invitation.getInviterId());
    } else {
      return null;
    }
  }

  @Override
  public void setInviter(UserModel inviter) {
    invitation.setInviterId(inviter.getId());
  }

  @Override
  public Date getCreatedAt() {
    return invitation.getCreatedAt();
  }

  @Override
  public void setCreatedAt(Date date) {
    invitation.setCreatedAt(date);
  }

  @Override
  public Set<String> getRoles() {
    return invitation.getRoles();
  }

  @Override
  public void setRoles(Collection<String> roles) {
    invitation.setRoles(Sets.newHashSet(roles));
  }
}
