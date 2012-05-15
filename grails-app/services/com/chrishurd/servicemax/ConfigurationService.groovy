package com.chrishurd.servicemax

import com.chrishurd.salesforce.SObjectWrapper

class ConfigurationService {

    static scope = "session"
    def connectionService

    def orgConfigurationMap = [:]

    def getConfiguration(orgInfo) {
        if (! orgConfigurationMap.containsKey(orgInfo.name)) {
            return retrieveConfiguration(orgInfo)
        }
        else {
            orgConfigurationMap.get(orgInfo.name)
        }
    }

    def retrieveConfiguration(orgInfo) {

        orgConfigurationMap.put(orgInfo.name, new OrgConfiguration())

        def orgConfig =  orgConfigurationMap.get(orgInfo.name)

        retrievePageLayouts(orgInfo, orgConfig)
        retrievePageLayoutDetails(orgInfo, orgConfig)
        retrieveProcesses(orgInfo, orgConfig)
        retrieveConfigData(orgInfo, orgConfig)

        return orgConfigurationMap.get(orgInfo.name)

    }

    def retrievePageLayouts(orgInfo, orgConfig) {
        def fields = connectionService.getAllEditableFields(orgInfo, 'SVMXC__Page_Layout__c') as Set<String>
        fields.add('Name')

        connectionService.query(orgInfo, "SELECT Id, ${fields.join(',')} FROM SVMXC__Page_Layout__c ").each { record ->
            orgConfig.pageLayoutDetails.put(record.getId(), new SObjectWrapper(record.getId(), record.getField('Name'), record))
        }

    }

    def retrievePageLayoutDetails(orgInfo, orgConfig) {

        def fields = connectionService.getAllEditableFields(orgInfo, 'SVMXC__Page_Layout_Detail__c') as Set<String>
        fields.add('Name')

        connectionService.query(orgInfo, "SELECT Id, ${fields.join(',')} FROM SVMXC__Page_Layout_Detail__c ").each { record ->
            orgConfig.pageLayouts.put(record.getId(), new SObjectWrapper(record.getId(), record.getField('Name'), record))
        }

    }

    def retrieveProcesses(orgInfo, orgConfig) {

        connectionService.query(orgInfo, "SELECT Id, Name FROM RecordType WHERE SOBJECTTYPE = 'SVMXC__ServiceMax_Processes__c' ").each { obj ->
            orgConfig.processesRTMap.put(obj.getField('Id'), obj.getField('Name'))
        }

        def fields = connectionService.getAllEditableFields(orgInfo, 'SVMXC__ServiceMax_Processes__c') as Set<String>
        fields.add('Name')
        fields.add('RecordTypeId')

        connectionService.query(orgInfo, "SELECT Id, ${fields.join(',')} FROM SVMXC__ServiceMax_Processes__c ").each { record ->
            def type = orgConfig.processesRTMap.get(record.getField('RecordTypeId'))

            orgConfig.processes.put(record.getId(), new SObjectWrapper(record.getId(), type, record.getField('Name'), record))
        }
    }

    def retrieveConfigData(orgInfo, orgConfig) {

        connectionService.query(orgInfo, "SELECT Id, Name FROM RecordType WHERE SOBJECTTYPE = 'SVMXC__ServiceMax_Config_Data__c' ").each { obj ->
            orgConfig.configDataRTMap.put(obj.getField('Id'), obj.getField('Name'))
        }

        def fields = connectionService.getAllEditableFields(orgInfo, 'SVMXC__ServiceMax_Config_Data__c') as Set<String>
        fields.add('Name')
        fields.add('RecordTypeId')

        connectionService.query(orgInfo, "SELECT Id, ${fields.join(',')} FROM SVMXC__ServiceMax_Config_Data__c ").each { record ->
            def type = orgConfig.configDataRTMap.get(record.getField('RecordTypeId'))

            orgConfig.configData.put(record.getId(), new SObjectWrapper(record.getId(), type, record.getField('Name'), record))
        }
    }
}
