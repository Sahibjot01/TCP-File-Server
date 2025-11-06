import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileGenerator {

    private static final int NUM_FILES = 10; // Number of files to generate
    private static final int FILE_SIZE_MB = 2; // Approx size of each file in MB
    private static final String DIRECTORY = "./files/"; // Current directory, change if needed

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5); // 5 writer threads
        Random random = new Random();

        for (int i = 1; i <= NUM_FILES; i++) {
            final int fileId = i;
            executor.execute(() -> {
                String fileName = DIRECTORY + "file_" + fileId + ".txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

                    // Each line ~50 chars → ~20K lines for ~1 MB → write ~40K lines for ~2 MB
                    for (int line = 0; line < FILE_SIZE_MB * 20000; line++) {
                        // Generate random ASCII text
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < 50; j++) {
                            char c = (char) (random.nextInt(26) + 'a');
                            sb.append(c);
                        }
                        writer.write(sb.toString());
                        writer.newLine();
                    }

                    System.out.println("✅ Created: " + fileName + " by " + Thread.currentThread().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        System.out.println("Generating files...");
    }
}