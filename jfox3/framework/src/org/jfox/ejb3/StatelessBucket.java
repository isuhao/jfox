/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.ejb3;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jfox.ejb3.dependent.FieldEJBDependence;
import org.jfox.ejb3.dependent.FieldResourceDependence;
import org.jfox.ejb3.timer.EJBTimerTask;
import org.jfox.ejb3.timer.TimeoutUtils;
import org.jfox.entity.dependent.FieldPersistenceContextDependence;
import org.jfox.entity.dependent.FieldPersistenceUnitDependence;
import org.jfox.framework.component.Module;
import org.jfox.util.AnnotationUtils;
import org.jfox.util.ClassUtils;
import org.jfox.util.MethodUtils;

import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jws.WebService;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Container of Statless EJB，store all Meta data, and as EJB Factory
 *
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class StatelessBucket extends SessionBucket implements PoolableObjectFactory {

    private EJBObjectId ejbObjectId;

    private StatelessEJBContextImpl statelessEJBContext;
    private EJBTimerService ejbTimerService;

    /**
     * Timeout Methods, invalid for stateful session bean
     */
    protected List<Method> timeoutMethods = new ArrayList<Method>();

    /**
     * cache EJB proxy stub, stateless EJB have only one stub
     */
    private EJBObject proxyStub = null;

    /**
     * cache EJB instances
     */
    private final GenericObjectPool pool = new GenericObjectPool(this);

    /**
     * Web Service 发布接口
     */
    private Class webServiceEndpointInterface = null;

    /**
     * \@WebService Annotation
     */
    private WebService wsAnnotation = null;

    public StatelessBucket(EJBContainer container, Class<?> beanClass, Module module) {
        super(container, beanClass, module);

        // Stateless/Stateful 不同的Annotation
        introspectStateless();

        injectClassDependents();
    }

    protected void introspectStateless() {
        Stateless stateless = getBeanClass().getAnnotation(Stateless.class);
        String name = stateless.name();
        if (name.equals("")) {
            name = getBeanClass().getSimpleName();
        }
        setEJBName(name);

        String mappedName = stateless.mappedName();
        if (mappedName.equals("")) {
            if (isRemote()) {
                addMappedName(name + "/remote");
            }
            if (isLocal()) {
                addMappedName(name + "/local");
            }
        }
        else {
            addMappedName(mappedName);
        }

        setDescription(stateless.description());


        //parse @WebService, simple parse @WebService
        if (getBeanClass().isAnnotationPresent(WebService.class)) {
            wsAnnotation = getBeanClass().getAnnotation(WebService.class);
            String endpointInterfaceName = wsAnnotation.endpointInterface();
            if (endpointInterfaceName == null || endpointInterfaceName.trim().length() == 0) {
                Class<?>[] beanInterfaces = this.getEJBInterfaces();
                if (beanInterfaces.length > 1) {
                    logger.warn("Use first Bean Interface " + beanInterfaces[0].getName() + " as endpoint interface.");

                }
                this.webServiceEndpointInterface = beanInterfaces[0];
            }
            else {
                try {
                    // 使用 ModuleClassLoader
                    Class endpointInterface = getModule().getModuleClassLoader().loadClass(endpointInterfaceName);
                    if (!endpointInterface.isInterface() || !Modifier.isPublic(endpointInterface.getModifiers())) {
                        logger.warn("Invalid endpoint interface: " + endpointInterface + " annotated in EJB bean class: " + getBeanClass().getName());
                    }
                    else {
                        this.webServiceEndpointInterface = endpointInterface;
                    }
                }
                catch (Exception e) {
                    logger.warn("Can not load endpoint interface: " + endpointInterfaceName + " annotated in EJB bean class: " + getBeanClass().getName(), e);
                }
            }
        }

        // beanClass is in superClass array
        Class<?>[] superClasses = ClassUtils.getAllSuperclasses(getBeanClass());

        List<Long> timeoutMethodHashes = new ArrayList<Long>();
        for (Class<?> superClass : superClasses) {

            for (Method timeoutMethod : introspectTimeoutMethod(superClass)) {
                long methodHash = MethodUtils.getMethodHash(timeoutMethod);
                if (!timeoutMethodHashes.contains(methodHash)) {
                    timeoutMethods.add(0, timeoutMethod);
                    timeoutMethodHashes.add(methodHash);
                }
            }
        }
        // timeout method
        if (TimedObject.class.isAssignableFrom(getBeanClass())) {
            timeoutMethods.add(TimeoutUtils.getTimeoutMethod());
        }
    }


    protected List<Method> introspectTimeoutMethod(Class superClass) {
        List<Method> timeoutMethods = new ArrayList<Method>();
        Method[] annotatedTimeoutMethods = AnnotationUtils.getAnnotatedDeclaredMethods(superClass, Timeout.class);
        for (Method timeoutMethod : annotatedTimeoutMethods) {
            if (checkTimeoutMethod(superClass, timeoutMethod, Timeout.class)) {
                timeoutMethod.setAccessible(true);
                timeoutMethods.add(0, timeoutMethod);
            }
        }
        return timeoutMethods;
    }
    
    protected boolean checkTimeoutMethod(Class<?> interceptorClass, Method timeoutMethod, Class<? extends Annotation> timeoutAnnotation) {
        if (!Modifier.isAbstract(timeoutMethod.getModifiers())
                && !Modifier.isStatic(timeoutMethod.getModifiers())
                && timeoutMethod.getParameterTypes().length == 1
                && timeoutMethod.getParameterTypes()[0].equals(Timer.class)) {
            return true;
        }
        else {
            logger.warn("Invalid @" + timeoutAnnotation.getSimpleName() + " method: " + timeoutMethod + " in class: " + interceptorClass);
            return false;
        }
    }

    /**
     * 从 Pool 中得到一个新的 Bean 实例
     *
     * @param ejbObjectId ejb object id
     * @throws EJBException exception
     */
    public ExtendEJBContext getEJBContext(EJBObjectId ejbObjectId) throws EJBException {
        try {
            EJBContextImpl ejbContext = (EJBContextImpl)pool.borrowObject();
            return ejbContext;
        }
        catch (Exception e) {
            throw new EJBException("Create EJBContext failed.", e);
        }
    }

    protected Method[] getTimeoutMethods() {
        return timeoutMethods.toArray(new Method[timeoutMethods.size()]);
    }

    /**
     * 将实例返回给 pool
     *
     * @param ejbContext ejb context
     */
    public void reuseEJBContext(ExtendEJBContext ejbContext) {
        try {
            pool.returnObject(ejbContext);
        }
        catch(Exception e){
            throw new EJBException("Return EJBContext to pool failed!", e);
        }
    }

    public ExtendEJBContext createEJBContext(EJBObjectId ejbObjectId, Object instance) {
        if (statelessEJBContext == null) {
            statelessEJBContext = new StatelessEJBContextImpl(ejbObjectId, instance);
        }
        return statelessEJBContext;
    }

    /**
     * 每个Stateless Bucket只有一个 EJBObjectId
     */
    public synchronized EJBObjectId createEJBObjectId() {
        if (ejbObjectId == null) {
            ejbObjectId = new EJBObjectId(getEJBName());
        }
        return ejbObjectId;
    }

    public Class getWebServiceEndpointInterface() {
        //TOOD: 获取 webServiceEndpointInterface
        return webServiceEndpointInterface;
    }

    public WebService getWebServiceAnnotation() {
        return wsAnnotation;
    }

    public boolean isStateful(){
        return false;
    }

    public boolean isWebService() {
        return getWebServiceAnnotation() != null;
    }

    /**
     * destroy bucket, invoke when container unload ejb
     */
    public void stop() {
        logger.debug("Destroy EJB: " + getEJBName() + ", Module: " + getModule().getName());
        try {
            pool.clear();
            // don't close pool, because of Bucket reusing
//            pool.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 生成基于动态代理的 Stub
     */
    public synchronized EJBObject createProxyStub() {
        if (proxyStub == null) {
            proxyStub = super.createProxyStub();
        }
        return proxyStub;
    }

    //--- jakarta commons-pool PoolableObjectFactory ---
    public Object makeObject() throws Exception {
        Object obj = getBeanClass().newInstance();
        ExtendEJBContext ejbContext = createEJBContext(createEJBObjectId(), obj);
        // post construct
        for (Method postConstructMethod : getPostConstructMethods()) {
            logger.debug("PostConstruct method for ejb: " + getEJBName() + ", method: " + postConstructMethod);
            postConstructMethod.invoke(ejbContext.getEJBInstance());
        }

        // 注入 @EJB
        for (FieldEJBDependence fieldEJBDependence : fieldEJBdependents) {
            fieldEJBDependence.inject(ejbContext);
        }

        // 注入 @Resource
        for (FieldResourceDependence fieldResourceDependence : fieldResourcedependents) {
            fieldResourceDependence.inject(ejbContext);
        }

        // 注入 @PersistenceContext
        for (FieldPersistenceContextDependence fieldPersistenceContextDependence : fieldPersistenceContextDependences) {
            fieldPersistenceContextDependence.inject(ejbContext);
        }

        // 注入 @PersistenceUnit
        for (FieldPersistenceUnitDependence persistenceUnitDependence : fieldPersistenceUnitDependences) {
            persistenceUnitDependence.inject(ejbContext);
        }
        
        //返回 EJBContext
        return ejbContext;
    }

    public boolean validateObject(Object obj) {
        return true;
    }

    public void activateObject(Object obj) throws Exception {
    }

    public void passivateObject(Object obj) throws Exception {
    }

    public void destroyObject(Object obj) throws Exception {
        for (Method preDestroyMethod : getPreDestroyMethods()) {
            logger.debug("PreDestory method for ejb: " + getEJBName() + ", method: " + preDestroyMethod);
            preDestroyMethod.invoke(((ExtendEJBContext)obj).getEJBInstance());
        }
    }

    // EJBContext Implementation
    @SuppressWarnings({"deprecation"})
    public class StatelessEJBContextImpl extends EJBContextImpl {

        public StatelessEJBContextImpl(EJBObjectId ejbObjectId, Object ejbInstance) {
            super(ejbObjectId, ejbInstance);
        }

        public TimerService getTimerService() throws IllegalStateException {
            if (ejbTimerService == null) {
                ejbTimerService = new EJBTimerService();
            }
            return ejbTimerService;
        }

        // SessionContext
        public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException {
            return (T)proxyStub;
        }

        // EJBObject & EJBLocalObject
        public void remove() throws RemoveException {
            try {
                destroyObject(getEJBInstance());
            }
            catch (Exception e) {
                String msg = "Remove EJB instance failed!";
                logger.warn(msg, e);
                throw new RemoveException(msg);
            }
        }
    }


    // EJB TimerService，only stateless, MDB, Entity can register TimerService
    @SuppressWarnings("unchecked")
    public class EJBTimerService implements TimerService {

        public Timer createTimer(final long duration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
            EJBTimerTask timer = (EJBTimerTask)getEJBContainer().getTimerService().createTimer(duration, info);
            timer.setEJBObjectId(createEJBObjectId());
            timer.addTimeoutMethod(getTimeoutMethods());
            timer.setSessionContext(EJBInvocation.current().getSessionContext());
            return timer;
        }

        public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
            EJBTimerTask timer = (EJBTimerTask)getEJBContainer().getTimerService().createTimer(expiration, info);
            timer.setEJBObjectId(createEJBObjectId());
            timer.addTimeoutMethod(getTimeoutMethods());
            timer.setSessionContext(EJBInvocation.current().getSessionContext());
            return timer;
        }

        public Timer createTimer(final long initialDuration, final long intervalDuration, final Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
            EJBTimerTask timer = (EJBTimerTask)getEJBContainer().getTimerService().createTimer(initialDuration, intervalDuration, info);
            timer.setEJBObjectId(createEJBObjectId());
            timer.addTimeoutMethod(getTimeoutMethods());
            timer.setSessionContext(EJBInvocation.current().getSessionContext());
            return timer;
        }

        public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
            EJBTimerTask timer = (EJBTimerTask)getEJBContainer().getTimerService().createTimer(initialExpiration, intervalDuration, info);
            timer.setEJBObjectId(createEJBObjectId());
            timer.addTimeoutMethod(getTimeoutMethods());
            timer.setSessionContext(EJBInvocation.current().getSessionContext());
            return timer;
        }

        public Collection getTimers() throws IllegalStateException, EJBException {
            List<EJBTimerTask> beanTimers = new ArrayList<EJBTimerTask>();
            for (EJBTimerTask timerTask : (Collection<EJBTimerTask>)getEJBContainer().getTimerService().getTimers()) {
                if (timerTask.getEJBObjectId().equals(createEJBObjectId())) {
                    beanTimers.add(timerTask);
                }
            }
            return Collections.unmodifiableCollection(beanTimers);
        }
    }

    public static void main(String[] args) {

    }
}
