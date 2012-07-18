package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 7/14/12
 * Time: 7:16 PM
 */
class SFMObjectMapping {

    def objectMapping
    def fieldMappings = [:]

    def targetObject
    def sourceObject
    def targetFields = [] as Set<String>
    def sourceFields = [] as Set<String>


}
