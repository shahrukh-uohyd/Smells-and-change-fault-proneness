

package mlssdd.antipatterns.detection;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSAntiPattern;

public abstract class AbstractAntiPatternDetection {
	protected XPath xPath = XPathFactory.newInstance().newXPath();

	private Set<MLSAntiPattern> setOfAntiPatterns;

	/**
	 * Sets the set of anti-patterns to the anti-patterns detected in the
	 * document.
	 *
	 * @param	xml	srcML representation of the source code to
	 * analyze
	 */
	public abstract void detect(final Document xml);

	public String getAntiPatternName() {
		final String name = this.getName();
		return name.substring(0, name.length() - "Detection".length());
	}

	public Set<MLSAntiPattern> getAntiPatterns() {
		return this.setOfAntiPatterns;
	}

	public String getHelpURL() {
		return "";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Adds a line for each anti-pattern detected, starting with ID 0.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 */
	public void output(final PrintWriter aWriter) {
		this.output(aWriter, 0);
	}

	/**
	 * Adds a line for each anti-pattern detected.
	 *
	 * @param	aWriter	PrintWriter in which to add the line
	 * @param	count	ID number of the first anti-pattern
	 */
	public void output(final PrintWriter aWriter, int count) {
		try {
			final Iterator<MLSAntiPattern> iter =
				this.getAntiPatterns().iterator();
			while (iter.hasNext()) {
				final MLSAntiPattern antiPattern = iter.next();
				count++;
				aWriter.println("AP" + count + "," + antiPattern.toCSVLine());
			}
		}
		catch (final NumberFormatException e) {
			e.printStackTrace();
		}
	}

	protected void setSetOfAntiPatterns(
		final Set<MLSAntiPattern> setOfAntiPatterns) {
		this.setOfAntiPatterns = setOfAntiPatterns;
	}
}
