package tv.ismar.sakura.core.client;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by huaijie on 1/12/16.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Chunked {
}
