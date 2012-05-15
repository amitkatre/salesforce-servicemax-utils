package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/11/12
 * Time: 8:28 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ConfigDataEnum {

    CONFIGURATION_ACCESS('Configuration_Access', []),
    CONFIGURATION_PROFILE('Configuration_Profile', []),
    EXPRESSIONS('Expressions', []),
    FIELD_MAPPING('Field_Mapping', []),
    FULFILLMENT_PATH('Fulfillment_Path', []),
    MOBILE_CONFIGURATION('Mobile_Configuration', []),
    OFFLINE_PROFILE('Offline_Profile', []),
    ROUTE_STOPS('Route_Stops', []),
    SF_ACTION_PARAMETER('SF_Action_Parameter', []),
    SCHEDULE('Schedule', []),
    SEARCH_ACCESS('Search_Access', []),
    SEARCH_FLOW_ACCESS('Service_Flow_Access', []),
    SETTING_VALUE('Setting_Value', []),
    VIEW_ACCESS('View_Access', []),
    WIZARD_ACCESS('Wizard_Access', []),
    WIZARD_LAYOUT('Wizard Layout', [])

    private final String type
    private final String[] fieldNames

    ConfigDataEnum(String type, String fieldNames) {
        this.type = type;
        this.fieldNames = fieldNames
    }

    public String type() { return type }
    public String fieldNames() { return fieldNames }


}