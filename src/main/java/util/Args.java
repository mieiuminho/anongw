package util;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

public final class Args {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--target-server", "-ts"}, description = "Target server's address")
    private String targetServerAddress;

    @Parameter(names = {"--port", "-p"}, description = "Port to use for the connection")
    private int port;

    @Parameter(names = {"--overlay-peers", "-op"}, description = "List of anonymous gateways the anonymous gateway is"
            + " allowed to connect through UDP", variableArity = true)
    private List<String> overlayPeersAddresses;

    public String getTargetServerAddress() {
        return this.targetServerAddress;
    }

    public int getAnonPort() {
        return this.port;
    }

    public List<String> getOverlayPeersAddresses() {
        return this.overlayPeersAddresses;
    }
}
