package com.chrishurd.salesforce

import grails.converters.JSON

class MetadataController {

    def domainService
    def metadataService
    def jsonService
    def organizationInfoService


    def index() {

        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())

        [orgList : orgList]
    }

    def load() {
        def orgInfo = organizationInfoService.getUserOrgs(domainService.getUserDomain(), Long.valueOf(params.id))
        def metadataMap =  metadataService.describeAllMetadata(orgInfo)
        def loadedObjects = [] as Set<String>
        if (orgInfo.loadedObjects) {
            loadedObjects = orgInfo.loadedObjects.split('\\|')
        }

        [orgInfo : orgInfo, metadataMap : metadataMap, loadedObjects : loadedObjects]
    }

    def save() {
        withFormat {
            json {
                def loadedObjects = params.get('loadedObjects')
                def orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
                orgInfo.loadedObjects = loadedObjects.join('|')
                orgInfo.save(flush: true)

                metadataService.getMetadata(orgInfo, loadedObjects)


                render new JSON(jsonService.preparePostResponse(orgInfo))
            }
        }
    }
}
