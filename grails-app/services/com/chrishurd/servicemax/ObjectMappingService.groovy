package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class ObjectMappingService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getObjectMappingByMapId(orgInfo, mapId) {
        def sfmObjectMapping = new SFMObjectMapping()
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__MapID__c = '${mapId}' ").each { record ->
            sfmObjectMapping.objectMapping = record

            if (record.getField('SVMXC__Target_Object_Name__c') != null) {
                sfmObjectMapping.targetObject = record.getField('SVMXC__Target_Object_Name__c')
            }

            if (record.getField('SVMXC__Source_Object_Name__c') != null) {
                sfmObjectMapping.sourceObject = record.getField('SVMXC__Source_Object_Name__c')
            }
        }

        if (sfmObjectMapping.objectMapping != null) {
            this.populateObjectMappingDetails(orgInfo, sfmObjectMapping)
            return sfmObjectMapping
        }
        else {
            return null
        }
    }

    def getObjectMapping(orgInfo, id) {
        def sfmObjectMapping = new SFMObjectMapping()
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " ID = '${id}' ").each { record ->
            sfmObjectMapping.objectMapping = record

            if (record.getField('SVMXC__Target_Object_Name__c') != null) {
                sfmObjectMapping.targetObject = record.getField('SVMXC__Target_Object_Name__c')
            }

            if (record.getField('SVMXC__Source_Object_Name__c') != null) {
                sfmObjectMapping.sourceObject = record.getField('SVMXC__Source_Object_Name__c')
            }
        }

        if (sfmObjectMapping.objectMapping != null) {
            this.populateObjectMappingDetails(orgInfo, sfmObjectMapping)
            return sfmObjectMapping
        }
        else {
            return null
        }
    }

    def populateObjectMappingDetails(orgInfo, sfmObjectMapping) {

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__MapID__c = '${sfmObjectMapping.objectMapping.getId()}' ").each { record ->
            sfmObjectMapping.fieldMappings.put(record.getId(), record)
            if (record.getField('SVMXC__Source_Field_Name__c') != null) {
                sfmObjectMapping.sourceFields.add(record.getField('SVMXC__Source_Field_Name__c'))
            }
            if (record.getField('SVMXC__Target_Field_Name__c') != null) {
                sfmObjectMapping.targetFields.add(record.getField('SVMXC__Target_Field_Name__c'))
            }
        }

        return sfmObjectMapping
    }

    def migrateObjectMapping(orgInfo, sfmObjectMapping) {
        def toObjMapping = this.getObjectMappingByMapId(orgInfo, sfmObjectMapping.objectMapping.getField('SVMXC__MapID__c'))
        def newObjMapping = new SObject()
        newObjMapping.setType('SVMXC__ServiceMax_Processes__c')
        if (toObjMapping) {
            if ("false".equals(toObjMapping.objectMapping.getField('SVMXC__IsStandard__c'))) {
                if (! toObjMapping.fieldMappings.isEmpty()) {
                    connectionService.delete(orgInfo, toObjMapping.fieldMappings.keySet() as String[])
                    toObjMapping.fieldMappings = [:]
                }
                newObjMapping.setId(toObjMapping.objectMapping.getId())
            }
            else {
                return toObjMapping
            }
        }
        else {
            toObjMapping = new SFMObjectMapping()
        }

        newObjMapping.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, "SVMXC__ServiceMax_Processes__c", "Object Mapping"))


        connectionService.migrateObject(orgInfo, sfmObjectMapping.objectMapping, newObjMapping, 'SVMXC__ServiceMax_Processes__c')

        connectionService.updateObjects(orgInfo, [newObjMapping])
        toObjMapping.objectMapping = newObjMapping

        sfmObjectMapping.fieldMappings.values().each { fm ->
            def newFM = new SObject()
            newFM.setType('SVMXC__ServiceMax_Config_Data__c')
            newFM.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Field Mapping'))
            connectionService.migrateObject(orgInfo, fm, newFM, 'SVMXC__ServiceMax_Config_Data__c')
            newFM.setField('SVMXC__MapID__c', newObjMapping.getId())
            toObjMapping.fieldMappings.put(fm.getId(), newFM)
        }

        connectionService.insert(orgInfo, toObjMapping.fieldMappings.values() as SObject[])

        return toObjMapping
    }
}
