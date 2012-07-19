package com.chrishurd.servicemax

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 7/18/12
 * Time: 5:24 PM
 */
class Submodule {

    def submodule
    def settings = [:]
    def settingsByName = [:]
    def tags = [:]
    def configData = [:]

    def getSetting(name) {
        if (settingsByName.isEmpty() && ! settings.isEmpty()) {
            settings.values().each {setting ->
                settingsByName.put(setting.getField('SVMXC__SettingID__c'), settings)
            }
        }

        return settingsByName.get(name)
    }
}


