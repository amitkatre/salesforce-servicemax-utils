package com.chrishurd.salesforce

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/29/12
 * Time: 4:54 PM
 */
public enum MetadataEnum {

    ACCOUNT_CRITERIA_BASED_SHARING_RULE("AccountCriteriaBasedSharingRule", true, false),
    ACCOUNT_OWNER_SHARING_RULE("AccountOwnerSharingRule", true, false),
    APEX_CLASS("ApexClass", true, false),
    APEX_COMPONENT("ApexComponent", true, false),
    APEX_PAGE("ApexPage", true, false),
    APEX_TRIGGER("ApexTrigger",true, false),
    CAMPAIGN_CRITERIA_BASED_SHARING_RULE("CampaignCriteriaBasedSharingRule", true, false),
    CAMPAIGN_OWNER_SHARING_RULE("CampaignOwnerSharingRule", true, false),
    CASE_CRITERIA_BASES_SHARING_RULE("CaseCriteriaBasedSharingRule", true, false),
    CASE_OWNER_SHARING_RULE("CaseOwnerSharingRule",true, false),
    CONTACT_CRITERIA_BASED_SHARING_RULE("ContactCriteriaBasedSharingRule", true, false),
    CONTACT_OWNER_SHARING_RULE("ContactOwnerSharingRule", true, false),
    CUSTOM_APPLICATION("CustomApplication", true, false),
    CUSTOM_LABELS("CustomLabels", true, false),
    CUSTOM_OBJECT("CustomObject", false, false),
    CUSTOM_OBJECT_CRITERIA_BASED_SHARING_RULE("CustomObjectCriteriaBasedSharingRule", false, false),
    CUSTOM_OBJECT_TRANSLATION("CustomObjectTranslation", false, false),
    CUSTOM_PAGE_WEB_LINK("CustomPageWebLink", true, false),
    CUSTOM_SITE("CustomSite", true, false),
    CUSTOM_TAB("CustomTab", true, false),
    Dashboard("Dashboard", true, false),
    DATA_CATEGORY_GROUP("DataCategoryGroup", true, false),
    Document("Document", true, true),
    EMAIL_TEMPLATE("EmailTemplate", true, true),
    Flow("Flow", true, false),
    Group("Group", true, false),
    HOME_PAGE_COMPONENT("HomePageComponent",true, false),
    HOME_PAGE_LAYOUT("HomePageLayout",true, false),
    LAYOUT("Layout", true, false),
    LEAD_CRITERIA_BASED_SHARING_RULE("LeadCriteriaBasedSharingRule", true, false),
    LEAD_OWNER_SHARING_RULE("LeadOwnerSharingRule", true, false),
    LETTER_HEAD("Letterhead", true, false),
    OPPORTUNITY_CRITERIA_BASED_SHARING_RULE("OpportunityCriteriaBasedSharingRule", true, false),
    OPPORTUNITY_OWNER_SHARING_RULE("OpportunityOwnerSharingRule", true, false),
    PERMISSION_SET("PermissionSet", true, false),
    PORTAL("Portal", true, false),
    PROFILE("Profile", true, false),
    QUEUE("Queue", true, false),
    REMOTE_SITE_SETTINGS("RemoteSiteSetting", true, false),
    REPORT("Report", true, true),
    REPORT_TYPE("ReportType", true, false),
    ROLE("Role", true, false),
    SCONTROLE("Scontrol", true, false),
    STATIC_RESOURCE("StaticResource", true, false),
    TRANSLATIONS("Translations", true, false),
    WORKFLOW("Workflow", true, false)

    public boolean wildcard
    public boolean hierarchical
    public String objectType

    MetadataEnum(String objectType, boolean wildcard, boolean hierarchical) {
        this.wildcard = wildcard
        this.hierarchical = hierarchical
        this.objectType = objectType
    }

}