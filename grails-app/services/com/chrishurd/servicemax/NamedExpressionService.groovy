package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class NamedExpressionService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getNamedExpressionByName(orgInfo, name) {
        def namedExpression = new NamedExpression()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${name}' ").each { record ->
            namedExpression.namedExpression = record
        }

        if (namedExpression.namedExpression != null) {
            this.populateNamedExpressionDetails(orgInfo, namedExpression)
            return namedExpression
        }
        else {
            return null
        }

    }

    def getNamedExpression(orgInfo, id) {
        def namedExpression = new NamedExpression()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " ID = '${id}' ").each { record ->
            namedExpression.namedExpression = record
        }

        if (namedExpression.namedExpression != null) {
            this.populateNamedExpressionDetails(orgInfo, namedExpression)
            return namedExpression
        }
        else {
            return null
        }

    }

    def populateNamedExpressionDetails(orgInfo, namedExpression) {
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Expression_Rule__c = '${namedExpression.namedExpression.getId()}' ").each { record ->
            namedExpression.expressions.put(record.getId(), record)
            namedExpression.obj = record.getField('SVMXC__Object_Name__c')
            namedExpression.fields.add(record.getField('SVMXC__Field_Name__c'))
        }
    }

    def migrateNamedExpression(orgInfo, nExpression) {
        def toNExpression = this.getNamedExpressionByName(orgInfo, nExpression.namedExpression.getField('SVMXC__ProcessID__c'))
        def newNExpression = new SObject()
        newNExpression.setType('SVMXC__ServiceMax_Processes__c')

        if (toNExpression) {
            if ("false".equals(toNExpression.namedExpression.getField('SVMXC__IsStandard__c'))) {
                connectionService.delete(orgInfo, toNExpression.expressions.keySet() as String[])
                toNExpression.expressions = [:]
                newNExpression.setId(toNExpression.namedExpression.getId())
            }
            else {
                return toNExpression
            }
        }
        else {
            toNExpression = new NamedExpression()
        }

        newNExpression.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'SVMX Rule'))
        connectionService.migrateObject(orgInfo, nExpression.namedExpression, newNExpression, 'SVMXC__ServiceMax_Processes__c')

        connectionService.updateObjects(orgInfo, [newNExpression])
        toNExpression.namedExpression = newNExpression

        nExpression.expressions.values().each { ne ->
            def newNE = new SObject()
            newNE.setType('SVMXC__ServiceMax_Config_Data__c')
            newNE.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Expressions'))
            connectionService.migrateObject(orgInfo, ne, newNE, 'SVMXC__ServiceMax_Config_Data__c')
            newNE.setField('SVMXC__Expression_Rule__c', newNExpression.getId())
            toNExpression.expressions.put(ne.getId(), newNE)
        }

        connectionService.insert(orgInfo, toNExpression.expressions.values() as SObject[])

        return toNExpression

    }
}
