
package mlssdd.codesmells.detection;

import java.io.PrintWriter;
import java.util.Set;
import org.w3c.dom.Document;

import mlssdd.kernel.impl.MLSCodeSmell;

//import util.help.IHelpURL;

public interface ICodeSmellDetection /* extends IHelpURL */ {
	final String FILE_QUERY = "//unit";

	final String C_FILES_QUERY = "//unit[@language='C++' or @language='C']";

	final String JAVA_FILES_QUERY = "//unit[@language='Java']";

	final String LANGUAGE_QUERY = "@language"; // Call on unit
	final String FUNC_QUERY = "ancestor::function/name | ancestor::constructor/name";
	final String CLASS_QUERY =
		"ancestor::class/name | ancestor::interface/name";
	final String PACKAGE_QUERY = "ancestor::unit/package/name | ancestor::unit/namespace/name";
	final String FILEPATH_QUERY = "@filename"; // Call on unit
	final String NAME_QUERY = "name";
	final String NATIVE_DECL_QUERY =
		"descendant::function_decl[type/specifier='native']/name";
	final String IMPL_QUERY = "descendant::function/name";
	final String HOST_CALL_QUERY = "descendant::call//name[last()]";
	void detect(final Document xml);
	String getCodeSmellName();
	Set<MLSCodeSmell> getCodeSmells();

	String getName();

	void output(final PrintWriter aWriter);

	void output(final PrintWriter aWriter, int count);
}
