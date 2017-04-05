package cn.timd.Jelery.Annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME )
@Documented
@Inherited
public @interface Task {
    String name() default "AUTO";
}
