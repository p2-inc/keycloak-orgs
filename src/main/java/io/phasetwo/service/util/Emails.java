package io.phasetwo.service.util;

import com.google.common.base.Strings;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class Emails {

  public static void sendEmail(
      String templateName,
      String subjectKey,
      List<Object> subjectAttributes,
      Map<String, Object> bodyAttributes,
      KeycloakSession session,
      RealmModel realm,
      UserModel user)
      throws EmailException {
    EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);

    String realmName =
        Strings.isNullOrEmpty(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
    bodyAttributes.put("realmName", realmName);
    bodyAttributes.put("user", new ProfileBean(user));

    emailTemplateProvider
        .setRealm(realm)
        .setUser(user)
        .setAttribute("realmName", realmName)
        .send(subjectKey, subjectAttributes, templateName, bodyAttributes);
  }

  /** Get a validated email address */
  public static InternetAddress getValidEmail(String email) throws AddressException {
    Objects.requireNonNull(email, "email must not be null to validate");
    try {
      if (email.startsWith("mailto:")) email = email.substring(7);
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return emailAddr;
    } catch (AddressException e) {
      throw e;
    }
  }

  public static boolean isValidEmail(String email) {
    try {
      getValidEmail(email);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
