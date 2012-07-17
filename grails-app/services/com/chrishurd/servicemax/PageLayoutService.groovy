package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class PageLayoutService {

    static scope = "request"
    def connectionService
    def namedSearchService

    def getPageLayoutByLayoutId(orgInfo, layoutId) {
        def pageLayout = new PageLayout()

        connectionService.retrieveObject(orgInfo, 'SVMXC__Page_Layout__c', " SVMXC__Page_Layout_ID__c = '${layoutId}' ").each { record ->
            pageLayout.pageLayout = record
            pageLayout.obj = record.getField('SVMXC__Object_Name__c')
        }

        if (pageLayout.pageLayout != null) {
            this.populatePageLayoutDetails(orgInfo, pageLayout);
            return pageLayout
        }
        else {
            return null
        }
    }

    def getPageLayout(orgInfo, id) {
        def pageLayout = new PageLayout()

        connectionService.retrieveObject(orgInfo, 'SVMXC__Page_Layout__c', " ID = '${id}' ").each { record ->
            pageLayout.pageLayout = record
            pageLayout.obj = record.getField('SVMXC__Object_Name__c')
        }

        if (pageLayout.pageLayout != null) {
            this.populatePageLayoutDetails(orgInfo, pageLayout);
            return pageLayout
        }
        else {
            return null
        }

    }

    def populatePageLayoutDetails(orgInfo, pageLayout) {

        def namedSearchIds = [] as Set<String>

        if (pageLayout.pageLayout.getField('SVMXC__Multi_Add_Configuration__c') != null) {
            namedSearchIds.add(pageLayout.pageLayout.getField('SVMXC__Multi_Add_Configuration__c'))
        }


        connectionService.retrieveObject(orgInfo, 'SVMXC__SFM_Event__c', " SVMXC__Page_Layout__c = '${pageLayout.pageLayout.getId()}' ").each { record ->
            pageLayout.events.put(record.getId(), record)
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__Page_Layout_Detail__c', " SVMXC__Page_Layout__c = '${pageLayout.pageLayout.getId()}' ").each { record ->
            if ('Section'.equals(record.getField('SVMXC__Detail_Type__c'))) {
                pageLayout.sections.put(record.getId(), record)
            }
            else {
                pageLayout.pageDetails.put(record.getId(), record)

                if (record.getField('SVMXC__Field_API_Name__c') != null) {
                    pageLayout.fields.add(record.getField('SVMXC__Field_API_Name__c'))
                }

                if (record.getField('SVMXC__Named_Search__c') != null) {
                    namedSearchIds.add(record.getField('SVMXC__Named_Search__c'))
                }
            }
        }

        if (! pageLayout.pageDetails.isEmpty()) {
            connectionService.retrieveObject(orgInfo, 'SVMXC__SFM_Event__c', " SVMXC__Page_Layout_Detail__c IN ('${pageLayout.pageDetails.keySet().join("', '")}') ").each { record ->
                pageLayout.events.put(record.getId(), record)
            }
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__Page_Layout__c', " SVMXC__Header_Page_Layout__c = '${pageLayout.pageLayout.getId()}' ").each { record ->

            if (record.getField('SVMXC__Multi_Add_Configuration__c')) {
                namedSearchIds.add(record.getField('SVMXC__Multi_Add_Configuration__c'))
            }
            def childPage = this.getChildPageDetails(orgInfo, record)
            namedSearchIds.addAll(childPage.namedSearchIds)

            pageLayout.childrenLayout.put(childPage.pageLayout.getId(), childPage)
        }

        if (! pageLayout.childrenLayout.isEmpty() ) {
            connectionService.retrieveObject(orgInfo, 'SVMXC__SFM_Event__c', " SVMXC__Page_Layout__c IN ('${pageLayout.childrenLayout.keySet().join("', '")}') ").each { record ->
                pageLayout.events.put(record.getId(), record)
            }
        }
        namedSearchIds.each { searchId ->
            def namedSearch = namedSearchService.getNamedSearch(orgInfo, searchId)
            pageLayout.namedSearches.put(searchId, namedSearch)
        }
    }

    def getChildPageDetails(orgInfo, pageLayout) {
        def childPageLayout = new PageLayout()
        childPageLayout.pageLayout = pageLayout

        childPageLayout.obj = pageLayout.getField('SVMXC__Object_Name__c')

        connectionService.retrieveObject(orgInfo, 'SVMXC__Page_Layout_Detail__c', " SVMXC__Page_Layout__c = '${pageLayout.getId()}' ").each { record ->
            childPageLayout.pageDetails.put(record.getId(), record)

            if (record.getField('SVMXC__Field_API_Name__c') != null) {
                childPageLayout.fields.add(record.getField('SVMXC__Field_API_Name__c'))
            }

            if (record.getField('SVMXC__Named_Search__c') != null) {
                childPageLayout.namedSearchIds.add(record.getField('SVMXC__Named_Search__c'))
            }
        }

        connectionService.retrieveObject(orgInfo, 'SVMXC__SFM_Event__c', " SVMXC__Page_Layout_Detail__c IN ('${childPageLayout.pageDetails.keySet().join("', '")}') ").each { record ->
            childPageLayout.events.put(record.getId(), record)
        }

        return childPageLayout
    }

    def migratePageLayout(orgInfo, pageLayout) {
        def toPageLayout = this.getPageLayoutByLayoutId(orgInfo, pageLayout.pageLayout.getField('SVMXC__Page_Layout_ID__c'))
        def newPageLayout = new SObject()
        newPageLayout.setType('SVMXC__Page_Layout__c')

        if (toPageLayout) {
            toPageLayout.originalId = pageLayout.pageLayout.getId()
            if ("false".equals(toPageLayout.pageLayout.getField('SVMXC__IsStandard__c'))) {
                this.deletePageLayoutDetails(orgInfo, toPageLayout)
                newPageLayout.setId(toPageLayout.pageLayout.getId())
            }
            else {
                if (! toPageLayout.childrenLayout.isEmpty()) {
                    def newMap = [:]
                    toPageLayout.childrenLayout.values().each { pl ->
                        pageLayout.childrenLayout.values().each { oldPL ->
                            if (pl.pageLayout.getField('SVMXC__Page_Detail_UniqueId__c').equals(oldPL.pageLayout.getField('SVMXC__Page_Detail_UniqueId__c'))) {
                                newMap.put(oldPL.pageLayout.getId(), pl)
                            }
                        }
                    }
                    toPageLayout.childrenLayout = newMap
                }
                return toPageLayout
            }
        }
        else {
            toPageLayout = new PageLayout()
            toPageLayout.originalId = pageLayout.pageLayout.getId()
        }

        connectionService.migrateObject(orgInfo, pageLayout.pageLayout, newPageLayout, 'SVMXC__Page_Layout__c')

        pageLayout.namedSearches.keySet().each { namedSearchId ->
            def namedSearch = pageLayout.namedSearches.get(namedSearchId)

            toPageLayout.namedSearches.put(namedSearchId, namedSearchService.migrateNamedSearch(orgInfo, namedSearch))
        }

        if (pageLayout.pageLayout.getField('SVMXC__Multi_Add_Configuration__c') != null) {
            newPageLayout.setField('SVMXC__Multi_Add_Configuration__c', toPageLayout.namedSearches.get(pageLayout.pageLayout.getField('SVMXC__Multi_Add_Configuration__c')).namedSearch.getId())
        }

        connectionService.updateObjects(orgInfo, [newPageLayout])
        toPageLayout.pageLayout = newPageLayout

        if (! pageLayout.childrenLayout.isEmpty()) {
            pageLayout.childrenLayout.keySet().each { childId ->
                def child = pageLayout.childrenLayout.get(childId)
                def newChildLayout = new PageLayout()
                def newChild = new SObject()
                newChild.setType('SVMXC__Page_Layout__c')
                connectionService.migrateObject(orgInfo, child.pageLayout, newChild, 'SVMXC__Page_Layout__c')
                newChild.setField('SVMXC__Header_Page_Layout__c', newPageLayout.getId())

                if (child.pageLayout.getField('SVMXC__Multi_Add_Configuration__c') != null) {
                    newChild.setField('SVMXC__Multi_Add_Configuration__c', toPageLayout.namedSearches.get(child.pageLayout.getField('SVMXC__Multi_Add_Configuration__c')).namedSearch.getId())
                }

                connectionService.updateObjects(orgInfo, [newChild])
                newChildLayout.pageLayout = newChild

                if (! child.pageDetails.isEmpty()) {
                    child.pageDetails.keySet().each { fieldObjId ->
                        def fieldObj = child.pageDetails.get(fieldObjId)
                        def newFieldObj = this.copyFieldObj(orgInfo, newChild, fieldObj, toPageLayout)
                        newChildLayout.pageDetails.put(fieldObjId, newFieldObj)
                    }

                    connectionService.updateObjects(orgInfo, newChildLayout.pageDetails.values())
                }

                if (! child.events.isEmpty()) {
                    child.events.keySet().each { eventId ->
                        def event = child.events.get(eventId)
                        newChildLayout.events.put(eventId, this.copyEvent(orgInfo, newChildLayout, event))
                    }

                    connectionService.updateObjects(orgInfo, newChildLayout.events.values())
                }

                toPageLayout.childrenLayout.put(childId, newChildLayout)
            }
        }

        if (! pageLayout.sections.isEmpty()) {
            pageLayout.sections.keySet().each { sectionId ->
                def section = pageLayout.sections.get(sectionId)
                def newSection = this.copyFieldObj(orgInfo, newPageLayout, section, toPageLayout )
                toPageLayout.sections.put(sectionId, newSection)
            }

            connectionService.updateObjects(orgInfo, toPageLayout.sections.values())
        }

        if (! pageLayout.pageDetails.isEmpty()) {
            pageLayout.pageDetails.keySet().each { pdId ->
                def pageDetail = pageLayout.pageDetails.get(pdId)
                def newPageDetail = this.copyFieldObj(orgInfo, newPageLayout, pageDetail, toPageLayout)
                toPageLayout.pageDetails.put(pdId, newPageDetail)
            }

            connectionService.updateObjects(orgInfo, toPageLayout.pageDetails.values())
        }

        if (! pageLayout.events.isEmpty()) {
            pageLayout.events.keySet().each { eventId ->
                def event = pageLayout.events.get(eventId)
                def newEvent = this.copyEvent(orgInfo, toPageLayout, event)
                toPageLayout.events.put(eventId, newEvent)
            }

            toPageLayout.events.keySet().each { eventId ->
                println(eventId)
                connectionService.updateObjects(orgInfo, [toPageLayout.events.get(eventId)])
            }
            //connectionService.updateObjects(orgInfo, toPageLayout.events.values())
        }

        return toPageLayout
    }


    def copyEvent(orgInfo, pageLayout, event) {
        def newEvent = new SObject()
        newEvent.setType('SVMXC__SFM_Event__c')
        connectionService.migrateObject(orgInfo, event, newEvent, 'SVMXC__SFM_Event__c')

        if (event.getField('SVMXC__Page_Layout__c') != null) {
            if (pageLayout.originalId.equals(event.getField('SVMXC__Page_Layout__c'))) {
                newEvent.setField('SVMXC__Page_Layout__c', pageLayout.pageLayout.getId())
            }
            else if (! pageLayout.childrenLayout.isEmpty() && pageLayout.childrenLayout.containsKey(event.setField('SVMXC__Page_Layout__c'))) {
                newEvent.setField('SVMXC__Page_Layout__c', pageLayout.childrenLayout.get(event.setField('SVMXC__Page_Layout__c')).pageLayout.getId())
            }
        }

        if (event.getField('SVMXC__Page_Layout_Detail__c') != null && pageLayout.pageDetails.containsKey(event.getField('SVMXC__Page_Layout_Detail__c'))) {
            newEvent.setField('SVMXC__Page_Layout_Detail__c', pageLayout.pageDetails.get(event.getField('SVMXC__Page_Layout_Detail__c')).getId())
        }

        return newEvent

    }

    def copyFieldObj(orgInfo, parent, fieldObj, pageLayout) {
        def newFieldObj = new SObject()
        newFieldObj.setType('SVMXC__Page_Layout_Detail__c')
        connectionService.migrateObject(orgInfo, fieldObj, newFieldObj, 'SVMXC__Page_Layout_Detail__c')
        newFieldObj.setField('SVMXC__Page_Layout__c', parent.getId())

        if (fieldObj.getField('SVMXC__Named_Search__c') != null) {
            def namedSearch = pageLayout.namedSearches.get(fieldObj.getField('SVMXC__Named_Search__c'))
            newFieldObj.setField('SVMXC__Named_Search__c', namedSearch.namedSearch.getId())
        }

        if (fieldObj.getField('SVMXC__Section__c') != null) {
            newFieldObj.setField('SVMXC__Section__c', pageLayout.sections.get(fieldObj.getField('SVMXC__Section__c')).getId())
        }

        return newFieldObj
    }

    def deletePageLayoutDetails(orgInfo, pageLayout) {
        if (! pageLayout.childrenLayout.isEmpty()) {
            pageLayout.childrenLayout.values().each { child ->
                if (! child.events.isEmpty()) {
                    connectionService.delete(orgInfo, child.events.keySet() as String[])
                }

                if (! child.pageDetails.isEmpty()) {
                    connectionService.delete(orgInfo, child.pageDetails.keySet() as String[])
                }
            }

            connectionService.delete(orgInfo, pageLayout.childrenLayout.keySet() as String[])
            pageLayout.childrenLayout = [:]
        }

        if (! pageLayout.sections.isEmpty()) {
            connectionService.delete(orgInfo, pageLayout.sections.keySet() as String[])
            pageLayout.sections = [:]
        }

        if (! pageLayout.pageDetails.isEmpty()) {
            connectionService.delete(orgInfo, pageLayout.pageDetails.keySet() as String[])
            pageLayout.pageDetails = [:]
        }

        if (! pageLayout.events.isEmpty()) {
            connectionService.delete(orgInfo, pageLayout.events.keySet() as String[])
            pageLayout.events = [:]
        }

        pageLayout.namedSearches = [:]

    }
}
