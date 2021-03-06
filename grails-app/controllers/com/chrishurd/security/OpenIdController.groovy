package com.chrishurd.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.openid.OpenIdAuthenticationFailureHandler as OIAFH

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.savedrequest.DefaultSavedRequest

/**
 * Manages associating OpenIDs with application users, both by creating a new local user
 * associated with an OpenID and also by associating a new OpenID to an existing account.
 */
class OpenIdController {

    /** Dependency injection for daoAuthenticationProvider. */
    def daoAuthenticationProvider

    /** Dependency injection for OpenIDAuthenticationFilter. */
    def openIDAuthenticationFilter

    /** Dependency injection for the springSecurityService. */
    def springSecurityService

    static defaultAction = 'auth'

    /**
     * Shows the login page. The user has the choice between using an OpenID and a username
     * and password for a local account. If an OpenID authentication is successful but there
     * is no corresponding local account, they'll be redirected to createAccount to create
     * a new account, or click through to linkAccount to associate the OpenID with an
     * existing local account.
     */
    def auth = {

        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        [openIdPostUrl: "${request.contextPath}$openIDAuthenticationFilter.filterProcessesUrl",
                daoPostUrl: "${request.contextPath}${config.apf.filterProcessesUrl}",
                persistentRememberMe: config.rememberMe.persistent,
                rememberMeParameter: config.rememberMe.parameter,
                openidIdentifier: config.openid.claimedIdentityFieldName]
    }

    /**
     * Initially we're redirected here after a UserNotFoundException with a valid OpenID
     * authentication. This action is specified by the openid.registration.createAccountUri
     * attribute.
     * <p/>
     * The GSP displays the OpenID that was received by the external provider and keeps it
     * in the session rather than passing it between submits so the user has no opportunity
     * to change it.
     */
    def createAccount = { OpenIdRegisterCommand command ->

        String openId = session[OIAFH.LAST_OPENID_USERNAME]
        if (!openId) {
            flash.error = 'Sorry, an OpenID was not found'
            return [command: command]
        }

        if (!request.post && !createNewAccount(openId)) {
            // show the form
            command.clearErrors()
            copyFromAttributeExchange command
            return [command: command, openId: openId]
        }

        if (command.hasErrors()) {
            return [command: command, openId: openId]
        }

        authenticateAndRedirect openId
    }

    /**
     * The registration page has a link to this action so an existing user who successfully
     * authenticated with an OpenID can associate it with their account for future logins.
     */
    def linkAccount = { OpenIdLinkAccountCommand command ->

        String openId = session[OIAFH.LAST_OPENID_USERNAME]
        if (!openId) {
            flash.error = 'Sorry, an OpenID was not found'
            return [command: command]
        }

        if (!request.post) {
            // show the form
            command.clearErrors()
            return [command: command, openId: openId]
        }

        if (command.hasErrors()) {
            return [command: command, openId: openId]
        }

        try {
            registerAccountOpenId openId
        }
        catch (AuthenticationException e) {
            flash.error = 'Sorry, no user was found with that username and password'
            return [command: command, openId: openId]
        }

        authenticateAndRedirect openId
    }

    /**
     * Authenticate the user for real now that the account exists/is linked and redirect
     * to the originally-requested uri if there's a SavedRequest.
     *
     * @param username the user's login name
     */
    private void authenticateAndRedirect(String username) {
        session.removeAttribute OIAFH.LAST_OPENID_USERNAME
        session.removeAttribute OIAFH.LAST_OPENID_ATTRIBUTES

        springSecurityService.reauthenticate username

        def config = SpringSecurityUtils.securityConfig

        def savedRequest = session[DefaultSavedRequest.SPRING_SECURITY_SAVED_REQUEST_KEY]
        if (savedRequest && !config.successHandler.alwaysUseDefault) {
            redirect url: savedRequest.redirectUrl
        }
        else {
            redirect uri: config.successHandler.defaultTargetUrl
        }
    }

    /**
     * Create the user instance and grant any roles that are specified in the config
     * for new users.
     * @param username the username
     * @param password the password
     * @param openId the associated OpenID
     * @return true if successful
     */
    private boolean createNewAccount(String openId) {
        boolean created = User.withTransaction { status ->
            def config = SpringSecurityUtils.securityConfig

            def password = springSecurityService.encodePassword('password')
            def user = new User(username: openId, password: password, enabled: true)

            user.addToOpenIds(url: openId)

            if (!user.save()) {
                return false
            }

            for (roleName in config.openid.registration.roleNames) {
                UserRole.create user, Role.findByAuthority(roleName)
            }
            return true
        }
        return created
    }

    /**
     * Associates an OpenID with an existing account. Needs the user's password to ensure
     * that the user owns that account, and authenticates to verify before linking.
     * @param username the username
     * @param password the password
     * @param openId the associated OpenID
     */
    private void registerAccountOpenId(String openId) {
        // check that the user exists, password is valid, etc. - doesn't actually log in or log out,
        // just checks that user exists, password is valid, account not locked, etc.
        daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(openId, 'password'))

        User.withTransaction { status ->
            def user = User.findByUsername(username)
            user.addToOpenIds(url: openId)
            if (!user.validate()) {
                status.setRollbackOnly()
            }
        }
    }

    /**
     * For the initial form display, copy any registered AX values into the command.
     * @param command the command
     */
    private void copyFromAttributeExchange(OpenIdRegisterCommand command) {
        List attributes = session[OIAFH.LAST_OPENID_ATTRIBUTES] ?: []
        for (attribute in attributes) {
            // TODO document
            String name = attribute.name
            if (command.hasProperty(name)) {
                command."$name" = attribute.values[0]
            }
        }
    }
}

class OpenIdRegisterCommand {

    String username = ""

    static constraints = {
        username blank: true, email: true, validator: { String username, command ->
            User.withNewSession { session ->
                if (username && User.countByUsername(username)) {
                    return 'openIdRegisterCommand.username.error.unique'
                }
            }
        }
    }
}

class OpenIdLinkAccountCommand {

    String username = ""

    static constraints = {
        username blank: false, email: true
    }
}
