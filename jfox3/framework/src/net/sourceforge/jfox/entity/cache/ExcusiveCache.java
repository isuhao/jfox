package net.sourceforge.jfox.entity.cache;

/**
 * //TODO: 独占式 Cache，在一个Cache对象被使用时，不允许其它线程使用
 *
 * @author <a href="mailto:yang_y@sysnet.com.cn">Young Yang</a>
 */
public class ExcusiveCache extends SimpleCache {

    public ExcusiveCache(CacheConfig config) {
        super(config);
    }

    public static void main(String[] args) {

    }
}