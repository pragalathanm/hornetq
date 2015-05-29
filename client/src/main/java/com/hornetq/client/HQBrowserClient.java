package com.hornetq.client;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class HQBrowserClient {

    private WebTarget target;
    private Client client;
    private static final Logger logger = Logger.getLogger(HQBrowserClient.class.getName());
    private static final String PATH = "/service/hornetq/configuration";

    public HQBrowserClient() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream("/hornetq.properties"));
        this.client = ClientBuilder.newClient();
        String url = properties.getProperty("hqbrowser.url");
        target = client.target(url);
    }

    public List<ServerConfiguration> getConfiguration() {
        Response response = target.path(PATH).request(MediaType.APPLICATION_JSON).get();
        List<ServerConfiguration> configurations = response.readEntity(ClusterConfiguration.class).getLiveServeConfigurations();
        if (response.getStatus() != 200) {
            throw new RuntimeException("Status:" + response.getStatus() + " received when contacting hqbrowser for configuration");
        }
        return configurations;
    }
}
