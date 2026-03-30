

package mlssdd.codesmells.detection.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

public class MemoryManagementMismatchDetection
		extends AbstractCodeSmellDetection implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final Set<MLSCodeSmell> notReleasedSet = new HashSet<>();

		final List<String> types = Arrays
			.asList(
				"StringChars",
				"StringUTFChars",
				"BooleanArrayElements",
				"ByteArrayElements",
				"CharArrayElements",
				"ShortArrayElements",
				"IntArrayElements",
				"LongArrayElements",
				"FloatArrayElements",
				"DoubleArrayElements",
				"PrimitiveArrayCritical",
				"StringCritical");

		final String genericCallQuery = "descendant::call[name/name='%s']";
		final String argQuery = "argument_list/argument[%d]/expr/name";
		final String nodeWithGivenArg =
			"%s[argument_list/argument[%d]/expr/name='%s']";

		try {
			final XPathExpression firstArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 1));
			final XPathExpression secondArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 2));
			final XPathExpression funcExpr = AbstractCodeSmellDetection.xPath
				.compile("descendant::function");

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node cXml = cList.item(i);
				//CHECK IF IT IS A JNI FILE
		    	XPathExpression n = xpath.compile("descendant::function_decl/type[name='JNIEXPORT'] |"+
		    		"descendant::function/type[name='JNIEXPORT'] |"+
		    		"descendant::decl/type[name='JNIEnv']");
		    	NodeList nativeImpl = (NodeList) n.evaluate(cXml, XPathConstants.NODESET);
		    
		      	if(nativeImpl.getLength()>0)
		      	{ //IT IS A JNI FILE
				final String cFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(cXml);

				// C: second argument
				// C++: first argument
				final boolean isC = AbstractCodeSmellDetection.LANGUAGE_EXP
					.evaluate(cXml)
					.equals("C");
				int nbArg;
				XPathExpression argExpr;
				if (isC) { // C file
					nbArg = 2;
					argExpr = secondArgExpr;
				}
				else { // C ++ file
					nbArg = 1;
					argExpr = firstArgExpr;
				}
				final NodeList funcList =
					(NodeList) funcExpr.evaluate(cXml, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();
				// Analysis for each function
				for (int j = 0; j < funcLength; j++) {
					final Node thisFunction = funcList.item(j);
					final String funcName = AbstractCodeSmellDetection.NAME_EXP
						.evaluate(thisFunction);
					// Analysis for each type
					final Iterator<String> it = types.iterator();
					while (it.hasNext()) {
						final String thisType = it.next();

						final String getType = String.format("Get%s", thisType);
						final String releaseType =
							String.format("Release%s", thisType);

						final String getCallQuery =
							String.format(genericCallQuery, getType);
						final String releaseCallQuery =
							String.format(genericCallQuery, releaseType);

						final NodeList getList =
							(NodeList) AbstractCodeSmellDetection.xPath
								.evaluate(
									getCallQuery,
									thisFunction,
									XPathConstants.NODESET);

						final MLSCodeSmell codeSmell = new MLSCodeSmell(
							this.getCodeSmellName(),
							thisType,
							funcName,
							"",
							"",
							cFilePath);

						// Look for a call to the matching release function
						// The second argument should match (name of the Java object)
						for (int k = 0; k < getList.getLength(); k++) {
							// TODO Second argument in C, but first in C++?
							final String arg =
								argExpr.evaluate(getList.item(k));
							final String nodeWithGivenArgQuery = String
								.format(
									nodeWithGivenArg,
									releaseCallQuery,
									nbArg,
									arg);
							final String matchedRelease =
								AbstractCodeSmellDetection.xPath
									.evaluate(
										nodeWithGivenArgQuery,
										thisFunction);
							if (matchedRelease == "") {
								notReleasedSet.add(codeSmell);
							}
						}

					}
				}
			}
			}
			this.setSetOfSmells(notReleasedSet);

		}
		catch (

		final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
