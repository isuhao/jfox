package org.jfox.ejb3.security;

import java.io.IOException;
import java.util.Arrays;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author <a href="mailto:yang_y@sysnet.com.cn">Young Yang</a>
 */
public class SampleCallbackHandler implements CallbackHandler {

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        System.out.println("SampleCallbackHandler.handle: " + Arrays.toString(callbacks));
    }

    public static void main(String[] args) {

    }
}
