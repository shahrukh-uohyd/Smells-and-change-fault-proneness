
package mlssdd.codesmells.detection.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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

public class NotHandlingExceptionsDetection extends AbstractCodeSmellDetection
		implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();

		final Set<String> methods = new HashSet<>(
			Arrays
				.asList(
					"FindClass",
					"GetFieldID",
					"GetStaticFieldID",
					"GetMethodID",
					"GetStaticMethodID"));
		final Set<String> exceptions =
			new HashSet<>(Arrays.asList("ExceptionOccurred", "ExceptionCheck"));
		final Set<MLSCodeSmell> notCheckedSet = new HashSet<>();

		try {
			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();

			final List<String> selectorList = new LinkedList<>();
			final List<String> exceptSelectorList = new LinkedList<>();
			for (final String method : methods) {
				selectorList
					.add(
						String
							.format(
								"descendant::call/name/name = '%s'",
								method));
			}
			for (final String exception : exceptions) {
				exceptSelectorList.add(String.format(". = '%s'", exception));
			}
			final String selector = String.join(" or ", selectorList);
			final String exceptSelector =
				String.join(" or ", exceptSelectorList);

			final String declQuery = String
				.format(
					"descendant::decl_stmt[%s]/decl | descendant::expr_stmt[%s]/expr",
					selector,
					selector);
			final String argQuery =
				"descendant::call/argument_list/argument[%d]/expr/name | descendant::call/argument_list/argument[%d]/expr/literal";
			final String exceptQuery = String
				.format(
					"descendant::if/condition/expr/call/name/name[%s]",
					exceptSelector);

			final XPathExpression declExpr =
				AbstractCodeSmellDetection.xPath.compile(declQuery);
			final XPathExpression exceptExpr =
				AbstractCodeSmellDetection.xPath.compile(exceptQuery);
			final XPathExpression firstArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 1, 1));
			final XPathExpression secondArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 2, 2));
			final XPathExpression thirdArgExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argQuery, 3, 3));

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
				final boolean isC = AbstractCodeSmellDetection.LANGUAGE_EXP
					.evaluate(cXml)
					.equals("C");

				final NodeList declList =
					(NodeList) declExpr.evaluate(cXml, XPathConstants.NODESET);
				final NodeList exceptList = (NodeList) exceptExpr
					.evaluate(cXml, XPathConstants.NODESET);
				final int declLength = declList.getLength();
				final int exceptLength = exceptList.getLength();

				for (int j = 0; j < declLength; j++) {
					final Node thisDecl = declList.item(j);
					final String funcName =
						AbstractCodeSmellDetection.FUNC_EXP.evaluate(thisDecl);
					String arg;

					// C file
					if (isC) {
						arg = thirdArgExpr.evaluate(thisDecl);
						if (arg.equals("")) {
							arg = secondArgExpr.evaluate(thisDecl);
						}
					}

					// C++ file
					else {
						arg = secondArgExpr.evaluate(thisDecl);
						if (arg.equals("")) {
							arg = firstArgExpr.evaluate(thisDecl);
						}
					}

					boolean isNotChecked = true;

					// Check if the exception is handled
					for (int k = 0; k < exceptLength; k++) {
						if (declList
							.item(j)
							.compareDocumentPosition(
								exceptList
									.item(
										k)) == Node.DOCUMENT_POSITION_FOLLOWING) {
							isNotChecked = false;
						}
					}

					if (isNotChecked) {
						notCheckedSet
							.add(
								new MLSCodeSmell(
									this.getCodeSmellName(),
									arg,
									funcName,
									"",
									"",
									cFilePath));
					}
				}
			}
			}

			this.setSetOfSmells(notCheckedSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

}
