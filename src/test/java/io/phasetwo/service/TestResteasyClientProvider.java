package io.phasetwo.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.admin.client.ClientBuilderWrapper;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.spi.ResteasyClientProvider;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@AutoService(ResteasyClientProvider.class)
public class TestResteasyClientProvider implements ResteasyClientProvider {
  @Override
  public Client newRestEasyClient(Object customJacksonProvider, SSLContext sslContext, boolean disableTrustManager) {
    ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    JacksonProvider resteasyJacksonProvider = new JacksonProvider();
    resteasyJacksonProvider.setMapper(mapper);

    ClientBuilder clientBuilder = ClientBuilderWrapper.create(sslContext, disableTrustManager)
            .register(resteasyJacksonProvider, 100);

    return clientBuilder.build();
  }

  @Override
  public <R> R targetProxy(WebTarget client, Class<R> targetClass) {
    return ResteasyWebTarget.class.cast(client).proxy(targetClass);
  }
}
