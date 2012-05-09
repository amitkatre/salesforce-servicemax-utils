package com.chrishurd.salesforce

class OrganizationInfoService {

    static transactional = true

    def deleteOrganizationInfo(orgInfo) {
        orgInfo.delete(failOnError:true)
    }
}
