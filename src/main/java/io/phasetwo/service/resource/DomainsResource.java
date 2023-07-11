package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import io.phasetwo.service.model.DomainModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Domain;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

@JBossLog
public class DomainsResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public DomainsResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Domain> getDomains() {
    log.debugf("Get domains for %s %s", realm.getName(), organization.getId());
    return organization.getDomains().stream().map(s -> lookupDomain(s)).map(d -> fromModel(d));
  }

  @GET
  @Path("{domainName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Domain getDomain(@PathParam("domainName") String domainName) {
    log.debugf("Get domain for %s %s %s", domainName, realm.getName(), organization.getId());
    return fromModel(lookupDomain(domainName));
  }

  private Domain fromModel(DomainModel d) {
    return new Domain()
        .domainName(d.getDomain())
        .verified(d.isVerified())
        .recordKey(RECORD_KEY)
        .recordValue(getRecordValue(d.getDomain()));
  }

  private static final String RECORD_KEY = "_org-domain-ownership";

  private String getRecordKey(String domainName) {
    return String.format("%s.%s", RECORD_KEY, domainName);
  }

  private String getRecordValue(String domainName) {
    return Hashing.sha256()
        .hashString(
            String.format("%s %s", domainName, organization.getId()), StandardCharsets.UTF_8)
        .toString();
  }

  private DomainModel lookupDomain(String domainName) {
    DomainModel d = organization.getDomain(domainName);
    if (d == null) {
      throw new NotFoundException(
          String.format("%s not a domain in %s", domainName, organization.getId()));
    }
    return d;
  }

  private void startVerification(String domainName) {
    String valueToCompare = getRecordValue(domainName);
    try {
      Lookup lookup = new Lookup(getRecordKey(domainName), Type.TXT);
      lookup.setResolver(new SimpleResolver());
      lookup.setCache(null);
      Record[] records = lookup.run();
      if (lookup.getResult() == Lookup.SUCCESSFUL) {
        StringBuilder builder = new StringBuilder();
        for (Record record : records) {
          final TXTRecord txt = (TXTRecord) record;
          builder.delete(0, builder.length());
          String text = Joiner.on("").join(txt.getStrings().iterator());
          log.infof("found record for %s = %s", getRecordKey(domainName), text);
          if (valueToCompare.equals(text)) {
            log.infof("verified domain %s using %s", domainName, valueToCompare);
            lookupDomain(domainName).setVerified(true);
            break;
          }
        }
      }
    } catch (Exception e) {
      log.warn("Error during DNS verification", e);
    }
  }

  @POST
  @Path("{domainName}/verify")
  @Produces(MediaType.APPLICATION_JSON)
  public Response verifyDomain(@PathParam("domainName") String domainName) {
    log.infof("verifyDomain %s %s", domainName, organization.getId());
    if (auth.hasManageOrgs() || auth.hasOrgManageOrg(organization)) {
      log.infof("startVerification %s %s", domainName, organization.getId());
      startVerification(lookupDomain(domainName).getDomain());
      DomainModel d = lookupDomain(domainName);
      Domain domain = fromModel(d);
      log.infof("endVerification %s %s %s", domainName, organization.getId(), domain);
      return Response.accepted().entity(domain).build();
    } else {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to validate domains for %s", organization.getId()));
    }
  }
}
