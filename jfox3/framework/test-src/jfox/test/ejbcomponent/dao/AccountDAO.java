package jfox.test.ejbcomponent.dao;

import java.sql.SQLException;
import java.util.List;

import org.jfox.entity.MappedEntity;
import jfox.test.jpa.Account;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
public interface AccountDAO {

    Account getAccountById(long id) throws SQLException;

    Account getAccountByIdSQL(long id) throws SQLException;

    MappedEntity getAccountMappedEntityById(long id) throws SQLException;

    List<Account> getAllAccounts() throws SQLException;

    Account createAccount(String name, String lastname, String mail) throws SQLException;
}
