/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.ejb3.interceptor;

import javax.ejb.EJBException;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * Interceptor Method anntated by @Interceptors on Class & Method
 *
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class InterceptorsInterceptorMethod implements InterceptorMethod {

    private Method interceptorMethod;

    private Object interceptorInstance;

    public InterceptorsInterceptorMethod(Class interceptorClass, Method interceptorMethod) {
        this.interceptorMethod = interceptorMethod;
        try {
            interceptorInstance = interceptorClass.newInstance();
        }
        catch(Exception e) {
            throw new EJBException("Could not create Interceptor class.", e);
        }
    }

    public Object invoke(InvocationContext invocationContext) throws Exception {
        return interceptorMethod.invoke(interceptorInstance,invocationContext);
    }

    public static void main(String[] args) {

    }
}
