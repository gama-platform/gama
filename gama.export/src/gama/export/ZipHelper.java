package gama.export;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.*;
import java.io.IOException;
import java.util.stream.Stream;


public class ZipHelper {

    public static void zip(Path srcDirectory, Path dest) throws IOException
    {
        try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest.toFile()))){
    
            // Walk the appRootPath tree stream
            try (Stream<Path> stream = Files.walk(srcDirectory)) {
                stream.forEach(sourcePath -> {
                try {
                    if(! Files.isDirectory(sourcePath))
                    {
                        ZipEntry entry = new ZipEntry(srcDirectory.relativize(sourcePath).toString());
                        // Replace existing files/directories if needed
                        // Create a new entry inside the ZIP archive
                        zos.putNextEntry(entry);
                        
                        // Write bytes to the entry
                        Files.copy(sourcePath, zos);
                        zos.closeEntry();
                    }
                    
                    // Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy: " + sourcePath, e);
                }
            });
            } catch (RuntimeException e) {
                // Unwrap IOException from the stream loop
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }
        }
    } 

    public static void unzip(Path src, Path destDirectory) throws IOException {

        if (! Files.isDirectory(destDirectory)) {
            destDirectory.toFile().mkdirs();
        }
        
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(src.toString()))) {
            ZipEntry entry = zipIn.getNextEntry();
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
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
}
