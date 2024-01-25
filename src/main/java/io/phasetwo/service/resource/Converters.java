package io.phasetwo.service.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.model.jpa.entity.TeamEntity;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import io.phasetwo.service.representation.Team;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.representations.account.UserRepresentation;

/** Utilities for converting Entities to/from Representations. */
public class Converters {

  public static OrganizationRole convertOrganizationRole(OrganizationRoleModel m) {
    OrganizationRole r =
        new OrganizationRole().id(m.getId()).name(m.getName()).description(m.getDescription());
    return r;
  }

  public static Organization convertOrganizationModelToOrganization(OrganizationModel e) {
    Organization o =
        new Organization()
            .id(e.getId())
            .name(e.getName())
            .displayName(e.getDisplayName())
            .domains(e.getDomains())
            .url(e.getUrl())
            .realm(e.getRealm().getName());
    o.setAttributes(e.getAttributes());
    return o;
  }

  public static Team convertTeamEntityToTeam(TeamEntity e) {
    Team t = new Team().id(e.getId()).name(e.getName()).organizationId(e.getOrganization().getId());
    e.getAttributes().forEach(a -> t.attribute(a.getName(), a.getValue()));
    return t;
  }

  public static UserRepresentation convertUserEntityToUserRepresentation(UserEntity e) {
    UserRepresentation r = new UserRepresentation();
    r.setEmail(e.getEmail());
    r.setFirstName(e.getFirstName());
    r.setLastName(e.getLastName());
    r.setUsername(e.getUsername());
    r.setEmailVerified(e.isEmailVerified());
    r.setId(e.getId());
    Map<String, List<String>> attr = Maps.newHashMap();
    e.getAttributes()
        .forEach(
            a -> {
              List<String> l = attr.get(a.getName());
              if (l == null) l = Lists.newArrayList();
              if (!l.contains(a.getValue())) l.add(a.getValue());
              attr.put(a.getName(), l);
            });
    r.setAttributes(attr);
    return r;
  }

  public static Invitation convertInvitationEntityToInvitation(InvitationEntity e) {
    Invitation i =
        new Invitation()
            .id(e.getId())
            .email(e.getEmail())
            .createdAt(e.getCreatedAt())
            .inviterId(e.getInviterId())
            .organizationId(e.getOrganization().getId())
            .roles(Lists.newArrayList(e.getRoles()))
            .teamIds(e.getTeams().stream().map(t -> t.getId()).collect(Collectors.toList()));
    Map<String, List<String>> attr = Maps.newHashMap();
    e.getAttributes()
        .forEach(
            a -> {
              List<String> l = attr.get(a.getName());
              if (l == null) l = Lists.newArrayList();
              if (!l.contains(a.getValue())) l.add(a.getValue());
              attr.put(a.getName(), l);
            });
    i.setAttributes(attr);
    return i;
  }

  public static Invitation convertInvitationModelToInvitation(InvitationModel e) {
    Invitation i =
        new Invitation()
            .id(e.getId())
            .email(e.getEmail())
            .createdAt(e.getCreatedAt())
            .inviterId(e.getInviter().getId())
            .invitationUrl(e.getUrl())
            .organizationId(e.getOrganization().getId())
            .roles(Lists.newArrayList(e.getRoles()));
    i.setAttributes(Maps.newHashMap(e.getAttributes()));
    return i;
  }
}
