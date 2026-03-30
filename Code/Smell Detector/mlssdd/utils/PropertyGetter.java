

package mlssdd.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

final public class PropertyGetter {

	public static int getIntProp(final String prop, final int defaultValue) {
		final Properties props = new Properties();
		try {
			props.load(new FileInputStream("../rsc/config.properties"));
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		return Integer
			.parseInt(props.getProperty(prop, Integer.toString(defaultValue)));
	}

}
