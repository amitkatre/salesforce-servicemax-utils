package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/11/12
 * Time: 8:04 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ProcessesEnum {

    WIZARD_STEP('Wizard_Step', []),
    DELIVERY_RULE('Delivery_Rule', []),
    DISPATCH_PROCESS('Dispatch_Process', []),
    FORWARD_SHIPMENT_PROCESS('Forward_Shipment_Process', []),
    FULFILLMENT_PROCESS('Fulfillment_Process', []),
    INITIAL_RETURN_PROCESS('Initiate_Return_Process', []),
    INVENTORY_PROCESS('Inventory_Process', []),
    MODULE('Module', []),
    NAMED_SEARCH('Named_Search', []),
    OBJECT_MAPPING('Object_Mapping', []),
    OUTBOUND_ROUTE_CALCULATION('Outbound_Route_Calculation', []),
    PROCESS_NODE_OBJECTS('Process_Node_Objects', []),
    RECEIVING_PROCESS('Receiving_Process', []),
    SF_ACTION('SF_Action', []),
    SVMX_RULE('SVMX_Rule', []),
    SETTINGS('Settings', []),
    SHIPPING_PROCESS('Shipping_Process', []),
    SUBMODULE('Submodule', []),
    TARGET_MANAGER('Target_Manager', []),
    VIEW_DEFINITION('View_Definition', []),
    WIZARD('Wizard', [])

    private final String type
    private final String[] fieldNames

    ProcessesEnum(String type, String fieldNames) {
        this.type = type;
        this.fieldNames = fieldNames
    }

    public String type() { return type }
    public String fieldNames() { return fieldNames }






}
