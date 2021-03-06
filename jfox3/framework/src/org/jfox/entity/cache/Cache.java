/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.entity.cache;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public interface Cache {

    public String getName();

    public Collection<Serializable> keys();
    
    /**
     * cache对象
     *
     * @param key id
     * @param obj 需要 cache 的对象
     */
    public void put(Serializable key, Serializable obj);

    /**
     * 获得cache对象
     *
     * @param key id
     */
    public Serializable get(Serializable key);

    /**
     * 删除一个cache的对象
     *
     * @param key id
     * @return Serializable object removed
     */
    public Serializable remove(Serializable key);

    /**
     * 判断对象是否在cache中
     *
     * @param key id
     * @return true/false
     */
    public boolean contains(Serializable key);

    /**
     * 清空cache
     */
    public void clear();

    /**
     * 清除过期的对象
     */
    public void clean();

    /**
     * 获得 Cache 的状态
     */
    public CacheStat getStatus();

    /**
     * 获得 Cache 的Config
     */
    public CacheConfig getConfig();

}
