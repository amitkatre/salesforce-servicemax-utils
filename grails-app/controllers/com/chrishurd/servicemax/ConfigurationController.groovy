package com.chrishurd.servicemax

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class ConfigurationController {

    private static final log = LogFactory.getLog(this)

    def domainService
    def jsonService
    def transactionService
    def organizationInfoService
    def moduleService


    def index() {

        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())

        [orgList : orgList]
    }

    def load() {
        def orgInfo = organizationInfoService.getUserOrg(domainService.getUserDomain(), Long.valueOf(params.fromOrg))
        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())
        def types = params.getList('migrateWhat')
        def migrationObjects

        types.each { type ->
            if (type.equals("sfmTransaction")) {
                migrationObjects = transactionService.getSFMTransactions(orgInfo)
            }
            else if (type.equals("module")) {
                migrationObjects =  moduleService.getCustomModules(orgInfo)
            }
        }


        [orgInfo : orgInfo, migrationObjects : migrationObjects, orgList: orgList]
    }

    def save() {
        def id = params.objectId
        def type = params.type
        def fromOrg = organizationInfoService.getUserOrg(domainService.getUserDomain(),Long.valueOf(params.fromOrg))
        def toOrg = organizationInfoService.getUserOrg(domainService.getUserDomain(),Long.valueOf(params.toOrg))


        withFormat {
            json {
                def errors
                try {

                    def results
                    if ("sfmTransaction".equals(type)) {
                        results = transactionService.migrateSFMTransaction(fromOrg, toOrg, id)
                    }
                    else if ("module".equals(type)) {
                        results = moduleService.migrateModule(fromOrg, toOrg, id)
                    }

                    if (results instanceof Set<Object>) {
                        errors = results
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
