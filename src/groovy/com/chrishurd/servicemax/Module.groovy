package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 7/18/12
 * Time: 5:23 PM
 */
class Module {

    def module
    def submodules = [:]
    def submodulesByModuleId = [:]
    def profiles = [:]
    def profilesByName = [:]
    def globalProfile

    def getProfile(name) {
        if (profilesByName.isEmpty() && ! profiles.isEmpty()) {
            profiles.values().each { profile ->
                profilesByName.put(profile.getField('SVMXC__Profile_Name__c'), profile)
            }
        }
        return profilesByName.get(name)
    }

    def getSubmodule(name) {
        if (submodulesByModuleId.isEmpty() && ! submodules.isEmpty()) {
            submodules.values().each { submodule ->
                submodulesByModuleId.put(submodule.submodule.getField('SVMXC__SubmoduleID__c'), submodule)
            }
        }

        return submodulesByModuleId.get(name)
    }

}
