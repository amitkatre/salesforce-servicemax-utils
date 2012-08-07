package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class ProfileService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getCustomProfiles(orgInfo) {
        def migrationObjects = []

        connectionService.query(orgInfo, "SELECT Id, SVMXC__Profile_Name__c, SVMXC__Configuration_Type__c, LastModifiedDate FROM SVMXC__ServiceMax_Config_Data__c WHERE SVMXC__IsDefault__c = false AND SVMXC__Active__c = true AND SVMXC__RecordType_Name__c = 'Configuration Profile' ORDER BY SVMXC__Profile_Name__c ").each { record ->
            def mObj = new MigrationObject()
            mObj.type = 'profile'
            mObj.id = record.getId()
            mObj.name = "${record.getField('SVMXC__Profile_Name__c')} (${record.getField('SVMXC__Configuration_Type__c')})"
            mObj.modifiedDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SS", record.getField('LastModifiedDate'))
            migrationObjects.add(mObj)
        }

        return migrationObjects
    }

    def migrationProfile(fromOrgInfo, toOrgInfo, id) {
        def fromProfile = this.getProfile(fromOrgInfo, id)

        def toProfile = this.getProfileByName(toOrgInfo, fromProfile.profile.getField('SVMXC__Profile_Name__c'))
        def errors = [] as Set<Object>

        if (! fromProfile.configAccess.isEmpty()) {
            fromProfile.configAccess.values().each { cfgAccess ->
                if (fromProfile.sfProfileLookup.containsKey(cfgAccess.getField('SVMXC__Profile__c')) && ! toProfile.sfProfileLookup.containsKey(fromProfile.sfProfileLookup.get(cfgAccess.getField('SVMXC__Profile__c')))) {
                    errors.add("Missing profile '${fromProfile.sfProfileLookup.get(cfgAccess.getField('SVMXC__Profile__c'))}'")
                }
            }
        }

        if (! fromProfile.settings.isEmpty()) {
            fromProfile.settings.values().each { setting ->
                if (! toProfile.settingLookup.containsKey(fromProfile.settingLookup.get(setting.getField('SVMXC__Setting_ID__c')))) {
                    errors.add("Missing setting '${fromProfile.settingLookup.get(setting.getField('SVMXC__Setting_ID__c'))}' ")
                }
            }
        }

        if (! errors.isEmpty()) {
            return errors
        }

        def newProfile = new SObject()
        newProfile.setType('SVMXC__ServiceMax_Config_Data__c')
        newProfile.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Configuration Profile'))
        if (toProfile.profile) {
            if (! toProfile.settings.isEmpty()) {
                connectionService.delete(toOrgInfo, toProfile.settings.keySet() as String[])
                toProfile.settings.clear()
            }

            if (! toProfile.configAccess.isEmpty()) {
                connectionService.delete(toOrgInfo, toProfile.configAccess.keySet() as String[])
                toProfile.configAccess.clear()
            }

            newProfile.setId(toProfile.profile.getId())
        }

        connectionService.migrateObject(toOrgInfo, fromProfile.profile, newProfile, 'SVMXC__ServiceMax_Config_Data__c')
        connectionService.updateObjects(toOrgInfo, [newProfile])
        toProfile.profile = newProfile

        if (! fromProfile.settings.isEmpty()) {
            fromProfile.settings.values().each { setting ->
                if (toProfile.settingLookup.containsKey(fromProfile.settingLookup.get(setting.getField('SVMXC__Setting_ID__c')))) {
                    def newSetting = new SObject()
                    newSetting.setType('SVMXC__ServiceMax_Config_Data__c')
                    newSetting.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Setting Value'))
                    connectionService.migrateObject(toOrgInfo, setting, newSetting, 'SVMXC__ServiceMax_Config_Data__c')
                    newSetting.setField('SVMXC__Setting_Configuration_Profile__c', newProfile.getId())
                    newSetting.setField('SVMXC__Setting_ID__c', toProfile.settingLookup.get(fromProfile.settingLookup.get(setting.getField('SVMXC__Setting_ID__c'))))
                    toProfile.settings.put(setting.getId(), newSetting)
                }
            }

            connectionService.updateObjects(toOrgInfo, toProfile.settings.values())
        }

        if (! fromProfile.configAccess.isEmpty()) {
            fromProfile.configAccess.values().each { access ->
                if (fromProfile.sfProfileLookup.containsKey(access.getField('SVMXC__Profile__c'))) {
                    def newAccess = new SObject()
                    newAccess.setType('SVMXC__ServiceMax_Config_Data__c')
                    newAccess.setField('RecordTypeId', recordTypeService.getRecordTypeId(toOrgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Configuration Access'))
                    connectionService.migrateObject(toOrgInfo, access, newAccess, 'SVMXC__ServiceMax_Config_Data__c')
                    newAccess.setField('SVMXC__Access_Configuration_Profile__c', newProfile.getId())
                    newAccess.setField('SVMXC__Profile__c', toProfile.sfProfileLookup.get(fromProfile.sfProfileLookup.get(access.getField('SVMXC__Profile__c'))))
                    toProfile.configAccess.put(access.getId(), newAccess)
                }
            }

            connectionService.updateObjects(toOrgInfo, toProfile.configAccess.values())
        }

        return toProfile

    }

    def getProfileByName(orgInfo, name) {
        def profile = new Profile()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Profile_Name__c = '${name}' ").each { record ->
            profile.profile = record
        }

        if (profile.profile) {
            this.populateProfileDetails(orgInfo, profile)
        }

        this.populateLookupDetails(orgInfo, profile)

        return profile
    }


    def getProfile(orgInfo, id) {
        def profile = new Profile()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " Id = '${id}' ").each { record ->
            profile.profile = record
        }

        this.populateLookupDetails(orgInfo, profile)
        this.populateProfileDetails(orgInfo, profile)

        return profile

    }

    def populateLookupDetails(orgInfo, profile) {

        connectionService.query(orgInfo, " SELECT Id, SVMXC__Setting_Unique_ID__c FROM SVMXC__ServiceMax_Processes__c WHERE SVMXC__Record_Type_Name__c = 'Settings' ").each { record ->
            profile.settingLookup.put(record.getField('SVMXC__Setting_Unique_ID__c'), record.getId())
            profile.settingLookup.put(record.getId(), record.getField('SVMXC__Setting_Unique_ID__c'))

        }

        connectionService.query(orgInfo, 'SELECT Id, Name FROM Profile').each { record ->
            profile.sfProfileLookup.put(record.getField('Name'), record.getId())
            profile.sfProfileLookup.put(record.getId(), record.getField('Name'))
        }

    }

    def  populateProfileDetails(orgInfo, profile) {

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Access_Configuration_Profile__c = '${profile.profile.getId()}' AND SVMXC__RecordType_Name__c = 'Configuration Access' ").each { record ->
            profile.configAccess.put(record.getId(), record)
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Setting_Configuration_Profile__c = '${profile.profile.getId()}' AND  SVMXC__RecordType_Name__c = 'Setting Value' ").each { record ->
            profile.settings.put(record.getId(), record)
        }
    }
}
