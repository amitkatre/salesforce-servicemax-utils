package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class TransactionService {

    static scope = "request"
    def connectionService
    def namedExpressionService
    def pageLayoutService
    def objectMappingService
    def recordTypeService

    def getSFMTransactions(orgInfo) {
        def sfmTransactions = []
        connectionService.query(orgInfo, "SELECT Id, SVMXC__Name__c, CreatedBy.Name, LastModifiedBy.Name, LastModifiedDate, SVMXC__ProcessID__c FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__Record_Type_Name__c = 'Target Manager' AND SVMXC__IsStandard__c = false ORDER BY SVMXC__Name__c ").each { record ->
            def migrObj = new MigrationObject()
            migrObj.type = 'sfmTransaction'
            migrObj.id = record.getId()
            migrObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ProcessID__c')}"
            migrObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
            sfmTransactions.add(migrObj)
        }

        return sfmTransactions
    }

    def getTargetTransactionByProcessId(orgInfo, processId) {
        def sfmTransaction = new SFMTransaction()

            connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${processId}' ").each { record ->
                sfmTransaction.sfmTransaction = record
            }

            if (sfmTransaction.sfmTransaction) {
            connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Process__c = '${sfmTransaction.sfmTransaction.getId()}' AND SVMXC__Record_Type_Name__c = 'Process Node Objects' ").each {record ->
                sfmTransaction.nodeTargets.put(record.getId(), record);
            }

            connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Dispatch_Process__c = '${sfmTransaction.sfmTransaction.getId()}' AND SVMXC__RecordType_Name__c = 'Field Mapping' ").each {record ->
                sfmTransaction.sourceUpdates.put(record.getId(), record);
            }

            return sfmTransaction
        }
        else {
            return null
        }
    }

    def getSFMTransaction(orgInfo, id) {
        def sfmTransaction = new SFMTransaction()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " Id = '${id}' ").each { record ->
            sfmTransaction.sfmTransaction = record

            if (record.getField('SVMXC__Page_Layout__c') != null) {
                def pageLayout = pageLayoutService.getPageLayout(orgInfo, record.getField('SVMXC__Page_Layout__c'))
                sfmTransaction.pageLayout = pageLayout
                sfmTransaction.addFields(pageLayout.obj, pageLayout.fields)

                pageLayout.childrenLayout.values().each { child ->
                    sfmTransaction.addFields(child.obj, child.fields)
                }

                pageLayout.namedSearches.values().each { nameSearch ->
                    nameSearch.searchObjects.values().each {so ->
                        sfmTransaction.addFields(so.obj, so.fields)
                    }
                }

            }
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Process__c = '${sfmTransaction.sfmTransaction.getId()}' AND SVMXC__Record_Type_Name__c = 'Process Node Objects' ").each {record ->
            sfmTransaction.nodeTargets.put(record.getId(), record);

            if (record.getField('SVMXC__Module__c') != null) {
                def objMapping = objectMappingService.getObjectMapping(orgInfo, record.getField('SVMXC__Module__c'))
                sfmTransaction.objectMappings.put(record.getField('SVMXC__Module__c'), objMapping)
                sfmTransaction.addFields(objMapping.sourceObject, objMapping.sourceFields)
                sfmTransaction.addFields(objMapping.targetObject, objMapping.targetFields)
            }

            if (record.getField('SVMXC__Final_Exception_Process__c') != null) {
                def objMapping = objectMappingService.getObjectMapping(orgInfo, record.getField('SVMXC__Final_Exception_Process__c'))
                sfmTransaction.objectMappings.put(record.getField('SVMXC__Final_Exception_Process__c'), objMapping)
                sfmTransaction.addFields(objMapping.sourceObject, objMapping.sourceFields)
                sfmTransaction.addFields(objMapping.targetObject, objMapping.targetFields)
            }

            if (record.getField('SVMXC__Submodule__c') != null) {
                def namedExp = namedExpressionService.getNamedExpression(orgInfo, record.getField('SVMXC__Submodule__c'))
                sfmTransaction.namedExpressions.put(namedExp.namedExpression.getId(), namedExp)
                sfmTransaction.addFields(namedExp.obj, namedExp.fields)
            }
        }


        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Dispatch_Process__c = '${sfmTransaction.sfmTransaction.getId()}' AND SVMXC__RecordType_Name__c = 'Field Mapping' ").each {record ->
            sfmTransaction.sourceUpdates.put(record.getId(), record);

        }

        return sfmTransaction

    }


    def migrateSFMTransaction(fromOrgInfo, toOrgInfo, id) {

        def errors = [] as Set<Object>
        def fromSFMTransaction = getSFMTransaction(fromOrgInfo, id)
        def toSFMTransaction = this.getTargetTransactionByProcessId(toOrgInfo, fromSFMTransaction.sfmTransaction.getField('SVMXC__ProcessID__c'))

        if (! toSFMTransaction) {
            toSFMTransaction = new SFMTransaction()
        }

        fromSFMTransaction.fields.keySet().each { obj ->
            def fields = connectionService.getAllFieldsUpperCase(toOrgInfo, obj)

            fromSFMTransaction.fields.get(obj).each { field ->
                if (! fields.contains(field.toUpperCase())) {
                    errors.add("Missing field '${field}' for object '${obj}'")
                }
            }
        }

        if (! errors.isEmpty()) {
            return errors
        }

        fromSFMTransaction.objectMappings.keySet().each { omId ->
            def fromObjMapping = fromSFMTransaction.objectMappings.get(omId)

            toSFMTransaction.objectMappings.put(omId, objectMappingService.migrateObjectMapping(toOrgInfo, fromObjMapping))
        }

        fromSFMTransaction.namedExpressions.keySet().each { neId ->
            def fromNExpression = fromSFMTransaction.namedExpressions.get(neId)

            toSFMTransaction.namedExpressions.put(neId, namedExpressionService.migrateNamedExpression(toOrgInfo, fromNExpression))
        }

        if (fromSFMTransaction.pageLayout != null) {
            toSFMTransaction.pageLayout = pageLayoutService.migratePageLayout(toOrgInfo, fromSFMTransaction.pageLayout)
        }

        def transaction = fromSFMTransaction.sfmTransaction



        def newTransaction = new SObject()
        newTransaction.setType('SVMXC__ServiceMax_Processes__c')
        newTransaction.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Target Manager'))
        if (toSFMTransaction.sfmTransaction)
        {
            newTransaction.setId(toSFMTransaction.sfmTransaction.getId())

            if (! toSFMTransaction.nodeTargets.isEmpty()) {
                connectionService.delete(toOrgInfo, toSFMTransaction.nodeTargets.keySet() as String[])
                toSFMTransaction.nodeTargets = [:]
            }

            if (! toSFMTransaction.sourceUpdates.isEmpty()) {
                connectionService.delete(toOrgInfo, toSFMTransaction.sourceUpdates.keySet() as String[])
                toSFMTransaction.sourceUpdates = [:]
            }

        }

        connectionService.migrateObject(toOrgInfo, fromSFMTransaction.sfmTransaction, newTransaction, 'SVMXC__ServiceMax_Processes__c')

        if (transaction.getField('SVMXC__Page_Layout__c') != null) {
            newTransaction.setField('SVMXC__Page_Layout__c', toSFMTransaction.pageLayout.pageLayout.getId())
        }
        connectionService.updateObjects(toOrgInfo, [newTransaction])
        toSFMTransaction.sfmTransaction = newTransaction

        if (! fromSFMTransaction.nodeTargets.isEmpty()) {
            fromSFMTransaction.nodeTargets.keySet().each { ntId ->
                def nodeTarget = fromSFMTransaction.nodeTargets.get(ntId)
                def newNodeTarget = new SObject()
                newNodeTarget.setType('SVMXC__ServiceMax_Processes__c')
                newNodeTarget.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Process Node Objects'))
                connectionService.migrateObject(toOrgInfo, nodeTarget, newNodeTarget, 'SVMXC__ServiceMax_Processes__c')
                newNodeTarget.setField('SVMXC__Process__c', newTransaction.getId())

                if (nodeTarget.getField('SVMXC__Submodule__c') != null) {
                    def namedExp = toSFMTransaction.namedExpressions.get(nodeTarget.getField('SVMXC__Submodule__c'))
                    newNodeTarget.setField('SVMXC__Submodule__c', namedExp.namedExpression.getId())
                }

                if (nodeTarget.getField('SVMXC__Module__c') != null) {
                    def objMap = toSFMTransaction.objectMappings.get(nodeTarget.getField('SVMXC__Module__c'))
                    newNodeTarget.setField('SVMXC__Module__c', objMap.objectMapping.getId())
                }

                if (nodeTarget.getField('SVMXC__Page_Layout__c') != null) {

                    if (toSFMTransaction.pageLayout.originalId.equals(nodeTarget.getField('SVMXC__Page_Layout__c'))) {
                        newNodeTarget.setField('SVMXC__Page_Layout__c', toSFMTransaction.pageLayout.pageLayout.getId())
                    }
                    else  {
                        def pageLayout = toSFMTransaction.pageLayout.childrenLayout.get(nodeTarget.getField('SVMXC__Page_Layout__c'))
                        newNodeTarget.setField('SVMXC__Page_Layout__c', pageLayout.pageLayout.getId())
                    }
                }

                if (nodeTarget.getField('SVMXC__Final_Exception_Process__c') != null) {
                    def objMap = toSFMTransaction.objectMappings.get(nodeTarget.getField('SVMXC__Final_Exception_Process__c'))
                    newNodeTarget.setField('SVMXC__Final_Exception_Process__c', objMap.objectMapping.getId())
                }

                toSFMTransaction.nodeTargets.put(ntId, newNodeTarget)
            }

            def parents = []
            def hasParents = []
            toSFMTransaction.nodeTargets.values().each { node ->
                if (node.getField('SVMXC__Node_Parent__c') != null || node.getField('SVMXC__Parent_Object__c')) {
                    hasParents.add(node)
                }
                else {
                    parents.add(node)
                }
            }


            connectionService.updateObjects(toOrgInfo, parents)

            if (! hasParents.isEmpty()) {
                hasParents.each { node ->
                    if (node.getField('SVMXC__Parent_Object__c') != null) {
                        node.setField('SVMXC__Parent_Object__c', toSFMTransaction.nodeTargets.get(node.getField('SVMXC__Parent_Object__c')).getId())
                    }
                    else {
                        node.setField('SVMXC__Node_Parent__c', toSFMTransaction.nodeTargets.get(node.getField('SVMXC__Node_Parent__c')).getId())
                    }
                }

                connectionService.updateObjects(toOrgInfo, hasParents)

            }
        }

        if (! fromSFMTransaction.sourceUpdates.isEmpty()) {
            fromSFMTransaction.sourceUpdates.keySet().each { suId ->
                def sUpdate = fromSFMTransaction.sourceUpdates.get(suId)
                def newSUpdate = new SObject()
                newSUpdate.setType('SVMXC__ServiceMax_Config_Data__c')
                newSUpdate.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Field Mapping'))
                connectionService.migrateObject(toOrgInfo, sUpdate, newSUpdate, 'SVMXC__ServiceMax_Config_Data__c')
                newSUpdate.setField('SVMXC__Dispatch_Process__c', newTransaction.getId())

                if (sUpdate.getField('SVMXC__MapID__c') != null) {
                    newSUpdate.setField('SVMXC__MapID__c', toSFMTransaction.objectMappings.get(sUpdate.getField('SVMXC__MapID__c')).objectMapping.getId())
                }

                if (sUpdate.getField('SVMXC__Setting_ID__c') != null) {
                    newSUpdate.setField('SVMXC__Setting_ID__c', toSFMTransaction.nodeTargets.get(sUpdate.getField('SVMXC__Setting_ID__c')).getId())
                }

                toSFMTransaction.sourceUpdates.put(suId, newSUpdate)
            }

            connectionService.updateObjects(toOrgInfo, toSFMTransaction.sourceUpdates.values())
        }

        return toSFMTransaction

    }

}
