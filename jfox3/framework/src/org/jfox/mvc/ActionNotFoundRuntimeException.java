/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package code.google.webactioncontainer;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class ActionNotFoundRuntimeException extends ActionRuntimeException {

    public ActionNotFoundRuntimeException(String actionName) {
        super("WebAction name: " + actionName);
    }

    public static void main(String[] args) {

    }
}
