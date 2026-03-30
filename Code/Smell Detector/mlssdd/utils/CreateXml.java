package mlssdd.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.stream.StreamResult;
// import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
// import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
// import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
// import org.apache.commons.compress.utils.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class CreateXml {
		

	static final String srcmlPath = "srcml_1.0.0-1_ubuntu20.04/bin/srcml";

	 
	public static Document parseSingleDocument(final String fileName) {
		final List<String> params = new ArrayList<String>();
		params.add(CreateXml.srcmlPath);
		params.add(fileName);
		Document xmlDocument = null;
		try {
			final Process process = new ProcessBuilder(params).start();
			final InputStream inputStream = process.getInputStream();
			final DocumentBuilderFactory builderFactory =
				DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = builderFactory.newDocumentBuilder();
			xmlDocument = builder.parse(inputStream);
		}
		catch (final ParserConfigurationException | SAXException
				| IOException e) {
			e.printStackTrace();
		}
		return xmlDocument;
	}

}
