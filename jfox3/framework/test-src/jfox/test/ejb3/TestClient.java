/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package jfox.test.ejb3;

import jfox.test.ejb3.entity.Order;
import jfox.test.ejb3.lob.Lobber;
import org.jfox.ejb3.naming.JNDIContextHelper;
import org.jfox.ejb3.security.JAASLoginServiceImpl;
import org.jfox.ejb3.security.SampleCallbackHandler;
import org.jfox.entity.mapping.EntityFactory;
import org.jfox.framework.Framework;
import org.jfox.mvc.SessionContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class TestClient {

    private static Framework framework;

    public static final String MODULE_NAME = "ejb3_exmple";

    @BeforeClass
    public static void setUp() throws Exception {
//        PlaceholderUtils.loadGlobalProperty(Constants.GLOBAL_PROPERTIES);
        framework = new Framework();
//        framework.loadModule(new File("MODULES/ejbtest"));
        framework.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("Waiting for tearDown...");
        Thread.sleep(2000);
        framework.stop();
        framework = null;
    }

    @Before
    public void beforeMethod() throws Exception {

    }

    @After
    public void afterMethod() throws Exception {

    }

    @Test
    public void invokeStateless() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.stateless.Calculator calculator = (jfox.test.ejb3.stateless.Calculator)context.lookup("stateless.CalculatorBean/remote");
        Assert.assertEquals(calculator.add(100, 1), 101);
        Assert.assertEquals(calculator.subtract(100, 1), 99);
    }

    @Test
    public void invokeEnv() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.env.Calculator calculator = (jfox.test.ejb3.env.Calculator)context.lookup("env.CalculatorBean/remote");
        calculator.remember(100);
        Assert.assertEquals(calculator.takeout(), 100);

        calculator.clear();
        try {
            Assert.assertEquals(calculator.takeout(), 100);
            Assert.fail("memory not clear!");
        }
        catch (EJBException e) {
            // expect ejbexception in calculator.takeout() 
        }
    }

    @Test
    public void invokeInjection() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.injection.ShoppingCart cart = (jfox.test.ejb3.injection.ShoppingCart)context.lookup("injection.ShoppingCartBean/remote");
        cart.buy("apple", 3);
        cart.buy("banana", 4);
        cart.buy("apple", 7);
        Map<String, Integer> contents = cart.getCartContents();
        Assert.assertEquals(contents.get("apple"), 10);
    }

    @Test
    public void invokeCallback() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.callback.Calculator calculator = (jfox.test.ejb3.callback.Calculator)context.lookup("callback.CalculatorBean/remote");
        Assert.assertEquals(calculator.add(100, 1), 101);
        Assert.assertEquals(calculator.subtract(100, 1), 99);

        ((EJBObject)calculator).remove();
    }

    @Test
    public void invokeInterceptor() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.interceptor.Calculator calculator = (jfox.test.ejb3.interceptor.Calculator)context.lookup("interceptor.CalculatorBean/remote");
        Assert.assertEquals(calculator.add(100, 1), 101);
        Assert.assertEquals(calculator.subtract(100, 1), 99);

        ((EJBObject)calculator).remove();
    }

    @Test
    public void invokeTimer() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.timer.ExampleTimer calculator = (jfox.test.ejb3.timer.ExampleTimer)context.lookup("timer.ExampleTimerBean/remote");
        calculator.scheduleTimer(500);
    }

    @Test
    public void invokeEntity() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.entity.OrderDAO orderDAO = (jfox.test.ejb3.entity.OrderDAO)context.lookup("entity.OrderDAO/remote");
        for (Order order : orderDAO.getOrders()) {
            System.out.println(order);
        }
    }

    @Test
    public void invokeLob() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.lob.LobberDAO lobberDAO = (jfox.test.ejb3.lob.LobberDAO)context.lookup("lob.LobberDAO/remote");
        Lobber lobber = EntityFactory.newEntityObject(Lobber.class);
        lobber.setId(1);
        String clobby = "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work" +
                "This is a very long string that will be stored in a java.sql.Clob hopefully.  We'll see how this works and if it will work";
        lobber.setClobby(clobby);
        lobber.setBlobby(clobby.getBytes());
        lobberDAO.insertLobber(lobber);

        lobber.setClobby("Hello,World!");
        lobberDAO.updateLobber(lobber);

        Lobber getLobber = lobberDAO.getLobber(1);
        Assert.assertTrue(Arrays.equals(getLobber.getBlobby(), lobber.getBlobby()));
    }

    @Test
    public void invokeStateful() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.stateful.ShoppingCart calculator = (jfox.test.ejb3.stateful.ShoppingCart)context.lookup("stateful.ShoppingCartBean/remote");
        calculator.buy("apple", 1);
        calculator.buy("banana", 2);
        Assert.assertEquals(calculator.getCartContents().size(), 2);

    }

    @Test
    public void invokeSynchronization() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.synchronization.ShoppingCart calculator = (jfox.test.ejb3.synchronization.ShoppingCart)context.lookup("sychronization.ShoppingCartBean/remote");
        calculator.buy("apple", 1);
        calculator.buy("banana", 2);
        Assert.assertEquals(calculator.getCartContents().size(), 2);

    }

    @Test
    public void invokeSecurity() throws Exception {
        // 新建 JAASLoginService实例，实际环境中有JFox内核负责实例化
        JAASLoginServiceImpl loginService = new JAASLoginServiceImpl();
        loginService.postInject();
        // 使用构造的 SessionContext，实际环境中将由HttpServletRequest负责提供
        loginService.login(SessionContext.getCurrentThreadSessionContext(), new SampleCallbackHandler(), "role1", "1234");
        Context context = JNDIContextHelper.getInitalContext();
        jfox.test.ejb3.security.Calculator calculator = (jfox.test.ejb3.security.Calculator)context.lookup("security.CalculatorBean/remote");
        Assert.assertEquals(calculator.add(100, 1), 101);
        Assert.assertEquals(calculator.devide(100, 1), 100d);
        try {
            Assert.assertEquals(calculator.subtract(100, 1), 99);
            Assert.fail("subtract not throw EJBAccessException");
        }
        catch(EJBAccessException e) {

        }

        try {
            Assert.assertEquals(calculator.plus(2,2),4);
            Assert.fail("plus not throw EJBAccessException");
        }
        catch(EJBAccessException e) {

        }

    }

    @Test
    public void invokeQueueMDB() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        QueueConnectionFactory connectionFactory = (QueueConnectionFactory)context.lookup("defaultcf");
        QueueConnection connection = connectionFactory.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("testQ");
        QueueSender sender = session.createSender(queue);
        int i=0;
        while(i++<10){
            sender.send(session.createTextMessage("Hello, JMS! " + System.currentTimeMillis()));
            Thread.sleep(1000);
        }
    }

    @Test
    public void invokeTopicMDB() throws Exception {
        Context context = JNDIContextHelper.getInitalContext();
        TopicConnectionFactory connectionFactory = (TopicConnectionFactory)context.lookup("defaultcf");
        TopicConnection connection = connectionFactory.createTopicConnection();
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic queue = session.createTopic("testT");
        TopicPublisher sender = session.createPublisher(queue);
        int i=0;
        while(i++<10){
            sender.send(session.createTextMessage("Hello, JMS! " + System.currentTimeMillis()));
            Thread.sleep(1000);
        }

    }

}
