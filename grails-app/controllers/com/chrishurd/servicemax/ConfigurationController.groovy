package com.chrishurd.servicemax

import com.chrishurd.salesforce.OrganizationInfo

class ConfigurationController {

    def configurationService

    def index() {

        def config = configurationService.getConfiguration(OrganizationInfo.get(2))

        [config : config]

    }
}
