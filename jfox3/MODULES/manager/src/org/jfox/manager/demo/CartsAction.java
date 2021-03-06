/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.manager.demo;

import org.jfox.framework.annotation.ComponentBean;
import org.jfox.mvc.ActionContext;
import org.jfox.mvc.ActionSupport;
import org.jfox.mvc.PageContext;
import org.jfox.mvc.ParameterObject;
import org.jfox.mvc.SessionContext;
import org.jfox.mvc.annotation.ActionMethod;

import javax.ejb.EJB;
import java.util.ArrayList;

/**
 * carts actions
 *  
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
@ComponentBean(id="carts")
public class CartsAction extends ActionSupport {

    @EJB
    ICarts carts;

    @ActionMethod(name="view", successView = "demo/carts.fhtml", httpMethod = ActionMethod.HttpMethod.GET)
    public void doGetView(ActionContext actionContext) throws Exception{
        // do nothing, just forward to successView
    }

    @ActionMethod(name="submit", successView = "demo/carts.fhtml", parameterClass = CartParameterObject.class, httpMethod = ActionMethod.HttpMethod.POST)
    public void doPostSubmit(ActionContext actionContext) throws Exception {
        CartParameterObject invocation = (CartParameterObject)actionContext.getParameterObject();
        SessionContext sessionContext = actionContext.getSessionContext();
        ArrayList<String> carts = (ArrayList<String>)sessionContext.getAttribute("carts");
        if(carts == null) {
            carts = new ArrayList<String>();
            sessionContext.setAttribute("carts", carts);
        }

        if(invocation.getSubmit().equals("add")) {
            carts.add(invocation.getItem());
        }
        else if(invocation.getSubmit().equals("remove")){
            carts.remove(invocation.getItem());
        }
        PageContext pageContext = actionContext.getPageContext();
        pageContext.setAttribute("carts", carts);
    }

    public static class CartParameterObject extends ParameterObject {
        private String item;
        private String submit;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getSubmit() {
            return submit;
        }

        public void setSubmit(String submit) {
            this.submit = submit;
        }
    }

}
