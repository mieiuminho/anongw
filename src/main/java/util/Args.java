package util;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public final class Args {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--target-server", "-ts"}, description = "Target server's adress")
    private String targetServerAddress;

    @Parameter(names = {"--port", "-p"}, description = "Anonymous gateway port")
    private int anonPort;

    @Parameter(names = {"--overlay-peers", "-op"}, description = "List of anonymous gateways the anonymous gateway is"
            + " allows to connect through UDP", variableArity = true)
    private List<String> overlayPeersAdresses;

    public String getTargetServerAddress() {
        return this.targetServerAddress;
    }

    public int getAnonPort() {
        return this.anonPort;
    }

    public List<String> getOverlayPeersAddresses() {
        return this.overlayPeersAdresses;
    }

}
