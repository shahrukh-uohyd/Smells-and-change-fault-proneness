

package mlssdd.codesmells.detection;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSCodeSmell;

public abstract class AbstractCodeSmellDetection
		implements ICodeSmellDetection {
	//	private BoxPlot boxPlot;
	protected static XPath xPath = XPathFactory.newInstance().newXPath();

	/*
	 * Initialization of the constant <code>XPathExpression</code>s
	 */
	protected static XPathExpression FILE_EXP;
	protected static XPathExpression C_FILES_EXP;
	protected static XPathExpression JAVA_FILES_EXP;
	protected static XPathExpression LANGUAGE_EXP;
	protected static XPathExpression FUNC_EXP;
	protected static XPathExpression CLASS_EXP;
	protected static XPathExpression PACKAGE_EXP;
	protected static XPathExpression FILEPATH_EXP;
	protected static XPathExpression NAME_EXP;
	protected static XPathExpression NATIVE_DECL_EXP;
	protected static XPathExpression IMPL_EXP;
	protected static XPathExpression HOST_CALL_EXP;
	static {
		try {
			AbstractCodeSmellDetection.FILE_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.FILE_QUERY);
			AbstractCodeSmellDetection.C_FILES_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.C_FILES_QUERY);
			AbstractCodeSmellDetection.JAVA_FILES_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.JAVA_FILES_QUERY);
			AbstractCodeSmellDetection.LANGUAGE_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.LANGUAGE_QUERY);
			AbstractCodeSmellDetection.FUNC_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.FUNC_QUERY);
			AbstractCodeSmellDetection.CLASS_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.CLASS_QUERY);
			AbstractCodeSmellDetection.PACKAGE_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.PACKAGE_QUERY);
			AbstractCodeSmellDetection.FILEPATH_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.FILEPATH_QUERY);
			AbstractCodeSmellDetection.NAME_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.NAME_QUERY);
			AbstractCodeSmellDetection.NATIVE_DECL_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.NATIVE_DECL_QUERY);
			AbstractCodeSmellDetection.IMPL_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.IMPL_QUERY);
			AbstractCodeSmellDetection.HOST_CALL_EXP =
				AbstractCodeSmellDetection.xPath
					.compile(ICodeSmellDetection.HOST_CALL_QUERY);
		}
		catch (final XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<MLSCodeSmell> setOfSmells;

	public AbstractCodeSmellDetection() {
	}

	/**
	 * Sets the set of smells to the code smells detected in the
	 * document.
	 *
	 * @param	xml	srcML representation of the source code to
	 * analyze
	 */
	public abstract void detect(final Document xml);

	public String getCodeSmellName() {
		final String name = this.getName();
		return name.substring(0, name.length() - "Detection".length());
	}

	public Set<MLSCodeSmell> getCodeSmells() {
		return this.setOfSmells;
	}

	public String getHelpURL() {
		return "";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Adds a line for each code smell detected, starting with ID 0.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 */
	public void output(final PrintWriter aWriter) {
		this.output(aWriter, 0);
	}

	/**
	 * Adds a line for each code smell detected.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 * @param	count	ID number of the first code smell
	 */
	public void output(final PrintWriter aWriter, int count) {
		try {
			final Iterator<MLSCodeSmell> iter = this.getCodeSmells().iterator();
			while (iter.hasNext()) {
				final MLSCodeSmell codeSmell = iter.next();
				count++;
				aWriter.println("CS" + count + "," + codeSmell.toCSVLine());
			}
		}
		catch (final NumberFormatException e) {
			e.printStackTrace();
		}
	}

	protected void setSetOfSmells(final Set<MLSCodeSmell> setOfSmells) {
		this.setOfSmells = setOfSmells;
	}
}
