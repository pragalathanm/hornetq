package com.hornetq.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
class ServerConfiguration {

    private final String host;
    @XmlElement(name = "jgroupsPort")
    private final String port;

    ServerConfiguration() {
        this("localhost", "5445");
    }

    public ServerConfiguration(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}
