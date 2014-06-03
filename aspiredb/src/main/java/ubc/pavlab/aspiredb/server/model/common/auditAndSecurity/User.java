/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.server.model.common.auditAndSecurity;

import gemma.gsec.model.SecuredNotChild;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A user of the software system, who is authenticated. *
 * 
 * @author ?
 * @version $Id: User.java,v 1.9 2013/06/11 22:56:00 anton Exp $
 */
@Entity
@Table(name = "USER")
public class User implements SecuredNotChild {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    public User() {
    }

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "FIRSTNAME")
    private String firstName;

    @Column(name = "LASTNAME")
    private String lastName;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "PASSWORD_HINT")
    private String passwordHint;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "SIGNUP_TOKEN")
    private String signupToken;

    @Column(name = "SIGNUP_TOKEN_DATESTAMP")
    private java.util.Date signupTokenDatestamp;

    @Override
    public Long getId() {
        return this.id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public String getPasswordHint() {
        return this.passwordHint;
    }

    public void setPasswordHint( String passwordHint ) {
        this.passwordHint = passwordHint;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled( Boolean enabled ) {
        this.enabled = enabled;
    }

    public String getSignupToken() {
        return this.signupToken;
    }

    public void setSignupToken( String signupToken ) {
        this.signupToken = signupToken;
    }

    public java.util.Date getSignupTokenDatestamp() {
        return this.signupTokenDatestamp;
    }

    public void setSignupTokenDatestamp( java.util.Date signupTokenDatestamp ) {
        this.signupTokenDatestamp = signupTokenDatestamp;
    }

    @Override
    public boolean equals( Object object ) {
        if ( this == object ) {
            return true;
        }
        if ( !( object instanceof User ) ) {
            return false;
        }
        final User that = ( User ) object;
        if ( this.id == null || that.getId() == null || !this.id.equals( that.getId() ) ) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code based on this entity's identifiers.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode = 29 * hashCode + ( id == null ? 0 : id.hashCode() );

        return hashCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail( String email ) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

}