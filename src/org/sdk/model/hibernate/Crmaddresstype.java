package org.sdk.model.hibernate;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "CRMADDRESSTYPE", catalog = "EBINEUTRINODB", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Crmaddresstype.findAll", query = "SELECT c FROM Crmaddresstype c"),
    @NamedQuery(name = "Crmaddresstype.findById", query = "SELECT c FROM Crmaddresstype c WHERE c.id = :id"),
    @NamedQuery(name = "Crmaddresstype.findByName", query = "SELECT c FROM Crmaddresstype c WHERE c.name = :name")})
public class Crmaddresstype implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "NAME")
    private String name;

    public Crmaddresstype() {}

    public Crmaddresstype(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Crmaddresstype)) {
            return false;
        }
        Crmaddresstype other = (Crmaddresstype) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ebiNeutrinoSDK.model.hibernate.Crmaddresstype[ id=" + id + " ]";
    }
    
}