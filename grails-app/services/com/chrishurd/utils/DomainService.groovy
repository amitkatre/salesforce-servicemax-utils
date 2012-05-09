package com.chrishurd.utils

import com.chrishurd.security.User

class DomainService {

    def springSecurityService

    def getUserDomain() {
        def user = User.findByUsername(springSecurityService.principal.username)
        return user;
    }
}
