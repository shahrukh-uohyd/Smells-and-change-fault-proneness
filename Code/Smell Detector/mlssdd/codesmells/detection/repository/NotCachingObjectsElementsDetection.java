
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
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

import mlssdd.codesmells.detection.AbstractCodeSmellDetection;
import mlssdd.codesmells.detection.ICodeSmellDetection;
import mlssdd.kernel.impl.MLSCodeSmell;

public class NotCachingObjectsElementsDetection
		extends AbstractCodeSmellDetection implements ICodeSmellDetection {

	public void detect(final Document xml) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
		
		//  "An abundance of GetFieldID() and GetMethodID() calls,  in particular, if the
		//  calls are for the same fields and methods, indicates that the fields and
		// method are not being cached."
		//  https://www.ibm.com/developerworks/library/j-jni/index.html
		 

		final Set<String> methods = new HashSet<>(
			Arrays.asList("GetFieldID", "GetMethodID", " GetStaticMethodID"));
		final Set<MLSCodeSmell> notCachedSet = new HashSet<>();

		try {
			final NodeList cList =
				(NodeList) AbstractCodeSmellDetection.C_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final NodeList javaList =
				(NodeList) AbstractCodeSmellDetection.JAVA_FILES_EXP
					.evaluate(xml, XPathConstants.NODESET);
			final int cLength = cList.getLength();
			final int javaLength = javaList.getLength();

			/*
			 * FIRST CASE An ID is looked up in a function that is called several times
			 */

			// Functions that are potentially called several times in the host language
			final Set<String> nativeDeclSet = new HashSet<String>();
			final Set<String> hostCallSet = new HashSet<String>();
			final Set<String> severalCallSet = new HashSet<String>();
			for (int i = 0; i < javaLength; i++) {
				final Node javaXml = javaList.item(i);
				final NodeList nativeDeclList =
					(NodeList) AbstractCodeSmellDetection.NATIVE_DECL_EXP
						.evaluate(javaXml, XPathConstants.NODESET);
				final NodeList hostCallList =
					(NodeList) AbstractCodeSmellDetection.HOST_CALL_EXP
						.evaluate(javaXml, XPathConstants.NODESET);
				final int nativeDeclLength = nativeDeclList.getLength();
				final int hostCallLength = hostCallList.getLength();
				for (int j = 0; j < nativeDeclLength; j++) {
					nativeDeclSet.add(nativeDeclList.item(j).getTextContent());
				}
				for (int j = 0; j < hostCallLength; j++) {
					final Node thisNode = hostCallList.item(j);
					final String thisMethod = thisNode.getTextContent();
					final boolean isInALoop = !AbstractCodeSmellDetection.xPath
						.evaluate("ancestor::for|ancestor::while", thisNode)
						.equals("");
					if (!hostCallSet.add(thisMethod) || isInALoop) {
						severalCallSet.add(thisMethod);
					}
				}
			}
			nativeDeclSet.retainAll(severalCallSet);

			// Native functions that look up an ID
			final List<String> nativeSelectorList = new LinkedList<>();
			final List<String> IDSelectorList = new LinkedList<>();
			for (final String method : methods) {
				nativeSelectorList
					.add(
						String
							.format(
								"descendant::call/name/name = '%s'",
								method));
				IDSelectorList.add(String.format("name/name = '%s'", method));
			}
			final String nativeSelector =
				String.join(" or ", nativeSelectorList);
			final String IDSelector = String.join(" or ", IDSelectorList);
			final String nativeQuery = String
				.format(
					"descendant::function[(%s) and name != 'JNI_OnLoad']",
					nativeSelector);
			final String IDQuery = String
				.format("descendant::call[%s]//argument_list/", IDSelector)
					+ "argument[position() = %d]";

			// Queries used for second case detection
			final String funcQuery =
				"descendant::function[name != 'JNI_OnLoad']";
			final String callTemplate = "descendant::call[name/name = '%s']";
			final String argsQuery = "descendant::argument_list";
			final String argNameQuery =
				"descendant::argument_list/argument[position() = %d]";

			final XPathExpression nativeExpr =
				AbstractCodeSmellDetection.xPath.compile(nativeQuery);
			final XPathExpression CIDExpr = AbstractCodeSmellDetection.xPath
				.compile(String.format(IDQuery, 3));
			final XPathExpression CPPIDExpr = AbstractCodeSmellDetection.xPath
				.compile(String.format(IDQuery, 2));
			final XPathExpression funcExpr =
				AbstractCodeSmellDetection.xPath.compile(funcQuery);
			final XPathExpression argsExpr =
				AbstractCodeSmellDetection.xPath.compile(argsQuery);
			final XPathExpression CArgNameExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argNameQuery, 3));
			final XPathExpression CPPArgNameExpr =
				AbstractCodeSmellDetection.xPath
					.compile(String.format(argNameQuery, 2));

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
				XPathExpression IDExpr;
				XPathExpression argNameExpr;
				if (isC) {
					IDExpr = CIDExpr;
					argNameExpr = CArgNameExpr;
				}
				else {
					IDExpr = CPPIDExpr;
					argNameExpr = CPPArgNameExpr;
				}

				final NodeList nativeList = (NodeList) nativeExpr
					.evaluate(cXml, XPathConstants.NODESET);
				for (int j = 0; j < nativeList.getLength(); j++) {
					// WARNING: This only keeps the part of the function name after the last
					// underscore ("_") in respect to JNI syntax. Therefore, it does not work for
					// functions with _ in their names. This should not happen if names are written
					// in lowerCamelCase.
					final String funcLongName =
						AbstractCodeSmellDetection.NAME_EXP
							.evaluate(nativeList.item(j));
					final String[] partsOfName = funcLongName.split("_");
					final String funcName = partsOfName[partsOfName.length - 1];

					if (nativeDeclSet.contains(funcName)) {
						final NodeList IDs = (NodeList) IDExpr
							.evaluate(
								nativeList.item(j),
								XPathConstants.NODESET);
						for (int k = 0; k < IDs.getLength(); k++) {
							notCachedSet
								.add(
									new MLSCodeSmell(
										this.getCodeSmellName(),
										IDs.item(k).getTextContent(),
										funcLongName,
										"",
										"",
										cFilePath));
						}
					}
				}

				/*
				 * SECOND CASE Inside a function, a same ID is looked up at least twice
				 */
				// We consider that the necessary fields are cached in the function JNI_OnLoad,
				// called only once
				final NodeList funcList =
					(NodeList) funcExpr.evaluate(cXml, XPathConstants.NODESET);
				final int funcLength = funcList.getLength();
				// Analysis for each function
				for (int j = 0; j < funcLength; j++) {
					final String funcLongName =
						AbstractCodeSmellDetection.NAME_EXP
							.evaluate(funcList.item(j));
					// Analysis for each Get<>ID
					for (final String method : methods) {
						final Set<NodeList> args = new HashSet<>();
						final String callQuery =
							String.format(callTemplate, method);
						final NodeList callList =
							(NodeList) AbstractCodeSmellDetection.xPath
								.evaluate(
									callQuery,
									funcList.item(j),
									XPathConstants.NODESET);
						final int callLength = callList.getLength();
						for (int k = 0; k < callLength; k++) {
							// Arguments should be treated and compared as NodeLists and not Strings, in
							// case they do not respect the same conventions (e.g. concerning spaces)
							final NodeList theseArgs = (NodeList) argsExpr
								.evaluate(
									callList.item(k),
									XPathConstants.NODESET);
							if (this.setContainsNodeList(args, theseArgs)) {
								notCachedSet
									.add(
										new MLSCodeSmell(
											this.getCodeSmellName(),
											argNameExpr
												.evaluate(callList.item(k)),
											funcLongName,
											"",
											"",
											cFilePath));
							}
							else {
								args.add(theseArgs);
							}

						}
					}
				}
			}
			}
			this.setSetOfSmells(notCachedSet);
		}
		catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	private boolean nodeListsEqual(final NodeList nl1, final NodeList nl2) {
		final int l1 = nl1.getLength();
		final int l2 = nl2.getLength();

		if (l1 != l2) {
			return false;
		}

		for (int i = 0; i < l1; i++) {
			if (!nl1.item(i).isEqualNode(nl2.item(i))) {
				return false;
			}
		}

		return true;
	}

	private boolean setContainsNodeList(
		final Set<NodeList> hs,
		final NodeList nl) {
		for (final NodeList cur_nl : hs) {
			if (this.nodeListsEqual(cur_nl, nl)) {
				return true;
			}
		}

		return false;
	}

}
