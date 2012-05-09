package com.chrishurd.utils

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/8/12
 * Time: 6:26 PM
 */
class AjaxPostResponse {
    boolean success
    String message
    String html
    def domainObject
    def errors = [:]
}
