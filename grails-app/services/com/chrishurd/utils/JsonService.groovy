package com.chrishurd.utils

/*
 *
 * Mostly stolen from Gregg Bolinger
 */
class JsonService {

    static transactional = false
    def grailsApplication

    def filterForGrid(clazz, listOptions, filters = null) {
        def instances = clazz.createCriteria().list(listOptions) {
            if (filters) {
                and {
                    filters.rules.each { rule ->
                        ilike(rule.field, "${rule.data}%")
                    }
                }
            }
        }
        return instances
    }


    def preparePostResponse(domainInstance) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def postResponse = new AjaxPostResponse(domainObject: domainInstance)
        if (domainInstance.hasErrors()) {
            domainInstance.errors.allErrors.each { error ->
                postResponse.errors."${error.field}" = g.message(error: error)
            }
            postResponse.success = false
            postResponse.message = "There was an error"
        } else {
            postResponse.success = true
            postResponse.message = "Success"
        }
        return postResponse
    }

    def preparePostResponse(domainInstance, html) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def postResponse = new AjaxPostResponse(domainObject: domainInstance, html: html)
        if (domainInstance.hasErrors()) {
            domainInstance.errors.allErrors.each { error ->
                postResponse.errors."${error.field}" = g.message(code: "${domainInstance.class.name}.${error.field}.${domainInstance.errors.getFieldError(error.field).code}.error")
            }
            postResponse.success = false
            postResponse.message = "There was an error"
        } else {
            postResponse.success = true
            postResponse.message = "Success"
        }
        return postResponse
    }

    def prepareErrorPostResponse(String message) {
        def postResponse = new AjaxPostResponse()
        postResponse.errors.error = message
        postResponse.success = false
        postResponse.message = "There was an error"
        return postResponse
    }
}
