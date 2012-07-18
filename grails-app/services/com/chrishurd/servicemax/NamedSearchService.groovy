package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class NamedSearchService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getNamedSearchByProcessId(orgInfo, processId) {
        def namedSearch = new NamedSearch()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__ProcessID__c = '${processId}' ").each { record ->
            namedSearch.namedSearch = record
        }

        if (namedSearch.namedSearch != null) {
            this.populateNamedSearchDetails(orgInfo, namedSearch)
            return namedSearch
        }
    }

    def getNamedSearch(orgInfo, id) {
        def namedSearch = new NamedSearch()

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " ID = '${id}' ").each { record ->
            namedSearch.namedSearch = record
        }

        if (namedSearch.namedSearch != null) {
            this.populateNamedSearchDetails(orgInfo, namedSearch)
            return namedSearch
        }
    }

    def populateNamedSearchDetails(orgInfo, namedSearch) {
        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Processes__c', " SVMXC__Module__c = '${namedSearch.namedSearch.getId()}' ").each { record ->
            namedSearch.searchObjects.put(record.getId(), this.getSearchObject(orgInfo, record))
        }
    }

    def getSearchObject(orgInfo, searchObj) {

        def searchObject = new SearchObject()
        searchObject.searchObject = searchObj;
        searchObject.obj = searchObj.getField('SVMXC__Source_Object_Name__c')

        connectionService.retrieveObject(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', " SVMXC__Expression_Rule__c = '${searchObj.getId()}' ").each {record ->
            searchObject.objectFields.put(record.getId(), record);
            if (record.getField('SVMXC__Field_Name__c') != null) {
                searchObject.fields.add(record.getField('SVMXC__Field_Name__c'))
            }
        }

        return searchObject
    }

    def migrateNamedSearch(orgInfo, namedSearch) {
        def toNamedSearch = this.getNamedSearchByProcessId(orgInfo, namedSearch.namedSearch.getField('SVMXC__ProcessID__c'))
        def newNamedSearchObj = new SObject()
        newNamedSearchObj.setType('SVMXC__ServiceMax_Processes__c')

        if (toNamedSearch) {
            if ("false".equals(toNamedSearch.namedSearch.getField('SVMXC__IsStandard__c'))) {
                if (! toNamedSearch.searchObjects.isEmpty()) {
                    toNamedSearch.searchObjects.values().each { so ->
                        if (! so.objectFields.isEmpty()) {
                            connectionService.delete(orgInfo, so.objectFields.keySet() as String[])
                        }
                    }

                    connectionService.delete(orgInfo, toNamedSearch.searchObjects.keySet() as String[])
                    toNamedSearch.searchObjects = [:]
                }
                connectionService.delete(orgInfo, toNamedSearch.searchObjects.keySet() as String[])
                toNamedSearch.searchObjects = [:]
                newNamedSearchObj.setId(toNamedSearch.namedSearch.getId())
            }
            else {
                return toNamedSearch
            }
        }
        else {
            toNamedSearch = new NamedSearch()
        }

        newNamedSearchObj.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'Named Search'))
        connectionService.migrateObject(orgInfo, namedSearch.namedSearch, newNamedSearchObj, 'SVMXC__ServiceMax_Processes__c')
        connectionService.updateObjects(orgInfo, [newNamedSearchObj])
        toNamedSearch.namedSearch = newNamedSearchObj

        namedSearch.searchObjects.keySet().each { soId ->
            def searchObject = namedSearch.searchObjects.get(soId)
            def nsSearchObject = new SearchObject()
            def newSearchObject = new SObject()
            newSearchObject.setType('SVMXC__ServiceMax_Processes__c')
            newSearchObject.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Processes__c', 'Named Search'))
            connectionService.migrateObject(orgInfo, searchObject.searchObject, newSearchObject, 'SVMXC__ServiceMax_Processes__c')
            newSearchObject.setField('SVMXC__Module__c', newNamedSearchObj.getId())
            connectionService.insert(orgInfo, [newSearchObject] as SObject[])

            if (! searchObject.objectFields.isEmpty()) {
                searchObject.objectFields.keySet().each { ofId ->
                    def objectField = searchObject.objectFields.get(ofId)
                    def newObjectField = new SObject()
                    newObjectField.setType('SVMXC__ServiceMax_Config_Data__c')
                    newObjectField.setField('RecordTypeId', recordTypeService.getRecordTypeId(orgInfo, 'SVMXC__ServiceMax_Config_Data__c', 'Expressions'))
                    connectionService.migrateObject(orgInfo, objectField, newObjectField, 'SVMXC__ServiceMax_Config_Data__c')
                    newObjectField.setField('SVMXC__Expression_Rule__c', newSearchObject.getId())
                    nsSearchObject.objectFields.put(ofId, newObjectField)
                }

                connectionService.insert(orgInfo, nsSearchObject.objectFields.values() as SObject[])

            }

            toNamedSearch.searchObjects.put(soId, nsSearchObject)
        }

        return toNamedSearch

    }
}
