package com.chrishurd.salesforce


/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/10/12
 * Time: 6:39 PM
 * To change this template use File | Settings | File Templates.
 */
class SFConnection extends com.sforce.ws.ConnectorConfig {

    def connection

    def SFConnection(OrganizationInfo orgInfo) {
        this.setUsername(orgInfo.username);
        this.setPassword(orgInfo.getOrgPassword());
        if (orgInfo.sandbox) {
            this.setAuthEndpoint('https://test.salesforce.com/services/Soap/u/24.0')
        }
        else {
            this.setAuthEndpoint('https://login.salesforce.com/services/Soap/u/24.0')
        }
        this.setReadTimeout(600000)
    }

}
