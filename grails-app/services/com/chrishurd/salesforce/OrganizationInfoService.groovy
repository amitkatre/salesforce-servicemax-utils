package com.chrishurd.salesforce

class OrganizationInfoService {

    static scope = "session"
    static transactional = true

    def deleteOrganizationInfo(orgInfo) {
        orgInfo.delete(failOnError:true)
    }

    def getUserOrgs(user) {
        def orgList = OrganizationInfo.createCriteria().list {
            eq('user', user)
            order('name', 'asc')
        }

        return orgList
    }
}
