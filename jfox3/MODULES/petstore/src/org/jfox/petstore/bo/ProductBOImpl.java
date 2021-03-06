/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package org.jfox.petstore.bo;

import java.sql.SQLException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jfox.petstore.dao.ProductDAO;
import org.jfox.petstore.entity.Product;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
@Stateless
@Local
public class ProductBOImpl implements ProductBO{

    @EJB
    ProductDAO productDAO;

    public List<Product> getProductsByCategory(String categoryId) {
        try {
            return productDAO.getProductListByCategory(categoryId);
        }
        catch(SQLException e) {
            throw new EJBException(e);
        }
    }


    public Product getProduct(String productId) {
        try {
            return productDAO.getProduct(productId);
        }
        catch(SQLException e) {
            throw new EJBException(e);
        }
    }

    public List<Product> searchProductList(String[] keywords) {
        try {
            String[] sqlKeyWords = new String[keywords.length];
            for(int i=0; i< keywords.length; i++){
                String sqlKW = "%" + keywords[i] + "%";
                sqlKeyWords[i] = sqlKW;
            }
            return productDAO.searchProductList(sqlKeyWords);
        }
        catch(SQLException e) {
            throw new EJBException(e);
        }
    }

    public static void main(String[] args) {

    }
}
