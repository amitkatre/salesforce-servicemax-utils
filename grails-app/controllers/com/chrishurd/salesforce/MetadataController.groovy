package com.chrishurd.salesforce

import grails.converters.JSON

class MetadataController {

    def domainService
    def metadataService
    def jsonService


    def index() {

        def orgList = OrganizationInfo.createCriteria().list {
            eq('user', domainService.getUserDomain())
            order('name', 'asc')
        }


        [orgList : orgList]
    }

    def load() {
        def orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
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
