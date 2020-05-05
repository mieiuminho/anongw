package anongw;

import anongw.server.AnonGW;
import anongw.util.Args;
import anongw.util.Parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;

public final class App {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    private App() {
    }

    public static void main(final String[] args) {
        Args arguments = new Args();

        JCommander commands = JCommander.newBuilder().addObject(arguments).build();

        try {
            commands.parse(args);
        } catch (Exception e) {
            commands.usage();
            System.exit(1);
        }

        new App().start(arguments);
    }

    public void start(final Args args) {
        this.welcome();
        new AnonGW(args.getTargetServer(), args.getIP(), args.getPort(), args.getUDP(), args.getPeers()).startUp();
    }

    public void welcome() {
        for (String line : Parser.readFile("src/main/resources/art/logo.ascii")) {
            System.out.println(line);
        }
    }
}
