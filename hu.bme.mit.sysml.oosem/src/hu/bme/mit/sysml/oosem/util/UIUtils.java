package hu.bme.mit.sysml.oosem.util;

import java.util.List;
import java.util.stream.Collectors;

import org.omg.sysml.lang.sysml.Type;

public class UIUtils {
	public static String getFormatedBlockListText(List<Type> blocks) {
		return String.join(", ", blocks
				.stream()
				.map(p -> p.getDeclaredName())
				.collect(Collectors.toList()));
	}
}
