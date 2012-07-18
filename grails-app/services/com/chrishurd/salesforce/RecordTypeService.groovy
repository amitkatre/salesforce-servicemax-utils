package com.chrishurd.salesforce

class RecordTypeService {

    static scope = "session"
    def connectionService

    def recordTypeOrgMap = [:]

    def getRecordTypeId(orgInfo, String objectName, String recordType) {
        if (! recordTypeOrgMap.containsKey(orgInfo.id)) {
            recordTypeOrgMap.put(orgInfo.id, [:])
        }

        def recordTypeObjMap = recordTypeOrgMap.get(orgInfo.id)

        def recordTypeMap
        if (! recordTypeObjMap.containsKey(objectName)) {
            recordTypeMap = [:]
            connectionService.query(orgInfo, "SELECT Id, Name FROM RecordType WHERE sObjectType = '${objectName}' ").each { record ->
                recordTypeMap.put(record.getField('Name'), record.getId())
            }

            recordTypeObjMap.put(objectName, recordTypeMap)
        }
        else {
            recordTypeMap = recordTypeObjMap.get(objectName)
        }

        if (recordTypeMap) {
            return recordTypeMap.get(recordType)
        }
        else {
            return null
        }
    }
}
