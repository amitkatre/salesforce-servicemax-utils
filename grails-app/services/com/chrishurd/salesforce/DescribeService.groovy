package com.chrishurd.salesforce

class DescribeService {

    def connectionService

    def getAllObjects(orgInfo) {

        def objects = [] as Set<String>

        def connection =  connectionService.getPartnerConnection(orgInfo)
        def descrProcesses = connection.describeGlobal()

        for (def sObject : descrProcesses.getSobjects()) {
            objects.add(sObject.getName())
        }

        return objects

    }

    def getAllEditableFields(orgInfo, objectName) {

        def connection = connectionService.getPartnerConnection(orgInfo)

        def descrProcesses = connection.describeSObject(objectName);

        def fields = [] as Set<String>

        descrProcesses.getFields().each { field ->
            if (field.isUpdateable()) {
                fields.add(field.getName())
            }
        }

        return fields
    }
}
