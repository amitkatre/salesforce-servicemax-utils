package com.chrishurd.servicemax

import com.sforce.soap.partner.sobject.SObject

class CodeSnippetService {

    static scope = "request"
    def connectionService
    def recordTypeService

    def getCodeSnippetBySnippetId(orgInfo, snippetId) {
        def codeSnippet = new CodeSnippet()

        connectionService.retrieveObject(orgInfo, 'SVMXC__Code_Snippet__c', " SVMXC__SnippetId__c = '${snippetId}' ").each { record ->
            codeSnippet.codeSnippet = record
        }

        return codeSnippet
    }

    def getCodeSnippet(orgInfo, id) {
        def codeSnippet = new CodeSnippet()

        connectionService.retrieveObject(orgInfo, 'SVMXC__Code_Snippet__c', " ID = '${id}' ").each { record ->
            codeSnippet.codeSnippet = record
        }

        return codeSnippet
    }

    def migrateCodeSnippet(orgInfo, codeSnippet) {
        def toCodeSnippet = this.getCodeSnippetBySnippetId(orgInfo, codeSnippet.codeSnippet.getField('SVMXC__SnippetId__c'))
        def newCodeSnippet = new SObject()
        newCodeSnippet.setType('SVMXC__Code_Snippet__c')

        if (toCodeSnippet.codeSnippet) {
            if ("false".equals(toCodeSnippet.codeSnippet.getField('SVMXC__IsStandard__c'))) {
                newCodeSnippet.setId(toCodeSnippet.codeSnippet.getId())
            }
            else {
                return toCodeSnippet
            }
        }
        else {
            toCodeSnippet = new CodeSnippet()
        }

        connectionService.migrateObject(orgInfo, codeSnippet.codeSnippet, newCodeSnippet, 'SVMXC__Code_Snippet__c')
        connectionService.updateObjects(orgInfo, [newCodeSnippet])
        toCodeSnippet.codeSnippet = newCodeSnippet

        return toCodeSnippet

    }
}
