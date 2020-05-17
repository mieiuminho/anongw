package anongw;

import anongw.server.AnonGW;
import anongw.util.Parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import java.util.List;

@SuppressWarnings("checkstyle:magicnumber")
public final class App {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    @Parameter(names = {"--target-server", "-ts"}, description = "Target server's address")
    private String targetServer;

    @Parameter(names = {"--port", "-p"}, description = "Port to use for the TCP connections")
    private int port = 8080;

    @Parameter(names = {"--overlay-peers", "-op"}, description = "List of anonymous gateways the anonymous gateway is"
            + " allowed to connect through UDP", variableArity = true)
    private List<String> peers;

    @Parameter(names = {"--udp"}, description = "Port to use for UDP connections")
    private int udp = 6666;

    @Parameter(names = {"--host", "-ip"}, description = "The IP of the AnonGW")
    private String ip;

    public static void main(final String[] args) {
        App application = new App();

        JCommander commands = JCommander.newBuilder().addObject(application).build();

        try {
            commands.parse(args);
        } catch (Exception e) {
            commands.usage();
            System.exit(1);
        }

        application.start();
    }

    public void start() {
        this.welcome();
        new AnonGW(this.targetServer, this.ip, this.port, this.udp, this.peers).startUp();
    }

    public void welcome() {
        for (String line : Parser.readFile("src/main/resources/art/logo.ascii")) {
            System.out.println(line);
        }
    }
}
