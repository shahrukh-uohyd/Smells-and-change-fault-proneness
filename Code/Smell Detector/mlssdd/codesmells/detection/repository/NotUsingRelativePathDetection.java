
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



public class NotUsingRelativePathDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final Set<MLSCodeSmell> notRelativePathsSet = new HashSet<>();

		// TODO System.load and System.loadLibrary: only way to load a library?
		final String loadQuery =
			"descendant::call[name = 'System.loadLibrary' or name = 'System.load']//argument//literal";

		try {
			final XPathExpression loadExpr =
				AbstractCodeSmellDetection.xPath.compile(loadQuery);

			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();

			for (int i = 0; i < javaLength; i++) {
				//CHECK IF IT IS A JNI FILE
		    	XPathExpression n = xpath.compile("descendant::function_decl[type/specifier='native']/name");
		    	NodeList nativeMethods = (NodeList) n.evaluate(javaList.item(i), XPathConstants.NODESET);
		    
		      if(nativeMethods.getLength()>0)
		      	{ //IT IS A JNI FILE
				final NodeList loadList = (NodeList) loadExpr
					.evaluate(javaList.item(i), XPathConstants.NODESET);
				final int loadLength = loadList.getLength();
				for (int j = 0; j < loadLength; j++) {
					final Node thisLoad = loadList.item(j);
					final String lib = thisLoad.getTextContent();
					if (lib.charAt(1) != '.' && lib.charAt(1) != '/') {
						final String thisMethod =
							AbstractCodeSmellDetection.FUNC_EXP
								.evaluate(thisLoad);
						final String thisClass =
							AbstractCodeSmellDetection.CLASS_EXP
								.evaluate(thisLoad);
						final String thisPackage =
							AbstractCodeSmellDetection.PACKAGE_EXP
								.evaluate(thisLoad);
						final String javaFilePath =
							AbstractCodeSmellDetection.FILEPATH_EXP
								.evaluate(javaList.item(i));
						notRelativePathsSet
							.add(
								new MLSCodeSmell(
									this.getCodeSmellName(),
									lib,
									thisMethod,
									thisClass,
									thisPackage,
									javaFilePath));
					}
				}
			}
			}
			this.setSetOfSmells(notRelativePathsSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
