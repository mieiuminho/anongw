package anongw.util;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("checkstyle:magicnumber")
public final class Args {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--target-server", "-ts"}, description = "Target server's address")
    private String targetServer;

    public String getTargetServer() {
        return this.targetServer;
    }

    @Parameter(names = {"--port", "-p"}, description = "Port to use for the TCP connections")
    private int port = 8080;

    public int getPort() {
        return this.port;
    }

    @Parameter(names = {"--overlay-peers", "-op"}, description = "List of anonymous gateways the anonymous gateway is"
            + " allowed to connect through UDP", variableArity = true)
    private List<String> peers;

    public List<String> getPeers() {
        return this.peers;
    }

    @Parameter(names = {"--udp"}, description = "Port to use for UDP connections")
    private int udp = 6666;

    public int getUDP() {
        return this.udp;
    }

    @Parameter(names = {"--host", "-ip"}, description = "The IP of the AnonGW")
    private String ip;

    public String getIP() {
        return this.ip;
    }
}
