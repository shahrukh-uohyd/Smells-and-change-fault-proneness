
package mlssdd.codesmells.detection.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
import mlssdd.utils.PropertyGetter;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

public class PassingExcessiveObjectsDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		// TODO If object used for something else than get, it is not a code smell to
		// pass it as a parameter
		// TODO Not a code smell if assignment of the object (or pointer) in the native
		// code

		final int maxNbOfFields = PropertyGetter
			.getIntProp("PassingExcessiveObjects.MaxNbOfFields", 5);

		final Set<String> types = new HashSet<>(
			Arrays
				.asList(
					"ObjectField",
					"BooleanField",
					"ByteField",
					"CharField",
					"ShortField",
					"IntField",
					"LongField",
					"FloatField",
					"DoubleField"));

		final String staticTemplate = "Static%s";
		final String getTemplate = "Get%s";
		final String setTemplate = "Set%s";

		final Set<MLSCodeSmell> excessiveObjectsSet = new HashSet<>();
		final String callTemplate = "descendant::call/name/name[. = '%s']";
		final String paramQuery =
			"parameter_list/parameter[position()>2]/decl[type/name = 'jobject']/name";
		final String funcQuery = "descendant::function";

		try {
			final XPathExpression funcExpr =
				AbstractCodeSmellDetection.xPath.compile(funcQuery);
			final XPathExpression paramExpr =
				AbstractCodeSmellDetection.xPath.compile(paramQuery);

			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			for (int i = 0; i < cLength; i++) {
				final Node cFile = cList.item(i);
				//CHECK IF IT IS A JNI FILE
		    	XPathExpression n = xpath.compile("descendant::function_decl/type[name='JNIEXPORT'] |"+
		    		"descendant::function/type[name='JNIEXPORT'] |"+
		    		"descendant::decl/type[name='JNIEnv']");
		    	NodeList nativeImpl = (NodeList) n.evaluate(cFile, XPathConstants.NODESET);
		    
		      	if(nativeImpl.getLength()>0)
		      	{ //IT IS A JNI FILE

				final String cFilePath =
					AbstractCodeSmellDetection.FILEPATH_EXP.evaluate(cFile);

				final NodeList funcList =
					(NodeList) funcExpr.evaluate(cFile, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();

				// Analysis for each function
				for (int j = 0; j < funcLength; j++) {
					final Node thisFunc = funcList.item(j);
					final String funcName =
						AbstractCodeSmellDetection.NAME_EXP.evaluate(thisFunc);
					final NodeList paramList = (NodeList) paramExpr
						.evaluate(thisFunc, XPathConstants.NODESET);
					final int paramLength = paramList.getLength();

					// Analysis for each parameter that is an object
					for (int k = 0; k < paramLength; k++) {
						int nbGet = 0;
						final Iterator<String> it = types.iterator();
						while (it.hasNext()) {
							// TODO Refactor

							// If the function sets a field, then passing the object as an argument was
							// necessary and not a code smell
							final String thisType = it.next();
							final String setQuery = String
								.format(
									callTemplate,
									String.format(setTemplate, thisType));
							final NodeList setList =
								(NodeList) AbstractCodeSmellDetection.xPath
									.evaluate(
										setQuery,
										thisFunc,
										XPathConstants.NODESET);
							if (setList.getLength() > 0) {
								break;
							}
							final String setStaticQuery = String
								.format(
									callTemplate,
									String
										.format(
											setTemplate,
											String
												.format(
													staticTemplate,
													thisType)));
							final NodeList setStaticList =
								(NodeList) AbstractCodeSmellDetection.xPath
									.evaluate(
										setStaticQuery,
										thisFunc,
										XPathConstants.NODESET);
							if (setStaticList.getLength() > 0) {
								break;
							}

							// Accesses to fields of the current object
							final String getQuery = String
								.format(
									callTemplate,
									String.format(getTemplate, thisType));
							final NodeList getList =
								(NodeList) AbstractCodeSmellDetection.xPath
									.evaluate(
										getQuery,
										thisFunc,
										XPathConstants.NODESET);
							nbGet += getList.getLength();
							final String getStaticQuery = String
								.format(
									callTemplate,
									String
										.format(
											getTemplate,
											String
												.format(
													staticTemplate,
													thisType)));
							final NodeList getStaticList =
								(NodeList) AbstractCodeSmellDetection.xPath
									.evaluate(
										getStaticQuery,
										thisFunc,
										XPathConstants.NODESET);
							nbGet += getStaticList.getLength();
						}

						// If there are many accesses, the code smell is justified:
						// better pass the object as a parameter than pass too many fields
						if (nbGet > 0 && nbGet < maxNbOfFields) {
							excessiveObjectsSet
								.add(
									new MLSCodeSmell(
										this.getCodeSmellName(),
										paramList.item(k).getTextContent(),
										funcName,
										"",
										"",
										cFilePath));
						}
					}
				}
			}
			}

			this.setSetOfSmells(excessiveObjectsSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
