package com.chrishurd.salesforce

import com.sforce.soap.metadata.ListMetadataQuery
import com.sforce.soap.metadata.ManageableState

class MetadataService {

    def connectionService
    def grailsApplication

    def describeMetadata(orgInfo) {

        def connection = connectionService.getMetedataConnection(orgInfo)
        def metadataObjectTypes = ["AccountCriteriaBasedSharingRule", "AccountOwnerSharingRule", "ApexClass", "ApexComponent",
            "ApexPage", "ApexTrigger", "CampaignCriteriaBasedSharingRule", "CampaignOwnerSharingRule", "CaseCriteriaBasedSharingRule",
            "CaseOwnerSharingRule", "ContactCriteriaBasedSharingRule", "ContactOwnerSharingRule", "CustomApplication",
            "CustomLabels", "CustomObject", "CustomObjectCriteriaBasedSharingRule", "CustomObjectOwnerSharingRule",
            "CustomObjectTranslation", "CustomPageWebLink", "CustomSite", "CustomTab", "Dashboard", "DataCategoryGroup",
            "Document", "EmailTemplate", "Flow", "Group", "HomePageComponent", "HomePageLayout", "Layout",
            "LeadCriteriaBasedSharingRule", "LeadOwnerSharingRule", "Letterhead", "OpportunityCriteriaBasedSharingRule",
            "OpportunityOwnerSharingRule", "PermissionSet", "Portal", "Profile", "Queue", "RemoteSiteSetting",
            "Report", "ReportType", "Role", "Scontrol", "StaticResource", "Translations", "Workflow"]
        def queries = new ListMetadataQuery[3]
        def metadataMap = new HashMap<String, MetadataType>()
        def count = 0;

        metadataObjectTypes.eachWithIndex { objType, index ->
            def query = new ListMetadataQuery();
            query.setType(objType)
            queries[count] = query

            if (count == 2 || index == metadataObjectTypes.size() - 1) {
                connection.listMetadata(queries, Double.valueOf(grailsApplication.config.sfdc.api.version)).each { obj ->
                    if (! metadataMap.containsKey(obj.type)) {
                        def type = new MetadataType();
                        type.type = obj.type
                        metadataMap.put(obj.type, type)
                    }

                    def mObj = new MetadataObject()
                    mObj.createdBy = obj.createdByName
                    mObj.createdOn = obj.createdDate
                    mObj.id = obj.id
                    mObj.lastModifiedBy = obj.lastModifiedByName
                    mObj.lastModifiedOn = obj.lastModifiedDate
                    if (obj.manageableState.equals(ManageableState.installed) || obj.manageableState.equals(ManageableState.released)) {
                        mObj.managed = true
                    }
                    mObj.name = obj.fullName
                    mObj.namespace = obj.namespacePrefix
                    metadataMap.get(obj.type).addObject(mObj)
                }
                count = -1
                queries = new ListMetadataQuery[3]
            }
            ++count
        }

        return metadataMap
    }

    def getMetadataDir(orgInfo) {

    }
}
