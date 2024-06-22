import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Path path = Path.of("./index.js");
        Thread thread = new Thread(new Context(path));
        thread.start();
    }
}
