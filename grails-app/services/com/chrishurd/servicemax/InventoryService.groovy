package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class InventoryService {

    def connectionService
    def recordTypeService
    def moduleService

    def getCustomProcesses(orgInfo) {
        def migrationObjects = []

        connectionService.query(orgInfo, "SELECT Id, SVMXC__Name__c, SVMXC__ProcessID__c, LastModifiedDate FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__IsStandard__c = false AND SVMXC__Record_Type_Name__c = 'Inventory Process' ORDER BY SVMXC__Name__c ").each { record ->
            def mObj = new MigrationObject()
            mObj.type = 'inventory'
            mObj.id = record.getId()
            mObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ProcessID__c')})"
            mObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
            migrationObjects.add(mObj)
        }

        return migrationObjects
    }

    def migrateProcess(fromOrgInfo, toOrgInfo, id) {
        def fromInventory = this.getInventory(fromOrgInfo, id)

        def toInventory = this.getInventoryByProcessId(toOrgInfo, fromInventory.inventory.getField('SVMXC__ProcessID__c'))

        def newInv = new SObject()
        newInv.setType('SVMXC__ServiceMax_Processes__c')
        newInv.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Inventory Process'))

        if (toInventory) {
            if (! toInventory.processSteps.isEmpty()) {
                connectionService.delete(toOrgInfo, toInventory.processSteps.keySet() as String[])
                toInventory.processSteps = [:]
            }
            newInv.setId(toInventory.inventory.getId())
        }
        else {
            toInventory = new Inventory()
            def module = new Module()
            connectionService.retrieveObject(toOrgInfo, "SVMXC__ServiceMax_Processes__c", " SVMXC__ModuleID__c = '${fromInventory.module.module.getField('SVMXC__ModuleID__c')}' ").each { record ->
                module.module = record
            }

            moduleService.populateModuleProfileDetails(toOrgInfo, module)
            toInventory.module = module
        }

        connectionService.migrateObject(toOrgInfo, fromInventory.inventory, newInv, 'SVMXC__ServiceMax_Processes__c')

        toInventory.module = moduleService.migrateModuleDetails(toOrgInfo, fromInventory.module, toInventory.module)

        newInv.setField('SVMXC__Module__c', toInventory.module.module.getId())
        if (! toInventory.module.submodules.isEmpty()) {
            toInventory.module.submodules.keySet().each { subId ->
                newInv.setField('SVMXC__Submodule__c', toInventory.module.submodules.get(subId).submodule.getId())
            }
        }

        connectionService.updateObjects(toOrgInfo, [newInv])
        toInventory.inventory = newInv

        if (! fromInventory.processSteps.isEmpty()) {
            fromInventory.processSteps.values().each { step ->
                def newStep = new SObject()
                newStep.setType('SVMXC__ServiceMax_Config_Data__c')
                newStep.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Expressions'))
                connectionService.migrateObject(toOrgInfo, step, newStep, 'SVMXC__ServiceMax_Config_Data__c')
                newStep.setField('SVMXC__Inventory_Process__c', newInv.getId())
                toInventory.processSteps.put(step.getId(), newStep)
            }

            connectionService.updateObjects(toOrgInfo, toInventory.processSteps.values())
        }

        return toInventory
    }

    def getInventoryByProcessId(orgInfo, processId) {
        def inv = new Inventory()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${processId}' ").each { record ->
            inv.inventory = record
        }

        if (inv.inventory) {
            this.populateInventoryDetails(orgInfo, inv)
            return inv
        }

        return null
    }


    def getInventory(orgInfo, id) {
        def inv = new Inventory()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " Id = '${id}' ").each { record ->
            inv.inventory = record
        }

        this.populateInventoryDetails(orgInfo, inv)

        return inv
    }

    def populateInventoryDetails(orgInfo, inv) {

        connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Config_Data__c", " SVMXC__Inventory_Process__c = '${inv.inventory.getId()}' AND SVMXC__RecordType_Name__c = 'Expressions' ").each { record ->
            inv.processSteps.put(record.getId(), record)
        }

        def module = new Module()
        connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Processes__c", " Id = '${inv.inventory.getField('SVMXC__Module__c')}' ").each { record ->
            module.module = record
        }

        moduleService.populateModuleProfileDetails(orgInfo, module)

        connectionService.retrieveObject(orgInfo, "SVMXC__ServiceMax_Processes__c", " Id = '${inv.inventory.getField('SVMXC__Submodule__c')}' ").each { record ->
            module.submodules.put(record.getId(), moduleService.getSubmodule(orgInfo, record))
        }

        inv.module = module
    }


}
