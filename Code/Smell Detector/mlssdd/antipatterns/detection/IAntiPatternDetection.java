

package mlssdd.antipatterns.detection;

import java.io.PrintWriter;
import java.util.Set;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSAntiPattern;

public interface IAntiPatternDetection {

	final String C_FILES_QUERY = "//unit[@language='C++' or @language='C']";
	final String JAVA_FILES_QUERY = "//unit[@language='Java']";
	final String CLASS_QUERY = "class/name | interface/name";
	final String PACKAGE_QUERY = "package/name | namespace/name"; // Call on unit
	final String FILEPATH_QUERY = "@filename"; // Call on unit
	// final String NATIVE_QUERY =
	// 	"descendant::function_decl[specifier='native']/name";
	final String NATIVE_QUERY =
		"descendant::function_decl[type/specifier='native']";

	void detect(final Document xml);

	String getAntiPatternName();

	// TODO: We may have a vocabulary problem:
	// Do we detect antipatterns or rather micro-architectures
	// similar to some anti-patterns? Similarly to what happens
	// with design patterns...
	Set<MLSAntiPattern> getAntiPatterns();

	String getName();

	void output(final PrintWriter aWriter);

	void output(final PrintWriter aWriter, int count);
}
