package org.rill.bpm.api.scaleout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ScaleoutKeySource {
	
	enum RETRIEVE_TYPE {
		BO_ID, PROCESS_INSTANCE_ID, TASK_INSTANCE_ID
	}
	
	RETRIEVE_TYPE value();
	
	int index() default 0;
	
}
