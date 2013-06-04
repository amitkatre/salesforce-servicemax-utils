package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class WizardService {

    static scope = "request"
    def connectionService
    def recordTypeService
    def namedExpressionService
    def actionService
    def transactionService
    def inventoryService

    def getCustomWizards(orgInfo) {
        def migrationObjects = []

        connectionService.query(orgInfo, "SELECT Id, SVMXC__Name__c, SVMXC__ProcessID__c, LastModifiedDate FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__IsStandard__c = false AND SVMXC__Record_Type_Name__c = 'Wizard' ORDER BY SVMXC__Name__c ").each { record ->
            def mObj = new MigrationObject()
            mObj.type = 'inventory'
            mObj.id = record.getId()
            mObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ProcessID__c')})"
            mObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
            migrationObjects.add(mObj)
        }

        return migrationObjects
    }


    def getWizardByProcessId(orgInfo, processId) {
        def wizard = new Wizard()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${processId}' ").each { record ->
            wizard.wizard = record
        }


    }



}
