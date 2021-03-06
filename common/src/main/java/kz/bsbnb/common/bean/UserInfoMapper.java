package kz.bsbnb.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import kz.bsbnb.common.consts.DocType;
import kz.bsbnb.common.model.IPersistable;

import java.util.Date;

/**
 * Created by Olzhas.Pazyldayev on 02.08.2017.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoMapper implements IPersistable {
    private Long id;
    private String lastName; //"Иванов",
    private String firstName; //"Петр",
    private String middleName; //"Петрович",
    private String iin; //"123456789012",
    private String phone; //"887-54-8545",
    private String email; //"ritchie@mail.ru",
    private String status;
    private Boolean isOrg;
    private Boolean smsNotification;
    private Boolean emailNotification;
    private Date dateOfBirth;
    private String documentNumber;
    private String documentGivenAgency;
    private Date documentGivenDate;
    private Date documentExpireDate;
    private String documentType;



    public UserInfoMapper() {
    }

    public UserInfoMapper(String iin) {
        this.iin = iin;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getSmsNotification() {
        return smsNotification;
    }

    public void setSmsNotification(Boolean smsNotification) {
        this.smsNotification = smsNotification;
    }

    public Boolean getEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(Boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOrg() {
        return isOrg;
    }

    public void setOrg(Boolean org) {
        isOrg = org;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentGivenAgency() {
        return documentGivenAgency;
    }

    public void setDocumentGivenAgency(String documentGivenAgency) {
        this.documentGivenAgency = documentGivenAgency;
    }

    public Date getDocumentGivenDate() {
        return documentGivenDate;
    }

    public void setDocumentGivenDate(Date documentGivenDate) {
        this.documentGivenDate = documentGivenDate;
    }

    public Date getDocumentExpireDate() {
        return documentExpireDate;
    }

    public void setDocumentExpireDate(Date documentExpireDate) {
        this.documentExpireDate = documentExpireDate;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
