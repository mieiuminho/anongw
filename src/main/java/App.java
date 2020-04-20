import com.beust.jcommander.JCommander;
import java.util.List;
import server.AnonGW;
import util.Args;
import util.Parser;

public final class App {

    private App() {
    }

    public static void main(final String[] args) {

        Args arguments = new Args();

        JCommander.newBuilder().addObject(arguments).build().parse(args);

        new App().start(arguments.getTargetServerAddress(), arguments.getAnonPort(),
                arguments.getOverlayPeersAddresses());
    }

    public void start(final String targetServerAddress, final int anonPort, final List<String> overlayPeersAddresses) {
        this.welcome();
        new AnonGW(targetServerAddress, anonPort, overlayPeersAddresses).startUp();
    }

    public void welcome() {
        for (String line : Parser.readFile("src/main/resources/art/logo.ascii")) {
            System.out.println(line);
        }
    }
}
