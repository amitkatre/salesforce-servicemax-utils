package com.chrishurd



class MainController {

    def springSecurityService

    def index = {
        if (! springSecurityService.isLoggedIn()) {
            redirect( controller: 'openId', action: 'auth')
            return
        }
    }
}
