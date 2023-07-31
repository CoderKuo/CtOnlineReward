package cn.ctcraft.ctonlinereward.pojo.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigMapper {
    public String key() default "";

    public String setFunction() default "";

    public String getFunction() default "";
}
