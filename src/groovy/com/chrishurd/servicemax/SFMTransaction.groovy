package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 7/13/12
 * Time: 10:46 PM
 */
class SFMTransaction {

    def sfmTransaction
    def objectMappings = [:]
    def namedExpressions = [:]
    def pageLayout
    def nodeTargets = [:]
    def sourceUpdates = [:]

    def fields = [:]


    def addFields(obj, objFields) {
        if (obj) {
            if (! fields.containsKey(obj)) {
                fields.put(obj, [] as Set<String>)
            }

            fields.get(obj).addAll(objFields)
        }
    }

}
