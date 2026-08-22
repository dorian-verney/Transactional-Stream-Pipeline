
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

 void main(String[] args) throws InterruptedException {

    List.of("Producer.log", "Consumers.log").forEach(filename -> {
        try {
            Files.deleteIfExists(Path.of(System.getProperty("user.dir") + "/monitoring/" + filename));
        } catch (IOException e) {
            System.err.println("Failed to delete " + filename + ": " + e.getMessage());
        }
    });

    Pipeline pipeline = new Pipeline();
    pipeline.launchPipeline();

}
