import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
// import java.lang.UnsupportedOperationException;

public class ZipExtractor {

    public static void main(String[] args)
    {
        boolean verbose = false;
        Path[] paths = new Path[2];
        
        int index = 0;
        for (String arg : args)
        {
            if (arg.equals("-v"))
                verbose = true;
            else
            {
                paths[index] = Path.of(arg);
                index++;
            }
        }

        for (String arg : args)
        {
            System.out.println(arg);
        }

        if (index != 2)
        {
            System.out.println("usage: extract [-v] <source_file> <destination_dir>");
            System.exit(0);
        }

        if (verbose)
            System.out.println("source : " + paths[0] + "\ntarget : " + paths[1] );

        if (!Files.exists(paths[0]))
        {
            System.out.println("error : the source zip file  '" + paths[0] + "' does not exist.");
            System.exit(1);
        }

        if (!Files.isDirectory(paths[1]))
        {
            System.out.println("error : the destination directory '" + paths[1] + "' does not exist.");
            System.exit(1);
        }

        try {
            unzip(paths[0],paths[1]);
        } catch (Exception e) {
            System.exit(1);
        }

        System.exit(0);
    }

    public static boolean setPermissions(Path filePath, int mode)
    {
        try {
            Set<PosixFilePermission> permissions = new HashSet<>();
            
            // Owner bits
            if ((mode & 0400) != 0) permissions.add(PosixFilePermission.OWNER_READ);
            if ((mode & 0200) != 0) permissions.add(PosixFilePermission.OWNER_WRITE);
            if ((mode & 0100) != 0) permissions.add(PosixFilePermission.OWNER_EXECUTE);
            
            // Group bits
            if ((mode & 0040) != 0) permissions.add(PosixFilePermission.GROUP_READ);
            if ((mode & 0020) != 0) permissions.add(PosixFilePermission.GROUP_WRITE);
            if ((mode & 0010) != 0) permissions.add(PosixFilePermission.GROUP_EXECUTE);
            
            // Others bits
            if ((mode & 0004) != 0) permissions.add(PosixFilePermission.OTHERS_READ);
            if ((mode & 0002) != 0) permissions.add(PosixFilePermission.OTHERS_WRITE);
            if ((mode & 0001) != 0) permissions.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(filePath, permissions);
            return true;

        } catch (Exception e) 
        {
            return false;
        }
    }

    public static void unzip(Path src, Path destDirectory) throws IOException {

        Files.createDirectories(destDirectory);
        Path extractionRoot = destDirectory.toAbsolutePath().normalize().toRealPath();
        
        // try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new FileInputStream(src.toString()))) {
        
        try(ZipFile archive = ZipFile.builder().setFile(src.toFile()).get()) {
            Enumeration<ZipArchiveEntry> entries = archive.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                Path filePath = resolveZipEntry(extractionRoot, entry.getName());
                if (!entry.isDirectory()) {
                    // Create parent directories if they don't exist
                    Files.createDirectories(filePath.getParent());
                    filePath = resolveZipEntry(extractionRoot, entry.getName());
                    // Extract the file
                    try (InputStream is = archive.getInputStream(entry);
                         BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath,
                                 StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                                 StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)))
                    {
                        // byte[] bytesIn = new byte[4096];
                        byte[] bytesIn = new byte[8192];
                        int read = 0;
                        while ((read = is.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                    }
                } else { // if filepath is a directory
                    Files.createDirectories(filePath);
                    resolveZipEntry(extractionRoot, entry.getName());
                }

                int mode = entry.getUnixMode();
                if (mode > 0)
                    setPermissions(filePath, mode);
                
            }
        }
    }

    private static Path resolveZipEntry(Path extractionRoot, String entryName) throws IOException {
        Path entryPath = Path.of(entryName.replace('\\', '/'));
        Path filePath = extractionRoot.resolve(entryPath).normalize();
        if (entryName.isBlank() || entryPath.isAbsolute() || filePath.equals(extractionRoot)
                || !filePath.startsWith(extractionRoot)) {
            throw new IOException("ZIP entry is outside the extraction directory: " + entryName);
        }

        Path current = extractionRoot;
        for (Path component : extractionRoot.relativize(filePath)) {
            current = current.resolve(component);
            if (Files.isSymbolicLink(current)
                    || (Files.exists(current, LinkOption.NOFOLLOW_LINKS)
                            && !current.toRealPath().startsWith(extractionRoot))) {
                throw new IOException("ZIP entry traverses outside the extraction directory: " + entryName);
            }
        }
        return filePath;
    }
}
