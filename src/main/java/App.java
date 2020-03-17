import server.AnonGW;
import util.Parser;

public final class App {
    private App() {

    }

    public static void main(final String[] args) {
        new App().start();
    }

    public void start() {
        this.welcome();
        new AnonGW().startUp();
    }

    public void welcome() {
        for(String line: Parser.readFile("src/main/resources/art/logo.ascii")) {
            System.out.println(line);
        }
    }
}
