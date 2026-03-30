
package mlssdd.antipatterns.detection.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mlssdd.antipatterns.detection.AbstractAntiPatternDetection;
import mlssdd.antipatterns.detection.IAntiPatternDetection;
import mlssdd.kernel.impl.MLSAntiPattern;
import mlssdd.utils.PropertyGetter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class TooMuchScatteringDetectionModified extends AbstractAntiPatternDetection
		implements IAntiPatternDetection {

	@Override
	public void detect(final Document xml) {

		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		final int minNbOfClasses =
			PropertyGetter.getIntProp("TooMuchScattering.MinNbOfClasses", 2);
		final int maxNbOfMethods =
			PropertyGetter.getIntProp("TooMuchScattering.MaxNbOfMethods", 5);

		final Set<MLSAntiPattern> shortClassesSet = new HashSet<>();

		try {
			final XPathExpression JAVA_FILES_EXP = this.xPath
				.compile(
					IAntiPatternDetection.JAVA_FILES_QUERY
							+ "[package/name != '']");
			final XPathExpression PACKAGE_EXP =
				this.xPath.compile(IAntiPatternDetection.PACKAGE_QUERY);
			final XPathExpression CLASS_EXP =
				this.xPath.compile(IAntiPatternDetection.CLASS_QUERY);
			final XPathExpression FILEPATH_EXP =
				this.xPath.compile(IAntiPatternDetection.FILEPATH_QUERY);
			final XPathExpression NATIVE_EXP =
				this.xPath.compile(IAntiPatternDetection.NATIVE_QUERY);

			final Map<String, List<MLSAntiPattern>> classesInPackage =
				new HashMap<>();

			final NodeList javaList =
				(NodeList) JAVA_FILES_EXP.evaluate(xml, XPathConstants.NODESET);
			final int javaLength = javaList.getLength();
			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);

				//modification-- extracting native methods at class level
				// Java classes
				final NodeList classList = (NodeList) this.xPath
					.evaluate("descendant::class", javaXml, XPathConstants.NODESET);
				final int nbClasses = classList.getLength();
				
				for(int c=0; c<nbClasses; c++)
				{

				// Native method declaration
				// final NodeList nativeDeclList = (NodeList) NATIVE_EXP
				// 	.evaluate(classList.item(c), XPathConstants.NODESET);
					//only extract the native method from the current class (this include the methods from the inner class also.
					XPathExpression e5 = xpath.compile(".//function_decl[type/specifier='native']");
      				final NodeList nativeDeclList = (NodeList) e5.evaluate(classList.item(c), XPathConstants.NODESET);
				final int OuterDeclLength = nativeDeclList.getLength();
				//System.out.println(OuterDeclLength);
				//extract the native method from the inner classes.
				XPathExpression e6 = xpath.compile(".//class/block/function_decl[type/specifier='native']");
      				final NodeList nativeDeclList1 = (NodeList) e6.evaluate(classList.item(c), XPathConstants.NODESET);
				final int innerDeclLength = nativeDeclList1.getLength();				
				//System.out.println(innerDeclLength);
				//subtract the inner methods from the outer method to get the number of native methods in the current class only.
				int nativeDeclLength= OuterDeclLength-innerDeclLength;

				System.out.println(nativeDeclLength);
				if (nativeDeclLength > 0
						&& nativeDeclLength <= 5) {
					//final String thisClass = CLASS_EXP.evaluate(classList.item(c));
					//class name
			        XPathExpression c1 = xpath.compile("name/name | name");
			        String thisClass = c1.evaluate(classList.item(c));
					final String thisPackage = PACKAGE_EXP.evaluate(javaList.item(i));
					final String thisFilePath = FILEPATH_EXP.evaluate(javaXml);
					final MLSAntiPattern ap = new MLSAntiPattern(
						this.getAntiPatternName(),
						"",
						"",
						thisClass,
						thisPackage,
						thisFilePath);
					if (!classesInPackage.containsKey(thisPackage)) {
						classesInPackage.put(thisPackage, new LinkedList<>());
					}
					classesInPackage.get(thisPackage).add(ap);
				}

			}

		}

			for (final Map.Entry<String, List<MLSAntiPattern>> entry : classesInPackage
				.entrySet()) {
				if (entry.getValue().size() >= 2) {
					
					//modification--adding filepath in equals method of MLSAntipatter
					//because when adding to shortClassesSet it compares
					//and same method, class, package name can exist in different file.
					entry.getValue().forEach(shortClassesSet::add);
					
				}
			}
			System.out.println(shortClassesSet);
			this.setSetOfAntiPatterns(shortClassesSet);

		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
