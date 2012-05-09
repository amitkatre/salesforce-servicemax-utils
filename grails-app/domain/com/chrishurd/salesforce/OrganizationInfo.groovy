package com.chrishurd.salesforce

import com.chrishurd.security.User

class OrganizationInfo {

    String name
    String username
    String password
    String securityToken
    Boolean sandbox = false

    static belongsTo = [
            user: User
    ]


    static constraints = {
        name nullable: false, blank: false, maxSize: 255
        username nullable: false, blank: false, maxSize:  255
        password nullable: false, blank: false, maxSize: 255
        securityToken nullable: true, maxSize: 64
        sandbox nullable: false
    }
}
