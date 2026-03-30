
package mlssdd.codesmells.detection.repository;

import java.util.HashSet;
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



public class NotSecuringLibrariesDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final Set<MLSCodeSmell> notSecureLibraries = new HashSet<>();

		// TODO System.load and System.loadLibrary: only way to load a library?
		final String loadQuery =
			"call[name = 'System.loadLibrary' or name = 'System.load']//argument";
		final String secureQuery =
			"descendant::call[name = 'AccessController.doPrivileged']//"
					+ loadQuery;

		try {
			final XPathExpression loadExpr = AbstractCodeSmellDetection.xPath
				.compile("descendant::" + loadQuery);
			final XPathExpression secureExpr =
				AbstractCodeSmellDetection.xPath.compile(secureQuery);

			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);
				//CHECK IF IT IS A JNI FILE
		    	XPathExpression n = xpath.compile("descendant::function_decl[type/specifier='native']/name");
		    	NodeList nativeMethods = (NodeList) n.evaluate(javaXml, XPathConstants.NODESET);
		    
		      if(nativeMethods.getLength()>0)
		      	{ //IT IS A JNI FILE
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaXml);
					System.out.println(javaFilePath);

				final NodeList loadList = (NodeList) loadExpr
					.evaluate(javaXml, XPathConstants.NODESET);
				final NodeList secureList = (NodeList) secureExpr
					.evaluate(javaXml, XPathConstants.NODESET);
				final int loadLength = loadList.getLength();
				final int secureLength = secureList.getLength();

				// TODO Refactor the loops
				for (int j = 0; j < loadLength; j++) {
					final Node thisNode = loadList.item(j);
					final String thisLibrary = thisNode.getTextContent();
					final String thisMethod =
						AbstractCodeSmellDetection.FUNC_EXP.evaluate(thisNode);
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP.evaluate(thisNode);
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(thisNode);
					notSecureLibraries
						.add(
							new MLSCodeSmell(
								this.getCodeSmellName(),
								thisLibrary,
								thisMethod,
								thisClass,
								thisPackage,
								javaFilePath));
				}
				for (int j = 0; j < secureLength; j++) {
					final Node thisNode = secureList.item(j);
					final String thisLibrary = thisNode.getTextContent();
					final String thisMethod =
						AbstractCodeSmellDetection.FUNC_EXP.evaluate(thisNode);
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP.evaluate(thisNode);
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(thisNode);
					notSecureLibraries
						.remove(
							new MLSCodeSmell(
								this.getCodeSmellName(),
								thisLibrary,
								thisMethod,
								thisClass,
								thisPackage,
								javaFilePath));
				}
			}
			}
			this.setSetOfSmells(notSecureLibraries);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
