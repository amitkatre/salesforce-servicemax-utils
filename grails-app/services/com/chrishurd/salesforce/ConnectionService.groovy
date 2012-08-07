package com.chrishurd.salesforce

import com.sforce.soap.partner.Connector
import com.sforce.soap.partner.CallOptions_element
import com.sforce.ws.ConnectorConfig
import java.util.regex.Pattern
import com.sforce.soap.metadata.MetadataConnection
import com.sforce.async.BulkConnection
import com.sforce.async.StatusCode
import com.sforce.soap.partner.FieldType
import com.sforce.soap.partner.sobject.SObject

class ConnectionService {

    static scope = "session"

    def partnerConnections = [:]
    def metaConnections = [:]
    def bulkConnections = [:]
    def allFields = [:]
    def allFieldsUpperCase = [:]
    def allEditableFields = [:]

    def getPartnerConnection(orgInfo) {
        return getPartnerConfig(orgInfo).connection
    }

    def getPartnerConfig(orgInfo) {
        if (! partnerConnections.containsKey(orgInfo.name)) {
            partnerConnections.put(orgInfo.name, initPartnerConnection(orgInfo))
        }

        return partnerConnections.get(orgInfo.name)
    }


    def initPartnerConnection(OrganizationInfo orgInfo) {

        def connectionInfo = new SFConnection(orgInfo)
        def connection = Connector.newConnection(connectionInfo)

        def co = new CallOptions_element();
        co.setClient(orgInfo.name)

        if (orgInfo.orgId == null) {
            orgInfo.orgId = connectionInfo.getServiceEndpoint().substring(connectionInfo.getServiceEndpoint().lastIndexOf('/') + 1)
            orgInfo.save()
        }

        connection.__setCallOptions(co)
        connectionInfo.connection = connection

        return connectionInfo
    }

    def getMetedataConnection(orgInfo) {
        if (! metaConnections.containsKey(orgInfo.name)) {
            def config = getPartnerConfig(orgInfo)

            def configNew = new ConnectorConfig()
            configNew.setSessionId(config.getSessionId())
            Pattern p = Pattern.compile("(.*/Soap)/./(.*)")
            configNew.setServiceEndpoint(p.matcher(config.getServiceEndpoint()).replaceFirst('\$1/m/\$2'))

            def conn = new MetadataConnection(configNew)

            conn.setCallOptions(orgInfo.name)
            metaConnections.put(orgInfo.name, conn)
        }

        return metaConnections.get(orgInfo.name)
    }

    def getBulkConnection(orgInfo) {
        if (! bulkConnections.containsKey(orgInfo.name)) {
            def config = getPartnerConfig(orgInfo)
            def newConfig = new ConnectorConfig()
            newConfig.setSessionId(config.getSessionId())
            newConfig.setServiceEndpoint(config.getServiceEndpoint())
            def p = Pattern.compile("(.*)/Soap/./(.*)/(.*)\$");
            newConfig.setRestEndpoint(p.matcher(config.getServiceEndpoint()).replaceFirst("\$1/async/\$2"))
            bulkConnections.put(orgInfo.name, new BulkConnection(newConfig))
        }

        return bulkConnections.get(orgInfo.name)
    }

    def query(orgInfo, sql) {
        def connection = getPartnerConnection(orgInfo)

        def results = connection.query(sql);
        def resultObjects = []
        def done = false
        while (! done) {
            results.getRecords().each { record ->
                resultObjects.add(record)
            }

            if (results.isDone()) {
                done = true
            }
            else {
                results = connection.queryMore(results.getQueryLocator())
            }
        }

        return resultObjects
    }

    def delete(orgInfo, ids) {
        def connection = getPartnerConnection(orgInfo)

        if (ids.size() > 100) {
            def batchIds = []
            ids.each { id ->
                if (batchIds.size() == 100) {
                    connection.delete(batchIds as String[])
                    batchIds = []
                }

                batchIds.add(id)
            }

            if (! batchIds.isEmpty()) {
                connection.delete(batchIds as String[])
            }
        }
        else {
            connection.delete(ids)
        }


    }

    def updateObjects(orgInfo, objects) {
        def insertObjs = []
        def updateObjs = []

        objects.each { obj ->
            if (obj.getId() != null) {
                updateObjs.add(obj)
            }
            else {
                insertObjs.add(obj)
            }
        }

        if (! insertObjs.isEmpty()) {
            def batchInsert = []
            insertObjs.each { obj ->
                if (batchInsert.size() == 100) {
                    insert(orgInfo, batchInsert as SObject[])
                    batchInsert.clear()
                }

                batchInsert.add(obj)
            }

            if (! batchInsert.isEmpty()) {
                insert(orgInfo, batchInsert as SObject[])
            }
        }

        if (! updateObjs.isEmpty()) {
            def batchUpdate = []
            updateObjs.each { obj ->
                if (batchUpdate.size() == 100) {
                    update(orgInfo, batchUpdate as SObject[])
                    batchUpdate.clear()
                }

                batchUpdate.add(obj)
            }

            if (! batchUpdate.isEmpty()) {
                update(orgInfo, batchUpdate as SObject[])
            }

        }
    }

    def insert(orgInfo, objects) {
        def connection = getPartnerConnection(orgInfo)

        def results = connection.create(objects)
        def error = checkForErrors(results)

        if (! error) {
            results.eachWithIndex { result, i ->
                objects[i].setId(result.getId())
            }
        }

        if (error) {
            throw new Exception(error)
        }
    }

    def update(orgInfo, objects) {
        def connection = getPartnerConnection(orgInfo)

        def results =  connection.update(objects)
        def error = checkForErrors(results)
        if (error) {
            throw new Exception(error)
        }
    }


    def checkForErrors(results) {
        def failures = []
        def optimisticFailure = false

        results.each { result ->
            if (! result.getSuccess()) {
                optimisticFailure = handleError(failures, result.getErrors()[0])
            }
        }
        return handleFailures(failures, optimisticFailure)
    }

    static def handleError(failures, error) {
        failures.add(error)
        return error.getStatusCode() == StatusCode.ENTITY_FAILED_IFLASTMODIFIED_ON_UPDATE
    }

    static def handleFailures(failures, optimisticFailure) {
        if (! failures.isEmpty()) {
            Iterator<Error> iter = failures.iterator()
            while (iter.hasNext()) {
                def error = iter.next()

                if (error.getStatusCode() == StatusCode.ALL_OR_NONE_OPERATION_ROLLED_BACK && failures.size() > 1) {
                    iter.remove()
                }
            }
            def error = failures.get(0).getMessage()
            return error
        }

        return null
    }


    def retrieveObject(OrganizationInfo orgInfo, String tableName, String where) {

        def fields = this.getAllEditableFields(orgInfo, tableName) as Set<String>
        fields.add('Name')
        def results =  this.query(orgInfo, "SELECT Id, ${fields.join(',')} FROM ${tableName} WHERE $where ")
        return results
    }


    def migrateObject(orgInfo, fromObj, toObj, tableName) {
        def fields = this.getAllFields(orgInfo, tableName) as Set<String>
        fields.each { field ->
            if (field.isUpdateable() && ! (["ID", "NAME", "OWNERID", "RECORDTYPEID"] as Set<String>).contains(field.getName().toUpperCase())) {
                if (field.getType().equals(FieldType._boolean)) {
                    if ("true".equals(fromObj.getField(field.getName()))) {
                        toObj.setField(field.getName(), new Boolean(true))
                    }
                    else {
                        toObj.setField(field.getName(), new Boolean(false))
                    }
                }
                else {
                    toObj.setField(field.getName(), fromObj.getField(field.getName()))
                }
            }
        }

    }

    def getAllObjects(orgInfo) {

        def objects = [] as Set<String>

        def connection =  this.getPartnerConnection(orgInfo)
        def descrProcesses = connection.describeGlobal()

        for (def sObject : descrProcesses.getSobjects()) {
            objects.add(sObject.getName())
        }

        return objects

    }

    def getAllEditableFields(orgInfo, objectName) {

        if (! allEditableFields) {
            allEditableFields = [:]
        }

        if (! allEditableFields.containsKey(orgInfo.id) || ! allEditableFields.get(orgInfo.id).containsKey(objectName)) {
            if (! allEditableFields.containsKey(orgInfo.id)) {
                allEditableFields.put(orgInfo.id, [:])
            }

            if (! allEditableFields.get(orgInfo.id).containsKey(objectName)) {
                allEditableFields.get(orgInfo.id).putAt(objectName, [])
            }

            def connection = this.getPartnerConnection(orgInfo)

            def descrProcesses = connection.describeSObject(objectName);

            def fields = [] as Set<String>

            descrProcesses.getFields().each { field ->
                if (field.isUpdateable()) {
                    fields.add(field.getName())
                }
            }

            allEditableFields.get(orgInfo.id).get(objectName).addAll(fields)

        }

        return allEditableFields.get(orgInfo.id).get(objectName)

    }

    def getAllFields(orgInfo, objectName) {
        if (! allFields) {
            allFields = [:]
        }
        if (! allFields.containsKey(orgInfo.id) || ! allFields.get(orgInfo.id).containsKey(objectName)) {
            if (! allFields.containsKey(orgInfo.id)) {
                allFields.put(orgInfo.id, [:])
            }

            if (! allFields.get(orgInfo.id).containsKey(objectName)) {
                allFields.get(orgInfo.id).putAt(objectName, [])
            }

            def connection = this.getPartnerConnection(orgInfo)

            def descrProcesses = connection.describeSObject(objectName);

            allFields.get(orgInfo.id).get(objectName).addAll(descrProcesses.getFields())

        }

        return allFields.get(orgInfo.id).get(objectName)

    }

    def getAllFieldsUpperCase(orgInfo, objectName) {

        if (! allFieldsUpperCase) {
            allFieldsUpperCase = [:]
        }

        if (! allFieldsUpperCase.containsKey(orgInfo.id) || ! allFieldsUpperCase.get(orgInfo.id).containsKey(objectName)) {
            if (! allFieldsUpperCase.containsKey(orgInfo.id)) {
                allFieldsUpperCase.put(orgInfo.id, [:])
            }

            if (! allFieldsUpperCase.get(orgInfo.id).containsKey(objectName)) {
                allFieldsUpperCase.get(orgInfo.id).putAt(objectName, [])
            }

            def connection = this.getPartnerConnection(orgInfo)

            def descrProcesses = connection.describeSObject(objectName);

            def fields = [] as Set<String>

            descrProcesses.getFields().each { field ->
                fields.add(field.getName().toUpperCase())
            }

            allFieldsUpperCase.get(orgInfo.id).get(objectName).addAll(fields)

        }

        return allFieldsUpperCase.get(orgInfo.id).get(objectName)


    }


}
