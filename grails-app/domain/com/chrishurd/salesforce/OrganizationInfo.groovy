package com.chrishurd.salesforce

import com.chrishurd.security.User

class OrganizationInfo {

    String name
    String username
    String password
    String securityToken
    Boolean sandbox = false
    String loadedObjects

    static belongsTo = [
            user: User
    ]

    static mapping = {
        loadedObject type: 'text'
    }


    static constraints = {
        name nullable: false, blank: false, maxSize: 255
        username nullable: false, blank: false, maxSize:  255
        password nullable: false, blank: false, maxSize: 255
        securityToken nullable: true, maxSize: 64
        sandbox nullable: false
        loadedObjects nullable: true, blank: true, maxSize: 1000000
    }

    def getOrgPassword() {
        if (securityToken) {
            return "${password}${securityToken}"
        }
        else {
            return password
        }
    }
}
