package com.hornetq.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
@XmlRootElement
class ClusterConfiguration {

    private List<ServerConfiguration> liveServeConfigurations = new ArrayList<>();
    private List<ServerConfiguration> backupServeConfigurations = new ArrayList<>();

    public List<ServerConfiguration> getLiveServeConfigurations() {
        return liveServeConfigurations;
    }

    public void setLiveServeConfigurations(List<ServerConfiguration> liveServeConfigurations) {
        this.liveServeConfigurations = liveServeConfigurations;
    }

    public List<ServerConfiguration> getBackupServeConfigurations() {
        return backupServeConfigurations;
    }

    public void setBackupServeConfigurations(List<ServerConfiguration> backupServeConfigurations) {
        this.backupServeConfigurations = backupServeConfigurations;
    }
}
