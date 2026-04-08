package io.phasetwo.web;

import static io.phasetwo.service.Helpers.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTest;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestSuite;
import io.phasetwo.service.resource.OrganizationResourceProviderFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

@JBossLog
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
public class AbstractCypressOrganizationTest {

  protected static final boolean RUN_CYPRESS =
      Boolean.parseBoolean(System.getProperty("include.cypress", "false"));

  public static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "26.5.7"));
  public static final String REALM = "master";
  public static final String ADMIN_CLI = "admin-cli";

  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client",
    "io.phasetwo.keycloak:keycloak-events"
  };

  static List<File> getDeps() {
    List<File> dependencies = new ArrayList<File>();
    for (String dep : deps) {
      dependencies.addAll(getDep(dep));
    }
    return dependencies;
  }

  static List<File> getDep(String pkg) {
    return Maven.resolver()
        .loadPomFromFile("./pom.xml")
        .resolve(pkg)
        .withoutTransitivity()
        .asList(File.class);
  }

  public static Keycloak keycloak;
  public static ResteasyClient resteasyClient;

  public static final KeycloakContainer container = initKeycloakContainer();

  private static KeycloakContainer initKeycloakContainer() {
    KeycloakContainer keycloakContainer = new KeycloakContainer(KEYCLOAK_IMAGE)
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withContextPath("/auth")
            .withReuse(true)
            .withProviderClassesFrom("target/classes")
            .withProviderLibsFrom(getDeps())
            .withAccessToHost(true);
    if (isJacocoPresent()) {
      keycloakContainer = keycloakContainer.withCopyFileToContainer(
                      MountableFile.forHostPath("target/jacoco-agent/"),
                      "/jacoco-agent"
              )
              .withEnv("JAVA_OPTS", "-XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -javaagent:/jacoco-agent/org.jacoco.agent-runtime.jar=destfile=/tmp/jacoco.exec");
    } else {
      keycloakContainer = keycloakContainer.withEnv("JAVA_OPTS", "-XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m");
    }

    return keycloakContainer;
  }

  private static boolean isJacocoPresent() {
    return Files.exists(Path.of("target/jacoco-agent/org.jacoco.agent-runtime.jar"));
  }

  protected static final int WEBHOOK_SERVER_PORT = 8083;

  @AfterAll
  public static void tearDown() throws IOException {
    String containerId = container.getContainerId();
    String containerShortId;
    if (containerId.length() > 12) {
      containerShortId = containerId.substring(0, 12);
    } else {
      containerShortId = containerId;
    }
    container.getDockerClient().stopContainerCmd(containerId).exec();
    if (isJacocoPresent()) {
      Files.createDirectories(Path.of("target", "jacoco-report"));
      container.copyFileFromContainer("/tmp/jacoco.exec", "./target/jacoco-report/jacoco-%s.exec".formatted(containerShortId));
    }
    container.stop();
  }

  @BeforeAll
  public static void beforeAll() {
    container.start();

    Testcontainers.exposeHostPorts(WEBHOOK_SERVER_PORT);
    resteasyClient =
        new ResteasyClientBuilderImpl()
            .disableTrustManager()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
    enableEvents(keycloak, "master");
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return Keycloak.getInstance(getAuthUrl(), realm, user, pass, clientId);
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  protected Response getRequest(String... paths) {
    return getRequest(keycloak, String.join("/", paths));
  }

  protected Response getRequest(Keycloak keycloak, String path) {
    return givenSpec(keycloak).when().get(path).andReturn();
  }

  protected Response putRequest(Object body, String... paths) throws JsonProcessingException {
    return putRequest(keycloak, body, String.join("/", paths));
  }

  protected Response putRequest(Keycloak keycloak, Object body, String path)
      throws JsonProcessingException {
    return givenSpec(keycloak).and().body(toJsonString(body)).put(path).then().extract().response();
  }

  protected Response postRequest(Object body, String... paths) throws JsonProcessingException {
    return postRequest(keycloak, body, String.join("/", paths));
  }

  protected Response postRequest(Keycloak keycloak, Object body, String path)
          throws JsonProcessingException {
    return givenSpec(keycloak)
            .and()
            .body(toJsonString(body))
            .post(path)
            .then()
            .extract()
            .response();
  }

  protected RequestSpecification givenSpec(Keycloak keycloak, String... paths) {
    if (paths.length > 0) {
      return given()
          .baseUri(container.getAuthServerUrl())
          .basePath("realms/" + REALM + "/" + String.join("/", paths))
          .contentType("application/json")
          .auth()
          .oauth2(keycloak.tokenManager().getAccessTokenString());
    }
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("realms/" + REALM + "/" + OrganizationResourceProviderFactory.ID)
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString());
  }

  protected void importRealm(RealmRepresentation representation, Keycloak keycloak) {
    var response =
            given()
                    .baseUri(container.getAuthServerUrl())
                    .basePath("admin/realms/")
                    .contentType("application/json")
                    .auth()
                    .oauth2(keycloak.tokenManager().getAccessTokenString())
                    .and()
                    .body(representation)
                    .when()
                    .post()
                    .then()
                    .extract()
                    .response();
    assertThat(response.getStatusCode(), CoreMatchers.is(Status.CREATED.getStatusCode()));
  }

  List<DynamicContainer> convertToJUnitDynamicTests(CypressTestResults testResults) {
    return convertToJUnitDynamicTests("", testResults);
  }

  List<DynamicContainer> convertToJUnitDynamicTests(String namePrefix, CypressTestResults testResults) {
        List<DynamicContainer> dynamicContainers = new ArrayList<>();
        List<CypressTestSuite> suites = testResults.getSuites();
        for (CypressTestSuite suite : suites) {
            createContainerFromSuite(namePrefix, dynamicContainers, suite);
        }
        return dynamicContainers;
    }

    void createContainerFromSuite(String namePrefix, List<DynamicContainer> dynamicContainers, CypressTestSuite suite) {
        List<DynamicTest> dynamicTests = new ArrayList<>();
        for (CypressTest test : suite.getTests()) {
            dynamicTests.add(
                    DynamicTest.dynamicTest(
                            test.getDescription(),
                            () -> {
                                if (!test.isSuccess()) {
                                    log.error(test.getErrorMessage());
                                    log.error(test.getStackTrace());
                                }
                                Assertions.assertTrue(test.isSuccess());
                            }));
        }
        dynamicContainers.add(DynamicContainer.dynamicContainer(namePrefix + suite.getTitle(), dynamicTests));
    }

    protected RealmRepresentation importRealm(String jsonRepresentationPath, @Nullable String realmOverride) {
        RealmRepresentation realm =
                loadJson(getClass().getResourceAsStream(jsonRepresentationPath),
                        RealmRepresentation.class);
        if (realmOverride != null) {
            realm.setRealm(realmOverride);
        }
        importRealm(realm, keycloak);
        log.info("realm imported successfully:" + realm.getRealm());
        return realm;
    }

    protected static RealmResource findRealmByName(String realm) {
        return keycloak
                .realms()
                .realm(realm);
    }
}
