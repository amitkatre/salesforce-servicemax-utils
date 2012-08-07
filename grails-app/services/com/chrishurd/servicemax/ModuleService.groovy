package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class ModuleService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getCustomModules(orgInfo) {
        def migrationMap = [:]
        def moduleIds = [] as Set<String>
        connectionService.query(orgInfo, "SELECT Id, SVMXC__ModuleID__c, SVMXC__Name__c, SVMXC__Module__c, SVMXC__Record_Type_Name__c, LastModifiedDate FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__Record_Type_Name__c IN ('Module', 'Submodule', 'Settings') AND SVMXC__IsStandard__c = false ORDER BY SVMXC__Name__c ").each { record ->
            if (record.getField('SVMXC__Record_Type_Name__c').equals('Module')) {
                def migrObj = new MigrationObject()
                migrObj.type = 'module'
                migrObj.id = record.getId()
                migrObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ModuleID__c')})"
                migrObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
                migrationMap.put(record.getId(), migrObj)
            }
            else {
                if (record.getField('SVMXC__Module__c') != null) {
                    moduleIds.add(record.getField('SVMXC__Module__c'))
                }
            }
        }

        connectionService.query(orgInfo, "SELECT Id, SVMXC__Module__c FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__Record_Type_Name__c = 'Submodule' AND Id IN (SELECT SVMXC__Submodule__c FROM  SVMXC__ServiceMax_Tags__c WHERE (SVMXC__IsCustom__c = true OR SVMXC__Language__c != 'Master' )) ").each { record ->
            moduleIds.add(record.getField('SVMXC__Module__c'))
        }
        connectionService.query(orgInfo, "SELECT Id, SVMXC__ModuleID__c, SVMXC__Name__c, SVMXC__Module__c, SVMXC__Record_Type_Name__c, LastModifiedDate FROM SVMXC__ServiceMax_Processes__c WHERE Id IN ('${moduleIds.join("', '")}') ").each { record ->
                def migrObj = new MigrationObject()
                migrObj.type = 'module'
                migrObj.id = record.getId()
                migrObj.name = "${record.getField('SVMXC__Name__c')} (${record.getField('SVMXC__ModuleID__c')})"
                migrObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
                migrationMap.put(record.getId(), migrObj)
        }


        return migrationMap.values().sort { a, b -> a.name.compareTo(b.name)}
    }

    def migrateModule(fromOrgInfo, toOrgInfo, id) {
        def fromModule = this.getModule(fromOrgInfo, id)

        def toModule = this.getModuleByModuleId(toOrgInfo, fromModule.module.getField('SVMXC__ModuleID__c'))

        return this.migrateModuleDetails(toOrgInfo, fromModule, toModule)
    }

    def migrateModuleDetails(toOrgInfo, fromModule, toModule) {
        def newModule = new SObject()
        newModule.setType('SVMXC__ServiceMax_Processes__c')
        newModule.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Module'))
        if (toModule) {
            this.cleanModule(toOrgInfo, toModule)
            newModule.setId(toModule.module.getId())
        }
        else {
            toModule = new Module()
        }

        if ("false".equals(newModule.getField('SVMXC__IsStandard__c'))) {
            connectionService.migrateObject(toOrgInfo, fromModule.module, newModule, 'SVMXC__ServiceMax_Processes__c')
            connectionService.updateObjects(toOrgInfo, [newModule])
        }

        toModule.module = newModule

        if (! fromModule.submodules.isEmpty()) {
            fromModule.submodules.keySet().each { submoduleId ->
                def submodule = fromModule.submodules.get(submoduleId)
                def toSubmodule = toModule.getSubmodule(submodule.submodule.getField('SVMXC__SubmoduleID__c'))
                def newSubmodule = new SObject()
                def newSubId
                newSubmodule.setType('SVMXC__ServiceMax_Processes__c')
                newSubmodule.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Submodule'))
                if (toSubmodule) {
                    newSubmodule.setId(toSubmodule.submodule.getId())
                    newSubId = toSubmodule.submodule.getId()
                }
                else {
                    toSubmodule = new Submodule()
                }

                if (newSubId == null || "false".equals(submodule.submodule.getField('SVMXC__IsStandard__c'))) {
                    connectionService.migrateObject(toOrgInfo, submodule.submodule, newSubmodule, 'SVMXC__ServiceMax_Processes__c')
                    newSubmodule.setField('SVMXC__Module__c', newModule.getId())
                    if (newSubmodule.getField('SVMXC__SubmoduleID__c').size() < 8) {
                        newSubmodule.setField('SVMXC__Installation_Key__c', toOrgInfo.orgId.bytes.encodeBase64().toString())
                    }
                    connectionService.updateObjects(toOrgInfo, [newSubmodule])
                    newSubId = newSubmodule.getId()
                }

                toSubmodule.submodule = newSubmodule

                if (! submodule.tags.isEmpty()) {
                    submodule.tags.values().each { tag ->
                        def newTag = new SObject()
                        newTag.setType('SVMXC__ServiceMax_Tags__c')
                        connectionService.migrateObject(toOrgInfo, tag, newTag, 'SVMXC__ServiceMax_Tags__c')
                        newTag.setField('SVMXC__Submodule__c', newSubId)
                        newTag.setField('Name', tag.getField('Name'))
                        toSubmodule.tags.put(tag.getId(), newTag)
                    }

                    connectionService.updateObjects(toOrgInfo, toSubmodule.tags.values())
                }

                if (! submodule.settings.isEmpty()) {
                    submodule.settings.values().each { setting ->
                        def newSetting = new SObject()
                        newSetting.setType('SVMXC__ServiceMax_Processes__c')
                        newSetting.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Processes__c', 'Settings'))
                        connectionService.migrateObject(toOrgInfo, setting, newSetting, 'SVMXC__ServiceMax_Processes__c')
                        newSetting.setField('SVMXC__Module__c', newModule.getId())
                        newSetting.setField('SVMXC__Submodule__c', newSubId)
                        if (newSetting.getField('SVMXC__SettingID__c').size() < 8) {
                            newSetting.setField('SVMXC__Installation_Key__c', toOrgInfo.orgId.bytes.encodeBase64().toString())
                        }

                        toSubmodule.settings.put(setting.getId(), newSetting)
                    }

                    connectionService.updateObjects(toOrgInfo, toSubmodule.settings.values())
                }

                if (! submodule.configData.isEmpty()) {
                    submodule.configData.values().each { configData ->
                        def newConfigData = new SObject()
                        newConfigData.setType('SVMXC__ServiceMax_Config_Data__c')
                        newConfigData.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Setting Value'))
                        connectionService.migrateObject(toOrgInfo, configData, newConfigData, 'SVMXC__ServiceMax_Config_Data__c')
                        newConfigData.setField('SVMXC__Setting_ID__c', toSubmodule.settings.get(configData.getField('SVMXC__Setting_ID__c')).getId())
                        if (fromModule.globalProfile.getId().equals(configData.getField('SVMXC__Setting_Configuration_Profile__c'))) {
                            newConfigData.setField('SVMXC__Setting_Configuration_Profile__c', toModule.globalProfile.getId())
                            toSubmodule.configData.put(configData.getId(), newConfigData)
                        }
                        else if (fromModule.profiles.containsKey(configData.getField('SVMXC__Setting_Configuration_Profile__c'))) {
                            def prof = fromModule.profiles.get(configData.getField('SVMXC__Setting_Configuration_Profile__c'))
                            def newProf = toModule.getProfile(prof.getField('SVMXC__Profile_Name__c'))
                            if (newProf != null) {
                                newConfigData.setField('SVMXC__Setting_Configuration_Profile__c', newProf.getId())
                                toSubmodule.configData.put(configData.getId(), newConfigData)
                            }
                        }
                    }

                    if (! toSubmodule.configData.isEmpty()) {
                        connectionService.updateObjects(toOrgInfo, toSubmodule.configData.values())
                    }
                }

                toModule.submodules.put(submoduleId, toSubmodule)

            }
        }

        return toModule
    }

    def cleanModule(orgInfo, module) {
        def submodules = [:]
        def deleteSubmodules = []
        if (! module.submodules.isEmpty) {
            module.submodules.keySet().each { submoduleId ->
                def submodule = module.submodules.get(submoduleId)
                if (! submodule.tags.isEmpty()) {
                    connectionService.delete(orgInfo, submodule.tags.keySet() as String[])
                    submodule.tags = [:]
                }

                if (! submodule.configData.isEmpty()) {
                    connectionService.delete(orgInfo, submodule.configData.keySet() as String[])
                    submodule.configData = [:]
                }

                if (! submodule.settings.isEmpty()) {
                    connectionService.delete(orgInfo, submodule.settings.keySet() as String[])
                    submodule.settings = [:]
                }

                if ("false".equals(submodule.submodule.getField('SVMXC__IsStandard__c'))) {
                    deleteSubmodules.add(submodule.submodule.getId())
                }
                else {
                    submodules.put(submoduleId, submodule)
                }
            }

            module.submodules = submodules
        }

        if (! deleteSubmodules.isEmpty()) {
            connectionService.delete(orgInfo, deleteSubmodules as String[])
        }
    }

    def getModuleByModuleId(orgInfo, moduleId) {
        def module = new Module()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ModuleID__c = '${moduleId}' AND SVMXC__Record_Type_Name__c = 'Module' ").each { record ->
            module.module = record
        }

        if (module.module) {
            this.populateModuleDetails(orgInfo, module)
            this.populateModuleProfileDetails(orgInfo, module)
            return module
        }

        return null

    }

    def getModule(orgInfo, id) {
        def module = new Module()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " Id = '${id}' ").each { record ->
            module.module = record
        }

        if (module.module) {
            this.populateModuleDetails(orgInfo, module)
            this.populateModuleProfileDetails(orgInfo, module)
            return module
        }

        return null
    }

    def populateModuleDetails(orgInfo, module) {

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Module__c = '${module.module.getId()}' AND SVMXC__Record_Type_Name__c = 'Submodule' ").each { record ->
            module.submodules.put(record.getId(), this.getSubmodule(orgInfo, record))
        }
    }

    def populateModuleProfileDetails(orgInfo, module) {
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__RecordType_Name__c = 'Configuration Profile' AND (SVMXC__IsDefault__c = true OR (SVMXC__Active__c = true AND SVMXC__Configuration_Type__c = 'Global')) ").each {record ->
            if ("Global".equals(record.getField('SVMXC__Configuration_Type__c')) && 'true'.equals(record.getField('SVMXC__Active__c'))) {
                module.globalProfile = record
            }
            else {
                module.profiles.put(record.getId(), record)
            }
        }
    }

    def getSubmodule(orgInfo, submoduleObj) {
        def submodule = new Submodule()
        submodule.submodule = submoduleObj

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Tags__c', " SVMXC__Submodule__c = '${submoduleObj.getId()}' AND ( SVMXC__Language__c != 'Master' OR SVMXC__IsCustom__c = true ) ").each { record ->
            submodule.tags.put(record.getId(), record)
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Submodule__c = '${submoduleObj.getId()}' AND SVMXC__Record_Type_Name__c = 'Settings' AND SVMXC__IsStandard__c = false ").each { record ->
            submodule.settings.put(record.getId(), record)
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Setting_ID__c IN ('${submodule.settings.keySet().join("', '")}') AND SVMXC__RecordType_Name__c = 'Setting Value' ").each { record ->
            submodule.configData.put(record.getId(), record)
        }

        return submodule

    }
}
