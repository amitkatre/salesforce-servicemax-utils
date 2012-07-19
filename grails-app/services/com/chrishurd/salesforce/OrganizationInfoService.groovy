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

    def getUserOrg(user, id) {
        def orgList = OrganizationInfo.createCriteria().list {
            eq('user', user)
            eq('id', id)
            order('name', 'asc')
        }

        if (orgList.size() > 0) {
            return orgList.get(0)
        }
        else {
            throw new Exception("Unable to find the desired org")
        }
    }
}
