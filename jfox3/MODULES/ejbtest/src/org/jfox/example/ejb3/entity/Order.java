package org.jfox.example.ejb3.entity;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Column;

import net.sourceforge.jfox.entity.annotation.MappedColumn;
import net.sourceforge.jfox.entity.annotation.ParameterMap;

/**
 * @author <a href="mailto:yang_y@sysnet.com.cn">Young Yang</a>
 */
@Entity
public interface Order {

    @Column(name="id")
    Integer getId();
    void setId(Integer id);

    @Column(name = "createtime")
    String getCreateTime();
    void setCreateTime(String total);

    @MappedColumn(namedQuery = OrderDAOImpl.GET_LINEITEMS_BY_ORDER_ID, params = {@ParameterMap(name = "orderid", value="$this.getId()")})
    List<LineItem> getLineItems();
}
