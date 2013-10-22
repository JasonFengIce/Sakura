package cn.ismartv.speedtester.facilities;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Exclude any field which doesn't have a {@link Export} annotation.
 * @author Bob
 */
public class ExportExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes field) {
		return field.getAnnotation(Export.class) == null;
	}

}
