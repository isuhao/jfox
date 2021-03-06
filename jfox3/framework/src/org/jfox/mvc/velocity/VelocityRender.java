/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package code.google.webactioncontainer.velocity;

import code.google.webactioncontainer.WebContextLoader;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.IncludeRelativePath;
import org.apache.velocity.context.Context;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;
import code.google.webactioncontainer.PageContext;
import code.google.webactioncontainer.Render;
import code.google.webactioncontainer.servlet.ControllerServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Velocity Render
 *
 * handle .vm .vhtml .vhtm .tmpl
 *
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class VelocityRender implements Render {

    public final static String VELOCITY_PROPERTIES = "velocity.properties";
    private String inputEncoding = "UTF-8";
    private String outputEncoding = "UTF-8";

    /**
     * The HTTP content type context key.
     */
    public static final String CONTENT_TYPE = "default.contentType";

    /**
     * The default content type for the response
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/html";


    /**
     * Encoding for the output stream
     */
    public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";

    /**
     * The default content type, itself defaulting to {@link
     * #DEFAULT_CONTENT_TYPE} if not configured.
     */
    private static String defaultContentType;

    /**
     * Cache of writers
     */
    private final static SimplePool writerPool = new SimplePool(40);

    private static final EventCartridge ec = new EventCartridge();

    private VelocityEngine velocityEngine = null;

    static {
        //#include #parse 支持相对路径
        ec.addEventHandler(new IncludeRelativePath());
    }


    /**
     * Performs initialization of this servlet.  Called by the servlet
     * container on loading.
     *
     * @param config The servlet configuration to apply.
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        /*
         *  do whatever we have to do to init Velocity
         */
        initVelocity(config);

        /*
         *  Now that Velocity is initialized, cache some config.
         */
        defaultContentType = RuntimeSingleton.getString(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
    }

    protected void initVelocity(ServletConfig servletConfig) throws ServletException {
        try {
            String templateBaseDir = servletConfig.getServletContext().getRealPath(".");
            Properties p = loadConfiguration(servletConfig);
            // use loader path by module
            p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templateBaseDir);
            // overwrite velocity default avalog log system
            p.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4jLogSystem.class.getName());

            //overwrite encoding by ControllerServet
            p.setProperty(Velocity.INPUT_ENCODING, WebContextLoader.getEncoding());
            p.setProperty(Velocity.OUTPUT_ENCODING, WebContextLoader.getEncoding());
            inputEncoding = p.getProperty(Velocity.INPUT_ENCODING);
            outputEncoding = p.getProperty(Velocity.OUTPUT_ENCODING);

            velocityEngine = new VelocityEngine();
            velocityEngine.init(p);
        }
        catch (Exception e) {
            throw new ServletException("Error initializing Velocity: " + e, e);
        }
    }

    /**
     * 从 velocity.properties 中装载 velocity 的配置
     *
     * @param config servlet config
     * @throws IOException io exception
     */
    protected Properties loadConfiguration(ServletConfig config) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(VELOCITY_PROPERTIES);
        Properties p = new Properties();
        p.load(in);
        in.close();
        return p;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Context context = null;
        try {
            /*
             *  first, get a context
             */

            context = createVelocityContext(request, response);

            /*
             *   set the content type
             */

            setContentType(request, response);

            /*
             *  let someone handle the request
             */

            Template template = createTemplate(request, response, context);
            /*
             *  bail if we can't find the template
             */

            if (template == null) {
                return;
            }

            /*
             *  now merge it
             */

            mergeTemplate(template, context, response);
        }
        catch (Exception e) {
            /*
             *  call the error handler to let the derived class
             *  do something useful with this failure.
             */

            error(request, response, e);
        }
        finally {
            /*
             *  call cleanup routine to let a derived class do some cleanup
             */

            afterRender(request, response, context);
        }

    }

    /**
     * create a valid Template for velocity
     * </p>
     *
     * @param request         http request
     * @param response        http response
     * @param velocityContext a Velocity Context object to be filled with data. Will be used
     *                        for rendering this template
     * @return Template to be used for request
     * @throws Exception exception
     */
    protected Template createTemplate(HttpServletRequest request, HttpServletResponse response, Context velocityContext) throws Exception {
        String servletPath = request.getServletPath();
        return velocityEngine.getTemplate(servletPath, inputEncoding);
    }

    /**
     * merges the template with the context.  Only override this if you really, really
     * really need to. (And don't call us with questions if it breaks :)
     *
     * @param template template object returned by the handleRequest() method
     * @param context  context created by the createContext() method
     * @param response servlet reponse (use this to get the output stream or Writer
     * @throws Exception exception
     */
    protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
        ServletOutputStream output = response.getOutputStream();
        // ASSUMPTION: response.setContentType() has been called.
        String encoding = response.getCharacterEncoding();

        VelocityWriter vw = null;
        try {
            vw = (VelocityWriter)writerPool.get();

            if (vw == null) {
                vw = new VelocityWriter(new OutputStreamWriter(output,encoding), 4 * 1024, true);
            }
            else {
                vw.recycle(new OutputStreamWriter(output, encoding));
            }

            template.merge(context, vw);
        }
        finally {
            try {
                if (vw != null) {
                    /*
                     *  flush and put back into the pool
                     *  don't close to allow us to play
                     *  nicely with others.
                     */
                    vw.flush();

                    /*
                     * Clear the VelocityWriter's reference to its
                     * internal OutputStreamWriter to allow the latter
                     * to be GC'd while vw is pooled.
                     */
                    vw.recycle(null);

                    writerPool.put(vw);
                }
            }
            catch (Exception e) {
                // do nothing
            }
        }
    }

    /**
     * Sets the content type of the response, defaulting to {@link
     * #defaultContentType} if not overriden.
     *
     * @param request  The servlet request from the client.
     * @param response The servlet reponse to the client.
     */
    protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
        String contentType = defaultContentType;
        int index = contentType.lastIndexOf(';') + 1;
        if (index <= 0 || (index < contentType.length() &&
                contentType.indexOf("charset", index) == -1)) {
            // Append the character encoding which we'd like to use.
            String encoding = outputEncoding;
            //System.out.println("Chose output encoding of '" +
            //                   encoding + '\'');
            if (!DEFAULT_OUTPUT_ENCODING.equalsIgnoreCase(encoding)) {
                contentType += "; charset=" + encoding;
            }
        }
        response.setContentType(contentType);
        //System.out.println("Response Content-Type set to '" +
        //                   contentType + '\'');
    }

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *
     * @param request  original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param cause    Exception that was thrown by some other part of process.
     * @throws IOException      io exception
     * @throws ServletException servlet exception
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause) throws ServletException, IOException {
        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<title>Error</title>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>VelocityServlet: Error processing the template</h2>");
        html.append("<pre>");
        String why = cause.getMessage();
        if (why != null && why.trim().length() > 0) {
            html.append(why);
            html.append("<br>");
        }

        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));

        html.append(sw.toString());
        html.append("</pre>");
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print(html.toString());
    }


    /**
     * create VelocityContext and set key/value
     *
     * @param request  http request
     * @param response http response
     */
    protected Context createVelocityContext(HttpServletRequest request, HttpServletResponse response) {
        PageContext pageContext = (PageContext)request.getAttribute(ControllerServlet.PAGE_CONTEXT);
        VelocityContext velocityContext = new VelocityContext(pageContext.getResultMap());
        // 使用相对路径
        ec.attachToContext(velocityContext);
        return velocityContext;
    }

    protected void afterRender(HttpServletRequest request, HttpServletResponse response, Context velocityContext) {

    }

    public static void main(String[] args) {

    }
}
