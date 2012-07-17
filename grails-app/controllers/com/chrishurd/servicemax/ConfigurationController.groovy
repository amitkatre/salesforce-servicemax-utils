package com.chrishurd.servicemax

import com.chrishurd.salesforce.OrganizationInfo
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class ConfigurationController {

    private static final log = LogFactory.getLog(this)

    def domainService
    def jsonService
    def transactionService
    def organizationInfoService


    def index() {

        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())

        [orgList : orgList]
    }

    def load() {
        def orgInfo = OrganizationInfo.get(Long.valueOf(params.fromOrg))
        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())
        def types = params.getList('migrateWhat')
        def orgConfig = new SFMConfiguration()

        types.each { type ->
            if (type.equals("sfmTransaction")) {
                orgConfig.sfmTransactions = transactionService.getSFMTransactions(orgInfo)
            }
        }

        [orgInfo : orgInfo, orgConfig : orgConfig, orgList: orgList]
    }

    def save() {
        def id = params.objectId
        def type = params.type
        def fromOrg = OrganizationInfo.get(Long.valueOf(params.fromOrg))
        def toOrg = OrganizationInfo.get(Long.valueOf(params.toOrg))


        withFormat {
            json {
                def errors
                try {

                    if ("sfmTransaction".equals(type)) {
                        def results = transactionService.migrateSFMTransaction(fromOrg, toOrg, id)
                        if (results instanceof Set<Object>) {
                            errors = results
                        }
                    }
                }
                catch (Exception ex) {
                    log.error("save", ex)
                    render new JSON(jsonService.prepareErrorPostResponse(ex.toString()))
                    return
                }



                if (! errors || errors.isEmpty()) {
                    render new JSON([success : 'true'])
                }
                else {
                    render new JSON(jsonService.prepareErrorPostResponse(errors))
                }


            }
        }



    }
}
