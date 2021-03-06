/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package code.google.webactioncontainer.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Yang Yong</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FloatValidation {

    String errorId();
    
    /**
     * default float validator class
     */
    Class<? extends Validator> validatorClass() default FloatValidator.class;


    /**
     * min value
     */
    float minValue() default Float.MIN_VALUE;

    /**
     * max value
     */
    float maxValue() default Float.MAX_VALUE;

    /**
     * nullable
     */
    boolean nullable() default false;
}
