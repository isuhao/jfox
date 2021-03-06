/*
 * JFox - The most lightweight Java EE Application Server!
 * more details please visit http://www.huihoo.org/jfox or http://www.jfox.org.cn.
 *
 * JFox is licenced and re-distributable under GNU LGPL.
 */
package jfox.test.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * @author <a href="mailto:jfox.young@gmail.com">Young Yang</a>
 */
@Entity
public class Address implements Serializable{
    
    @Column(name="ADR_ID")
    long id;

    @Column(name="ADR_ACC_ID")
    long accountId;

    @Column(name="ADR_DESCRIPTION")
    String description;

    @Column(name="ADR_STREET")
    String street;

    @Column(name="ADR_CITY")
    String city;

    @Column(name="ADR_PROVINCE")
    String province;

    @Column(name="ADR_POSTAL_CODE")
    String postalCode;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String toString() {
        return "Address{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", province='" + province + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
