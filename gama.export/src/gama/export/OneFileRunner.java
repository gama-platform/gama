import java.io.InputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URI;

public class OneFileRunner {

    // private static String nativeImageLocation = Path.of(System.getProperty("java.home"),"bin","native-image").toString();
    // private static final Path tmpDirectoryPath = Path.of(System.getProperty("java.io.tmpdir"),"gama.simulation.launcher.tmp");
    private static final Path tmpDirectoryPath = Path.of("/home/cytech/gama.simulation.launcher.tmp");

    public static void unzip(Path src, Path destDirectory) throws IOException {

        if (! Files.isDirectory(destDirectory)) {
            destDirectory.toFile().mkdirs();
        }
        
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new FileInputStream(src.toString()))) {
            ZipArchiveEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                File filePath = new File(destDirectory.toString(), entry.getName());
                if (!entry.isDirectory()) {
                    // Create parent directories if they don't exist
                    filePath.getParentFile().mkdirs();
                    // Extract the file
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
                        byte[] bytesIn = new byte[4096];
                        int read = 0;
                        while ((read = zipIn.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                    }
                } else { // if filepath is a directory
                    filePath.mkdirs();
                }
                // zipIn.closeArchiveEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
    
    public static void main(String[] args)
    {
        try (InputStream is = OneFileRunner.class.getResourceAsStream("/home/cytech/tmp/launcher.zip")) {
            if (is == null) throw new RuntimeException("Embedded ZIP GAMA launcher not found !");
            Files.copy(is,tmpDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    //     ProcessBuilder pb = new ProcessBuilder(
    //     jdepsLocation,
    //     "--recursive",
    //     jarPath.toAbsolutePath().toString());

    //     try {

    //         Process process = pb.start();

    //         try (BufferedReader reader =
    //                 new BufferedReader(
    //                         new InputStreamReader(process.getInputStream()))) {

    //             String line;

    //             while ((line = reader.readLine()) != null) {
    //                 int arrow = line.indexOf("->");

    //                 if (arrow < 0) {
    //                     continue;
    //                 }

    //                 String dependency =
    //                         line.substring(arrow + 2).trim();


    //                 int firstSpace = dependency.indexOf(' ');

    //                 if (firstSpace > 0) {
    //                     dependency =
    //                             dependency.substring(0, firstSpace);
    //                 }

    //                 if (!dependency.isBlank()) {
    //                     result.add(dependency);
    //                 }
    //             }
    //         }

    //         process.waitFor();

    //     } catch (Exception e) {
    //         throw new RuntimeException(
    //                 "Failed to start GAMA Simulation launcher.", e);
    //     }
    }
}