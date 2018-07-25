/*
 * ParticipAct
 * Copyright 2013-2018 Alma Mater Studiorum – Università di Bologna
 * This file is part of ParticipAct.
 * ParticipAct is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.
 * ParticipAct is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with ParticipAct. If not, see <http://www.gnu.org/licenses/>.
 */

package it.unibo.participact.domain.local;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;

import it.unibo.participact.domain.enums.DocumentIdType;
import it.unibo.participact.domain.enums.Gender;
import it.unibo.participact.domain.enums.UniCity;
import it.unibo.participact.domain.enums.UniCourse;
import it.unibo.participact.domain.enums.UniSchool;

public class User implements Serializable {

    private static final long serialVersionUID = -7480986502756652528L;

    private Long id;

    private String name;

    private String surname;

    private Gender gender;

    private LocalDate birthdate;

    private DocumentIdType documentIdType;

    private String documentId;

    private String domicileAddress;
    private String domicileZipCode;
    private String domicileCity;
    private String domicileProvince;

    private String currentAddress;

    private String currentZipCode;

    private String currentCity;

    private String currentProvince;

    private String contactPhoneNumber;

    private String homePhoneNumber;

    private String projectPhoneNumber;

    private String officialEmail;

    private String projectEmail;

    private UniCity uniCity;

    private UniSchool uniSchool;

    private String uniDepartment;

    private String uniDegree;

    private UniCourse uniCourse;

    private Boolean uniIsSupplementaryYear;

    private Integer uniYear;

    private String imei;

    private String password;

    private DateTime registrationDateTime;

    private Account createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public DocumentIdType getDocumentIdType() {
        return documentIdType;
    }

    public void setDocumentIdType(DocumentIdType documentIdType) {
        this.documentIdType = documentIdType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDomicileAddress() {
        return domicileAddress;
    }

    public void setDomicileAddress(String domicileAddress) {
        this.domicileAddress = domicileAddress;
    }

    public String getDomicileZipCode() {
        return domicileZipCode;
    }

    public void setDomicileZipCode(String domicileZipCode) {
        this.domicileZipCode = domicileZipCode;
    }

    public String getDomicileCity() {
        return domicileCity;
    }

    public void setDomicileCity(String domicileCity) {
        this.domicileCity = domicileCity;
    }

    public String getDomicileProvince() {
        return domicileProvince;
    }

    public void setDomicileProvince(String domicileProvince) {
        this.domicileProvince = domicileProvince;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getCurrentZipCode() {
        return currentZipCode;
    }

    public void setCurrentZipCode(String currentZipCode) {
        this.currentZipCode = currentZipCode;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getCurrentProvince() {
        return currentProvince;
    }

    public void setCurrentProvince(String currentProvince) {
        this.currentProvince = currentProvince;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getHomePhoneNumber() {
        return homePhoneNumber;
    }

    public void setHomePhoneNumber(String homePhoneNumber) {
        this.homePhoneNumber = homePhoneNumber;
    }

    public String getProjectPhoneNumber() {
        return projectPhoneNumber;
    }

    public void setProjectPhoneNumber(String projectPhoneNumber) {
        this.projectPhoneNumber = projectPhoneNumber;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public void setOfficialEmail(String officialEmail) {
        this.officialEmail = officialEmail;
    }

    public String getProjectEmail() {
        return projectEmail;
    }

    public void setProjectEmail(String projectEmail) {
        this.projectEmail = projectEmail;
    }

    public UniCity getUniCity() {
        return uniCity;
    }

    public void setUniCity(UniCity uniCity) {
        this.uniCity = uniCity;
    }

    public UniSchool getUniSchool() {
        return uniSchool;
    }

    public void setUniSchool(UniSchool uniSchool) {
        this.uniSchool = uniSchool;
    }

    public String getUniDepartment() {
        return uniDepartment;
    }

    public void setUniDepartment(String uniDepartment) {
        this.uniDepartment = uniDepartment;
    }

    public String getUniDegree() {
        return uniDegree;
    }

    public void setUniDegree(String uniDegree) {
        this.uniDegree = uniDegree;
    }

    public UniCourse getUniCourse() {
        return uniCourse;
    }

    public void setUniCourse(UniCourse uniCourse) {
        this.uniCourse = uniCourse;
    }

    public Boolean getUniIsSupplementaryYear() {
        return uniIsSupplementaryYear;
    }

    public void setUniIsSupplementaryYear(Boolean uniIsSupplementaryYear) {
        this.uniIsSupplementaryYear = uniIsSupplementaryYear;
    }

    public Integer getUniYear() {
        return uniYear;
    }

    public void setUniYear(Integer uniYear) {
        this.uniYear = uniYear;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DateTime getRegistrationDateTime() {
        return registrationDateTime;
    }

    public void setRegistrationDateTime(DateTime registrationDateTime) {
        this.registrationDateTime = registrationDateTime;
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }
}
