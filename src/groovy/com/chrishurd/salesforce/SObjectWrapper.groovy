package com.chrishurd.salesforce

import com.sforce.soap.partner.sobject.SObject

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/13/12
 * Time: 3:32 PM
 */
class SObjectWrapper {

    def name
    def type
    def referencedBy = [] as Set<SObject>
    def currentSObject
    def newSObject
    def copy  = false
    def oldId
    def id

    def SObjectWrapper(id, type, name,  sobject) {
        this.name = name
        this.type =  type
        this.id = id
        this.sobject = currentSObject
    }

    def SObjectWrapper(id, name,  sobject) {
        this.name = name
        this.id = id
        this.sobject = currentSObject
    }

}
