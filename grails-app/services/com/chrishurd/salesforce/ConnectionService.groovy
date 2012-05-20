package com.chrishurd.salesforce

import com.sforce.soap.partner.Connector
import com.sforce.soap.partner.CallOptions_element
import com.sforce.ws.ConnectorConfig
import java.util.regex.Pattern
import com.sforce.soap.metadata.MetadataConnection
import com.sforce.async.BulkConnection

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


}
