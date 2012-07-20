package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class ActionService {

    def connectionService
    def recordTypeService

    def getCustomActions(orgInfo) {
        def migrationObjects = []

        connectionService.query(orgInfo, "SELECT Id, SVMXC__Name__c, SVMXC__ProcessID__c, LastModifiedDate FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__IsStandard__c = false AND SVMXC__Record_Type_Name__c = 'SF Action' ORDER BY SVMXC__Name__c ").each { record ->
            def mObj = new MigrationObject()
            mObj.type = 'sfAction'
            mObj.id = record.getId()
            mObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ProcessID__c')})"
            mObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
            migrationObjects.add(mObj)
        }

        return migrationObjects
    }

    def migrateAction(fromOrgInfo, toOrgInfo, id) {
        def fromAction = this.getAction(fromOrgInfo, id)
        def toAction = this.getActionByName(toOrgInfo, fromAction.action.getField('SVMXC__ProcessID__c'))

        def newAction = new SObject()
        newAction.setType('SVMXC__ServiceMax_Processes__c')
        newAction.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'SF Action'))
        if (toAction) {
            newAction.setId(toAction.action.getId())
            if (! toAction.params.isEmpty()) {
                connectionService.delete(toOrgInfo, toAction.params.keySet() as String[])
                toAction.params = [:]
            }
        }
        else {
            toAction = new Action()
        }

        connectionService.migrateObject(toOrgInfo, fromAction.action, newAction, 'SVMXC__ServiceMax_Processes__c')
        connectionService.updateObjects(toOrgInfo, [newAction])
        toAction.action = newAction

        if (! fromAction.params.isEmpty()) {
            fromAction.params.values().each { param ->
                def newParam = new SObject()
                newParam.setType('SVMXC__ServiceMax_Config_Data__c')
                newParam.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'SF Action Parameter'))
                connectionService.migrateObject(toOrgInfo, param, newParam, 'SVMXC__ServiceMax_Config_Data__c')
                newParam.setField('SVMXC__Dispatch_Process__c', newAction.getId())
                toAction.params.put(param.getId(), newParam)
            }

            connectionService.updateObjects(toOrgInfo, toAction.params.values())
        }

        return toAction


    }

    def getActionByName(orgInfo, name) {
        def action = new Action()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${name}' AND SVMXC__Record_Type_Name__c = 'SF Action' ").each { record ->
            action.action = record
        }

        if (action.action) {
            populateActionDetails(orgInfo, action)
            return action
        }

        return null
    }

    def getAction(orgInfo, id) {
        def action = new Action()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " Id = '${id}' ").each { record ->
            action.action = record
        }

        this.populateActionDetails(orgInfo, action)

        return action
    }

    def populateActionDetails(orgInfo, action) {
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Dispatch_Process__c = '${action.action.getId()}' AND SVMXC__RecordType_Name__c = 'SF Action Parameter' ").each { record ->
            action.params.put(record.getId(), record)
        }
    }
}
