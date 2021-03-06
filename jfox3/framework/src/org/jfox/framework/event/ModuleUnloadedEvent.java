/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.framework.event;

import org.jfox.framework.component.Module;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public class ModuleUnloadedEvent extends ModuleEvent {

    public ModuleUnloadedEvent(Module module) {
        super(module);
    }

}
