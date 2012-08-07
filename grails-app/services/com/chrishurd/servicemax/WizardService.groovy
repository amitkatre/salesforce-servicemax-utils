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


    def migrateWizard(fromOrgInfo, toOrgInfo, id) {
        def fromWizard = this.getWizard(fromOrgInfo, id)

        def toWizard = this.getWizardByProcessId(toOrgInfo, fromWizard.wizard.getField('SVMXC__ProcessID__c'))

        def newWizard = new SObject()
        newWizard.setType('SVMXC__ServiceMax_Processes__c')
        newWizard.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Wizard'))

        if (toWizard) {
            connectionService.delete(toOrgInfo, toWizard.wizardStep.keySet() as String[])
            toWizard.wizardStep = [:]
            newWizard.setId(toWizard.wizard.getId())
        }
        else {
            toWizard = new Wizard()
            this.populateWizardProfileDetails(toOrgInfo, toWizard)
        }

        connectionService.migrateObject(toOrgInfo, fromWizard.wizard, newWizard, 'SVMXC__ServiceMax_Processes__c')

        if (! fromWizard.namedExpressions.isEmpty()) {
            fromWizard.namedExpressions.keySet().each { neId ->
                toWizard.namedExpressions.put(neId, namedExpressionService.migrateNamedExpression(toOrgInfo, fromWizard.namedExpressions.get(neId)))
            }
        }

        if (fromWizard.wizard.getField('SVMXC__Submodule__c')) {
            newWizard.setField('SVMXC__Submodule__c', toWizard.namedExpressions.get(fromWizard.wizard.getField('SVMXC__Submodule__c')).namedExpression.getId())
        }

        connectionService.updateObjects(toOrgInfo, newWizard)
        toWizard.wizard = newWizard


    }

    def getWizardByProcessId(orgInfo, processId) {
        def wizard = new Wizard()

        connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Processes__c", " WHERE SVMXC__ProcessID__c = '${processId}' ").each { record ->
            wizard.wizard = record
        }

        if (wizard.wizard) {
            this.populateWizardDetails(orgInfo, wizard)
            return wizard
        }

        return null
    }


    def getWizard(orgInfo, id) {
        def wizard = new Wizard()

        connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Processes__c", " WHERE Id = '${id}' ").each { record ->
            wizard.wizard = record
        }


        return wizard
    }

    def populateWizardDetails(orgInfo, wizard) {

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Dispatch_Process__c = '${wizard.wizard.getId()}' AND SVMXC__RecordType_Name__c = 'Wizard Access' ").each { record ->
            wizard.wizardAccess.put(record.getId(), record)
        }

        if (wizard.wizard.getField('SVMXC__Submodule__c')) {
            wizard.namedExpressions.put(wizard.wizard.getField('SVMXC__Submodule__c'), namedExpressionService.getNamedExpression(orgInfo, wizard.wizard.getField('SVMXC__Submodule__c')))
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Module__c = '${wizard.wizard.getId()}' AND SVMXC__Record_Type_Name__c = 'Wizard Step' ").each { record ->
            this.getWizardStep(orgInfo, wizard, record)
        }

        this.populateWizardProfileDetails(orgInfo, wizard)

    }

    def populateWizardProfileDetails(orgInfo, wizard) {
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__RecordType_Name__c = 'Configuration Profile' AND (SVMXC__IsDefault__c = true OR (SVMXC__Active__c = true AND SVMXC__Configuration_Type__c = 'Global')) ").each {record ->
            wizard.profiles.put(record.getId(), record)
        }
    }

    def getWizardStep(orgInfo, wizard, wizardStepObj) {
        def wizardStep = new WizardStep()
        wizardStep.step = wizardStepObj

        if (wizardStepObj.getField('SVMXC__Process__c')) {
            connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Processes__c", " Id = '${wizardStepObj.getField('SVMXC__Process__c')}' ").each { record ->
                def recordTypeId = record.getField('RecordTypeId')

                if (recordTypeId.equals(recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'SF Action'))) {
                    wizardStep.action = actionService.getAction(orgInfo, wizardStepObj.getField('SVMXC__Process__c'))
                }
                else if (recordTypeId.equals(recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'Target Manager'))) {
                    wizardStep.action = transactionService.getSFMTransaction(orgInfo, wizardStepObj.getField('SVMXC__Process__c'))
                }
                else if (recordTypeId.equals(recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'Inventory Process'))) {
                    wizardStep.action = inventoryService.getInventory(orgInfo, wizardStepObj.getField('SVMXC__Process__c'))
                }
            }

        }

        wizardStep.nextStep = wizardStepObj.getField('SVMXC__Process1__c')

        if (wizardStepObj.getField('SVMXC__Submodule__c')) {
            wizard.namedExpressions.put(wizardStepObj.getField('SVMXC__Submodule__c'), namedExpressionService.getNamedExpression(orgInfo, wizardStepObj.getField('SVMXC__Submodule__c')))
        }

    }
}
