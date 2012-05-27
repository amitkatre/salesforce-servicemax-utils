package com.chrishurd.salesforce

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/26/12
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
class MetadataType {
    def type
    def managedObjects = []
    def unmanagedObjects = []

    def addObject(obj) {
        if (obj.managed) {
            managedObjects.add(obj)
        }
        else {
            unmanagedObjects.add(obj)
        }
    }
}
