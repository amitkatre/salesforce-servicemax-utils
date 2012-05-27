package com.chrishurd.salesforce

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/26/12
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
 */
class MetadataObject {

    def createdOn
    def lastModifiedOn
    def createdBy
    def lastModifiedBy
    def name
    def filename
    def id
    def namespace
    def managed = false



    def static nameComparator = [
        compare:{ a, b -> a.name <=> b.name }
    ] as Comparator



}
