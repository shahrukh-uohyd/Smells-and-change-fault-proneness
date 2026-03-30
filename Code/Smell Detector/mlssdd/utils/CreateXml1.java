package mlssdd.utils;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

// Apache Commons Compress (for tar.gz)
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public final class CreateXml1 {

    // Path to srcML executable (update as needed)
    static final String SRCML_PATH = "srcml_1.0.0-1_ubuntu20.04/bin/srcml";

    /**
     * Public method: parses one or multiple files to XML using srcML.
     */
    public static Document parse(final String... fileNames) {
        if (fileNames == null || fileNames.length == 0) {
            System.err.println("No input files specified.");
            return null;
        }

        if (fileNames.length == 1) {
            return parseSingleDocument(fileNames[0]);
        } else {
            return parseArchive(fileNames);
        }
    }

    /**
     * Handles multiple files: create a tar.gz archive, feed it to srcML, then delete it.
     */
    private static Document parseArchive(final String... fileNames) {
        List<File> files = new ArrayList<>();
        for (String name : fileNames) {
            if (name != null) {
                files.add(new File(name));
            }
        }

        if (files.isEmpty()) {
            System.err.println("No valid files to archive.");
            return null;
        }

        String archiveName = files.get(0).getName() + "_temp.tar.gz";

        try {
            // Create tar.gz archive
            try (FileOutputStream fos = new FileOutputStream(archiveName);
                 GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos);
                 TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos)) {

                taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
                taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                taos.setAddPaxHeadersForNonAsciiNames(true);

                for (File f : files) {
                    addToArchive(taos, f, ".");
                }
            }

            // Parse the archive via srcML
            Document xml = parseSingleDocument(archiveName);

            // Clean up
            new File(archiveName).delete();

            return xml;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Adds a file or directory recursively to the tar.gz archive.
     */
    private static void addToArchive(TarArchiveOutputStream out, File file, String base) throws IOException {
        String entryName = base + File.separator + file.getName();

        if (file.isFile()) {
            out.putArchiveEntry(new TarArchiveEntry(file, entryName));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchive(out, child, entryName);
                }
            }
        }
    }

    /**
     * Parses a single file (or archive) using srcML and returns the XML DOM.
     */
    public static Document parseSingleDocument(final String fileName) {
        List<String> params = new ArrayList<>();
        params.add(SRCML_PATH);
        params.add(fileName);

        Document xmlDocument = null;
        try {
            Process process = new ProcessBuilder(params).start();
            try (InputStream inputStream = process.getInputStream()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                xmlDocument = builder.parse(inputStream);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return xmlDocument;
    }

//     public static void main(String[] args) {
//     if (args.length == 0) {
//         System.out.println("Usage: java mlssdd.utils.CreateXml1 <file1> [file2 ...]");
//         return;
//     }

//     Document xml = CreateXml1.parse(args);
//     if (xml != null) {
//         System.out.println("XML document parsed successfully for " + args.length + " file(s).");
//     } else {
//         System.out.println("Failed to parse files.");
//     }
// }

}




