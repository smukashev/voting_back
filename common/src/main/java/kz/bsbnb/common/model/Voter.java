/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.bsbnb.common.util.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ruslan
 */
@Entity
@Table(name = "voter", schema = Constants.DB_SCHEMA_CORE)
public class Voter implements Serializable {

    private static final long serialVersionUID = 5847549508691885490L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "date_adding")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdding;
    @OneToMany(mappedBy = "voter")
    private List<Share> shares;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "voter", fetch = FetchType.EAGER)
    private Set<Decision> decisionSet;
    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "voter_user_fk"))
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;
    @JoinColumn(name = "voting_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "voter_voting_fk"))
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Voting voting;
    @Column(name = "is_shared")
    private Boolean isShared;
    @OneToOne
    @JoinColumn(name = "parent_voter_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "voter_parent_voter_fk"))
    private Voter parentVoter;


    public Voter() {
    }

    public Voter(Long id) {
        this.id = id;
    }

    public List<Share> getShares() {
        return shares;
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateAdding() {
        return dateAdding;
    }

    public void setDateAdding(Date dateAdding) {
        this.dateAdding = dateAdding;
    }

    @XmlTransient
    public Set<Decision> getDecisionSet() { return decisionSet;}

    public void setDecisionSet(Set<Decision> decisionSet) { this.decisionSet = decisionSet;}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Voting getVoting() {
        return voting;
    }

    public void setVoting(Voting voting) {
        this.voting = voting;
    }

    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    public Voter getParentVoter() {
        return parentVoter;
    }

    public void setParentVoter(Voter parentVoter) {
        this.parentVoter = parentVoter;
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
        if (!(object instanceof Voter)) {
            return false;
        }
        Voter other = (Voter) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "kz.bsbnb.common.model.Voter[ id=" + id + " ]";
    }

    public void addShare(Share share) {
        List<Share> result = getShares();
        if(result == null) {
            result = new ArrayList<>();
        }
        result.add(share);
        setShares(result);
    }
    
}
