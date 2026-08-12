package gama.export;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
// import java.util.zip.ZipArchiveEntry;
// import java.util.zip.ZipArchiveInputStream;
// import java.util.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URI;
import java.lang.UnsupportedOperationException;

public class ZipHelper {

    public static void zip(Path srcDirectory, Path dest) throws IOException
    {
        try(ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new FileOutputStream(dest.toFile()))){
    
            // Walk the appRootPath tree stream
            try (Stream<Path> stream = Files.walk(srcDirectory)) {
                stream.forEach(sourcePath -> {
                try {
                    if(! Files.isDirectory(sourcePath))
                    {
                        ZipArchiveEntry entry = new ZipArchiveEntry(srcDirectory.relativize(sourcePath).toString());
                        // Replace existing files/directories if needed
                        // Create a new entry inside the ZIP archive
                        zos.putArchiveEntry(entry);
                        
                        // Write bytes to the entry
                        Files.copy(sourcePath, zos);
                        zos.closeArchiveEntry();
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

    public static void transfertFilePermissions(Path targetFile, ZipArchiveEntry entry)
    {
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(targetFile);
            
            // Simpler alternative via converting Set<PosixFilePermission> to raw Unix bitfield octals:
            int unixMode = 0;

            for (PosixFilePermission perm : perms) {
                switch (perm) {
                    case PosixFilePermission.OWNER_READ -> unixMode |= 0400;
                    case PosixFilePermission.OWNER_WRITE -> unixMode |= 0200;
                    case PosixFilePermission.OWNER_EXECUTE -> unixMode |= 0100;
                    case PosixFilePermission.GROUP_READ -> unixMode |= 0040;
                    case PosixFilePermission.GROUP_WRITE -> unixMode |= 0020;
                    case PosixFilePermission.GROUP_EXECUTE -> unixMode |= 0010;
                    case PosixFilePermission.OTHERS_READ -> unixMode |= 0004;
                    case PosixFilePermission.OTHERS_WRITE -> unixMode |= 0002;
                    case PosixFilePermission.OTHERS_EXECUTE -> unixMode |= 0001;
                }
            }
            
            entry.setUnixMode(unixMode);
        } catch (IOException | UnsupportedOperationException e) {
            System.out.println("Error: could not transfert the permissions of " + targetFile);
            e.printStackTrace();
        }
    }


    public static void renameEntry(Path  zipPath, String sourceEntry, String targetEntry) {
        // Define the zip file system URI
        URI zipUri = URI.create("jar:" + zipPath.toUri().toString());
        Map<String, String> env = new HashMap<>();
        
        try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
            // Locate the target entry and define its new name
            Path sourcePath = zipfs.getPath(sourceEntry);
            Path targetPath = zipfs.getPath(targetEntry);
            
            // Execute the rename using an atomic move operation
            Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean removeEntry(Path  zipPath, String sourceEntry) {
        // Define the zip file system URI
        URI zipUri = URI.create("jar:" + zipPath.toUri().toString());
        Map<String, String> env = new HashMap<>();
        
        try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
            // Locate the target entry and define its new name
            Path sourcePath = zipfs.getPath(sourceEntry);
            
            // Execute the rename using an atomic move operation
            if(Files.exists(sourcePath))
            {
                Files.delete(sourcePath);
                return true;
            }

            return false;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
