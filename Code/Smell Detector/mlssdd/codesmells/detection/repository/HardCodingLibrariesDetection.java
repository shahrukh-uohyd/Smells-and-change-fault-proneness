
package mlssdd.codesmells.detection.repository;

import java.util.HashSet;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

public class HardCodingLibrariesDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final Set<MLSCodeSmell> hardCodedLibraries = new HashSet<>();
		final XPath xPath = XPathFactory.newInstance().newXPath();
		// TODO System.load and System.loadLibrary: only way to load a library?
		// TODO Considered hard-coded when trying to load a library in a try statement
		// and in a following catch statement: correct assumption?
		final String loadQuery =
			"//call[name = 'System.loadLibrary' or name = 'System.load']//argument";
		final String hardCodedQuery =
			String.format("descendant::try[catch%s]%s", loadQuery, loadQuery);

		try {

			final XPathExpression hardCodedExpr = xPath.compile(hardCodedQuery);

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node javaXml = cList.item(i);
				//CHECK IF IT IS A JNI FILE
		    	XPathExpression n = xpath.compile("descendant::function_decl[type/specifier='native']/name");
		    	NodeList nativeMethods = (NodeList) n.evaluate(javaXml, XPathConstants.NODESET);
		    
		      if(nativeMethods.getLength()>0)
		      	{ //IT IS A JNI FILE
				final String javaFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(javaXml);
				final NodeList loadList = (NodeList) hardCodedExpr
					.evaluate(javaXml, XPathConstants.NODESET);
				final int loadLength = loadList.getLength();
				for (int j = 0; j < loadLength; j++) {
					final String arg = loadList.item(j).getTextContent();
					System.out.println(arg);
					final String thisMethod =
						AbstractCodeSmellDetection.FUNC_EXP
							.evaluate(loadList.item(j));
					final String thisClass =
						AbstractCodeSmellDetection.CLASS_EXP
							.evaluate(loadList.item(j));
					final String thisPackage =
						AbstractCodeSmellDetection.PACKAGE_EXP
							.evaluate(loadList.item(j));
					hardCodedLibraries
						.add(
							new MLSCodeSmell(
								this.getCodeSmellName(),
								arg,
								thisMethod,
								thisClass,
								thisPackage,
								javaFilePath));
				}
			}
			}
			this.setSetOfSmells(hardCodedLibraries);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
