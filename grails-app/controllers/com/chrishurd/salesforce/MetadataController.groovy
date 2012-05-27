package com.chrishurd.salesforce

class MetadataController {

    def domainService
    def metadataService

    def index() {

        def orgList = OrganizationInfo.createCriteria().list {
            eq('user', domainService.getUserDomain())
            order('name', 'asc')
        }


        [orgList : orgList]
    }

    def load() {
        def orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
        def metadataMap =  metadataService.describeMetadata(orgInfo)

        [orgInfo : orgInfo, metadataMap : metadataMap]
    }
}
