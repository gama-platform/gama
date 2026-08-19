package gama.export;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.AutoCloseable;


import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import gama.api.runtime.SystemInfo;
import gama.export.ZipHelper;

/**
 * this class is a facade that creates the appropriate class to build a Zip/SevenZip file depending on the context,
 * and hides 'low level' zip entries gymastics under a higher abstraction method
 */
public class ArchiveBuilder implements AutoCloseable {

    private static boolean isWindows = false;

    public ZipArchiveOutputStream zipArchiveOutputStream = null;

    public SevenZOutputFile sevenZOutputFile = null;

    @FunctionalInterface
    private interface iAddEntryFunction {
        public void apply(Path targetFilePath, String entryName) throws IOException;
    }

    @FunctionalInterface
    private interface iAddEntryFromStringFunction {
        public void apply(String stringContent, String entryName) throws IOException;
    }

    private iAddEntryFunction addEntryProxy;

    private iAddEntryFromStringFunction addEntryFromStringProxy;

    /**
     * Add an archive entry to the appropriate archive type, 
     * depending on the context, from a file
     * 
     * @param targetFilePath
     *                Patj of the file to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    public void addEntry(Path targetFilePath, String entryName) throws IOException {
        this.addEntryProxy.apply(targetFilePath,entryName);
    }

    /**
     * Add an archive entry to the appropriate archive type, 
     * depending on the context, from a String value
     * 
     * @param stringContent
     *                String to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    public void addEntryFromString(String stringContent, String entryName) throws IOException {
        this.addEntryFromStringProxy.apply(stringContent, entryName);
    }


    /**
     * Add a ZipEntry to the ZipArchiveOutputStream from a file
     * 
     * @param targetFilePath
     *                Path of the file to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    private void addZipEntry(Path targetFilePath, String entryName) throws IOException
    {
        if (Files.isDirectory(targetFilePath))
            return;

        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        
        if (! this.isWindows)
                ZipHelper.transfertFilePermissions(targetFilePath,entry);
        
        zipArchiveOutputStream.putArchiveEntry(entry);
        
        // Write bytes to the entry
        Files.copy(targetFilePath, zipArchiveOutputStream);
        
        zipArchiveOutputStream.closeArchiveEntry();
    }

    /**
     * Add a SevenZArchiveEntry to the ZipArchiveOutputStream from a file
     * 
     * @param targetFilePath
     *                Path of the file to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    private void addSevenZEntry(Path targetFilePath, String entryName) throws IOException
    {
        // Create an entry with the target internal filename
        SevenZArchiveEntry entry = sevenZOutputFile.createArchiveEntry(targetFilePath.toFile(), entryName);
        sevenZOutputFile.putArchiveEntry(entry);
        
        if (! Files.isDirectory(targetFilePath))
        {
            // Read source file bytes and write directly to the archive
            byte[] data = Files.readAllBytes(targetFilePath);
            sevenZOutputFile.write(data);
        }
        
        // Finalize entry lifecycle
        sevenZOutputFile.closeArchiveEntry();
    }

    /**
     * Add a SevenZArchiveEntry to the SevenZOutputFile from a String value
     * 
     * @param stringContent
     *                String to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    private void addSevenZEntryFromString(String stringContent, String entryName) throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        byte[] data = stringContent.getBytes(StandardCharsets.UTF_8);

        entry.setName(entryName);
        entry.setSize(data.length);

        sevenZOutputFile.putArchiveEntry(entry);
        sevenZOutputFile.write(data);
        sevenZOutputFile.closeArchiveEntry();
    }

    /**
     * Add a ZipArchiveEntry to the ZipArchiveOutputStream from a String value
     * 
     * @param stringContent
     *                String to be copied in the entry
     * 
     * @param entryName
     *                actual String representing the entry, which will
     *                correspond to the path where the target file 
     *                will be extracted
     */
    private void addZipEntryFromString(String stringContent, String entryName) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        byte[] data = stringContent.getBytes(StandardCharsets.UTF_8);
        
        zipArchiveOutputStream.putArchiveEntry(entry);
        zipArchiveOutputStream.write(data);
        zipArchiveOutputStream.closeArchiveEntry();
    }

    /**
     * ArchiveBuilder constructor
     * 
     * @param archiveOutputPath
     *                        desired Path of the created archive file
     * 
     * @param selfExtractingArchive
     *                        boolean to set to true if the archive is meant
     *                        to be a self extracting archive 
     */
    public ArchiveBuilder(Path archiveOutputPath, boolean selfExtractingArchive) throws IOException
    {
        this.isWindows = SystemInfo.isWindows();

        if (this.isWindows && selfExtractingArchive)
        {
            sevenZOutputFile = new SevenZOutputFile(archiveOutputPath.toFile());
            this.addEntryProxy = this::addSevenZEntry;
            this.addEntryFromStringProxy = this::addSevenZEntryFromString;
        } 
        else
        {
            zipArchiveOutputStream = new ZipArchiveOutputStream(new FileOutputStream(archiveOutputPath.toFile()));
            this.addEntryProxy = this::addZipEntry;
            this.addEntryFromStringProxy = this::addZipEntryFromString;
        }
    }

    public void close() throws IOException
    {
        if (zipArchiveOutputStream != null)
            zipArchiveOutputStream.close();

        if (sevenZOutputFile != null)
            sevenZOutputFile.close();
    }
}