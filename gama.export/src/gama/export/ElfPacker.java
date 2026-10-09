package gama.export;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ElfPacker {

        private static final int ELF64_HEADER_SIZE = 64;

        private static final int ELF64_PROGRAM_HEADER_SIZE = 56;

        // ELF header offsets
        private static final int E_PH_OFF  = 32; // e_phoff

        private static final int E_PH_NUM  = 56; // e_phnum

        // ELF64 program header offsets
        private static final int PH_TYPE   = 0;
        
        private static final int PH_FILESZ = 32;
        
        private static final int PH_MEMSZ  = 40;

        private static final int PT_LOAD = 1;

        private static final String DATA_START_SYMBOL = "__DATA_START__";

        private static final int META_DATA_STRING_SIZE = 1024;
        
        private static final int META_DATA_SIZE = META_DATA_STRING_SIZE*5 + 8*3;

        private static final Path TMP_PATH = Path.of(System.getProperty("java.io.tmpdir"));
        // private static final Path TMP_PATH = Path.of(".");

        private ElfPacker() {}

        public static void pack(
                Path runnerPath, 
                Path extractorPath, 
                Path zipPayloadPath, 
                Path outputPath
                ) throws Exception {

                concatenate(runnerPath, extractorPath, zipPayloadPath, outputPath);


                final long runnerSize = java.nio.file.Files.size(runnerPath);
                final long extractorSize = java.nio.file.Files.size(extractorPath);
                final long zipPayloadSize = java.nio.file.Files.size(zipPayloadPath);
                final long appendedSize = extractorSize + zipPayloadSize;
                
                final long metaDataOffset = getMetaDataOffset(runnerPath);


                // Increase the first PT_LOAD segment's p_filesz/p_memsz
                // by the amount of data appended to the original ELF.
                updateLoadSegmentSizes(outputPath, appendedSize);

                // update the size of the extractor and zip payload in the runner's embedded data,
                // allowing him to later write the correct amount of bytes on the disk
                updateDataSizes(outputPath, metaDataOffset, runnerSize, extractorSize, zipPayloadSize);

                makeExecutable(outputPath);

                System.out.println("Created " + outputPath);
        }

        /**
         * Equivalent to:
         *
         * cat build/runner extract launcher.zip > build/out
         */
        public static void concatenate(
                Path runnerPath,
                Path extractorPath,
                Path zipPayloadPath,
                Path outputPath
                ) throws IOException {

                try (OutputStream out = Files.newOutputStream(
                        outputPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                )) {
                        Files.copy(runnerPath, out);
                        Files.copy(extractorPath, out);
                        Files.copy(zipPayloadPath, out);
                }
        }

        /**
         * Finds PT_LOAD program headers and increases p_filesz and p_memsz.
         * For an appended payload, the operation is:
         *     p_filesz += appendedSize
         *     p_memsz  += appendedSize
         * 
         * @param elf
         *          Path of the elf64 file to patch
         * @param appendedSize
         *                   Number of bytes appended to the file elf
         */
        public static void updateLoadSegmentSizes(
                Path elf,
                long appendedSize
                ) throws IOException {

                if (appendedSize < 0)
                        throw new IllegalArgumentException("Negative appended size");

                try (FileChannel channel = FileChannel.open(
                        elf,
                        StandardOpenOption.READ,
                        StandardOpenOption.WRITE))
                {
                        ByteBuffer elfHeader = ByteBuffer.allocate(ELF64_HEADER_SIZE)
                                .order(ByteOrder.LITTLE_ENDIAN);

                        readFully(channel, elfHeader, 0);
                        elfHeader.flip();

                        long phoff = elfHeader.getLong(E_PH_OFF);
                        int phnum = Short.toUnsignedInt(elfHeader.getShort(E_PH_NUM));

                        if (phoff < 0)
                                throw new IOException("Invalid e_phoff");

                        for (int i = 0; i < phnum; i++) {
                                long phOffset =
                                        phoff + (long) i * ELF64_PROGRAM_HEADER_SIZE;

                                ByteBuffer ph = ByteBuffer.allocate(
                                        ELF64_PROGRAM_HEADER_SIZE
                                ).order(ByteOrder.LITTLE_ENDIAN);

                                readFully(channel, ph, phOffset);
                                ph.flip();

                                int type = ph.getInt(PH_TYPE);

                                if (type != PT_LOAD) {
                                        continue;
                                }

                                long filesz = ph.getLong(PH_FILESZ);
                                long memsz = ph.getLong(PH_MEMSZ);

                                long newFilesz = Math.addExact(filesz, appendedSize);
                                long newMemsz = Math.addExact(memsz, appendedSize);

                                ByteBuffer sizes = ByteBuffer.allocate(16)
                                        .order(ByteOrder.LITTLE_ENDIAN);

                                sizes.putLong(newFilesz);
                                sizes.putLong(newMemsz);
                                sizes.flip();

                                channel.write(
                                        sizes,
                                        phOffset + PH_FILESZ
                                );

                                System.out.printf(
                                        "PT_LOAD #%d: p_filesz %d -> %d, "
                                        + "p_memsz %d -> %d%n",
                                        i,
                                        filesz,
                                        newFilesz,
                                        memsz,
                                        newMemsz
                                );
                        }
                }
        }

        public static void updateDataSizes(
                Path elf,
                long metaDataOffset,
                long runnerSize,
                long extractorSize,
                long zipPayloadSize
                ) throws IOException {

                if (runnerSize < 0)
                        throw new IllegalArgumentException("Negative runner size");

                if (extractorSize < 0)
                        throw new IllegalArgumentException("Negative extractor size");

                if (zipPayloadSize < 0)
                        throw new IllegalArgumentException("Negative zip size");

                try (FileChannel channel = FileChannel.open(
                        elf,
                        StandardOpenOption.WRITE)) 
                {
                        ByteBuffer metaData = ByteBuffer.allocate(META_DATA_SIZE)
                                .order(ByteOrder.LITTLE_ENDIAN);

                        int index = 0;
                        byte[] writingBuffer;

                        String timestampStr = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                        ); 
        
                        Path specificTmpPath = TMP_PATH.resolve("gama_simulation_launcher_" + timestampStr);
                        String specificTmpPathStr = specificTmpPath.toString();
                        String extractorTargetPathStr = specificTmpPath.resolve("extractor").toString();
                        String zipPayloadTargetPathStr = specificTmpPath.resolve("archive.zip").toString();
                        String extractedZipTargetPathStr = specificTmpPath.resolve("gama").toString();
                        String gamaEntryPointPathStr = Path.of(extractedZipTargetPathStr).resolve("Gama").toString();
                        
                        // writing the specific tmp path
                        index = writeString(metaData,index,specificTmpPathStr);

                        // // writing the extractor desired Path where the runner will write it on the disk
                        index = writeString(metaData,index,extractorTargetPathStr);

                        // writing the zip payload desired Path where the runner will write it on the disk
                        index = writeString(metaData,index,zipPayloadTargetPathStr);

                        // writing the desired Path where the zip will be extracted 
                        // (must be 1 directory deeper than the specific tmp path max) 
                        index = writeString(metaData,index,extractedZipTargetPathStr);

                        // writing the Gama entrypoint path
                        index = writeString(metaData,index,gamaEntryPointPathStr);

                        // writing the extractor size
                        metaData.putLong(index,extractorSize);
                        index += 8;
                        // writing the offset zip payload offset
                        metaData.putLong(index,extractorSize);
                        index += 8;
                        // writing the zip payload size
                        metaData.putLong(index,zipPayloadSize);
                        index += 8;

                        metaData.position(index);

                        // ByteBuffer debug = metaData.asReadOnlyBuffer();

                        // while (debug.hasRemaining()) {
                        //         int b = debug.get() & 0xff;

                        //         System.out.printf(
                        //                 "%c", (b >= 32 && b < 127) ? (char) b : '.'
                        //         );
                        // }

                        // System.out.println("");


                        metaData.flip();

                        long offset = metaDataOffset;

                        while (metaData.hasRemaining()) {
                                int written = channel.write(metaData, offset);
                                offset += written;
                        }
                }
        }


        private static void readFully(
                FileChannel channel,
                ByteBuffer buffer,
                long position
                ) throws IOException {

                while (buffer.hasRemaining()) {
                        int n = channel.read(buffer, position);

                        if (n < 0) 
                                throw new IOException("Unexpected end of file");

                        position += n;
                }
        }

        private static long getMetaDataOffset(Path runnerPath) throws IOException
        {
                byte[] data = Files.readAllBytes(runnerPath);

                String content = new String(
                        data,
                        StandardCharsets.US_ASCII
                );

                long offset = content.indexOf(DATA_START_SYMBOL);

                if (offset < 0)
                        throw new IOException("__DATA_START__ not found");

                // start + length + nullbyte
                return offset + DATA_START_SYMBOL.length() + 1;
        }

        /**
         * Equivalent to:
         *
         * chmod +x build/out
         */
        private static void makeExecutable(Path file) throws IOException {

                var permissions =
                        java.nio.file.Files.getPosixFilePermissions(file);

                permissions.add(
                        java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
                );
                permissions.add(
                        java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
                );
                permissions.add(
                        java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
                );

                java.nio.file.Files.setPosixFilePermissions(
                        file,
                        permissions
                );
        }

        private static int writeString(ByteBuffer buffer, int index, String data)
        {
                byte[] writingBuffer = data
                        .getBytes(StandardCharsets.US_ASCII);

                buffer.put(index, writingBuffer);
                index += data.length();

                // padding of null byte
                int paddingSize = META_DATA_STRING_SIZE - data.length();
                buffer.put(index, new byte[paddingSize]);
                index += paddingSize;

                return index;
        }
}