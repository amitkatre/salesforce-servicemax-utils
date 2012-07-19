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

        connection.delete(ids)
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

        def error
        if (! insertObjs.isEmpty()) {
            error = insert(orgInfo, insertObjs as SObject[])
            if (error) {
                return error
            }
        }

        if (! updateObjs.isEmpty()) {
            error = update(orgInfo, updateObjs as SObject[])
            if (error) {
                return error
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
            println(field.getName() + "     " + field.isUpdateable())
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

        def connection = this.getPartnerConnection(orgInfo)

        def descrProcesses = connection.describeSObject(objectName);

        def fields = [] as Set<String>

        descrProcesses.getFields().each { field ->
            if (field.isUpdateable()) {
                fields.add(field.getName())
            }
        }

        return fields
    }

    def getAllFields(orgInfo, objectName) {
        def connection = this.getPartnerConnection(orgInfo)

        def descrProcesses = connection.describeSObject(objectName);

        return descrProcesses.getFields()
    }

    def getAllFieldsUpperCase(orgInfo, objectName) {

        def connection = this.getPartnerConnection(orgInfo)

        def descrProcesses = connection.describeSObject(objectName);

        def fields = [] as Set<String>

        descrProcesses.getFields().each { field ->
            fields.add(field.getName().toUpperCase())
        }

        return fields
    }


}
