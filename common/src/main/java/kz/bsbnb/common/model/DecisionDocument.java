package kz.bsbnb.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.bsbnb.common.util.Constants;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by serik.mukashev on 11.12.2017.
 */
@Entity
@Table(name = "decision_document", schema = Constants.DB_SCHEMA_CORE)
public class DecisionDocument implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "voting_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "decision_document_voting_fk"))
    private Voting voting;
    @ManyToOne
    @JoinColumn(name = "voter_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "decision_document_voter_fk"))
    @JsonIgnore
    private Voter voter;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_voter_id",  referencedColumnName = "id", foreignKey = @ForeignKey(name = "decision_document_parent_voter_fk"))
    private Voter parentVoter;
    @Column(name = "json_document")
    @Size(max = 5_000_000)
    private String jsonDocument;
    @Column(name = "signature")
    @Size(max = 20_000)
    private String signature;
    @Column(name = "public_key")
    private String publicKey;
    @Column(name = "block_transaction_id", length = 1024)
    private String transactionId;
    @Column(name = "decision_document_hash", length = 2048)
    private String decisionDocumentHash;

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Voting getVoting() {
        return voting;
    }

    public void setVoting(Voting voting) {
        this.voting = voting;
    }

    public Voter getVoter() {
        return voter;
    }

    public void setVoter(Voter voter) {
        this.voter = voter;
    }

    public Voter getParentVoter() {
        return parentVoter;
    }

    public void setParentVoter(Voter parentVoter) {
        this.parentVoter = parentVoter;
    }

    public String getJsonDocument() {
        return jsonDocument;
    }

    public void setJsonDocument(String jsonDocument) {
        this.jsonDocument = jsonDocument;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDecisionDocumentHash() {
        return decisionDocumentHash;
    }

    public void setDecisionDocumentHash() {
        this.decisionDocumentHash = getMessageDigestFromDocument();
    }

    public String getMessageDigestFromDocument() {
        byte[] encodedhash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            encodedhash = digest.digest(jsonDocument.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        if(hash == null) return "";
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
