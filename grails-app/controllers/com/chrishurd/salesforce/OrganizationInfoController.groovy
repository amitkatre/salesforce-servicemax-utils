package com.chrishurd.salesforce

import grails.converters.JSON
import com.chrishurd.utils.GridUtils
import com.chrishurd.utils.GridRow
import com.chrishurd.security.User

class OrganizationInfoController {

    def domainService
    def jsonService
    def organizationInfoService
    def connectionService

    def index = { }

    def list = {
        withFormat {
            html {

            }
            json {
                def row
                int page = params.page.toInteger()
                int rows = params.rows.toInteger()
                def sidx = params.sidx
                if (!params.sidx) {
                    sidx = 'id'
                }
                def sord = params.sord

                def orgInfoList = OrganizationInfo.createCriteria().list {
                    eq('user', domainService.getUserDomain())
                    order('name', 'asc')
                }

                def gridModel = GridUtils.getGridModel(orgInfoList?.size(), rows, page, sidx, sord)

                orgInfoList?.each { orgInfo ->
                    row = new GridRow(id: "${orgInfo.id}")
                    row.cell = [
                        orgInfo.id.toString(),
                        orgInfo.name,
                        (orgInfo.sandbox ? 'Test' : 'Prod'),
                        "<span class=\"edit-organizationInfo ui-icon ui-icon-pencil {id: '${orgInfo.id}'}\" ></span>"
                             + "<span class=\"delete-organizationInfo ui-icon ui-icon-trash {id: '${orgInfo.id}'}\" ></span>"
                             + "<span class=\"check-organzationInfo ui-icon ui-icon-check {id: '${orgInfo.id}'}\" ></span>"
                    ]
                    gridModel.grid.rows.add(row)
                }

                render new JSON(gridModel.grid)
            }
        }
    }

    def edit = {
        def orgInfo
        if (params.id) {
            orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
        }
        else {
            orgInfo = new OrganizationInfo()
        }

        return [orgInfo : orgInfo]
    }



    def delete = {
        def jsonResponse
        def id = params.id
        def orgInfo = OrganizationInfo.get(Long.valueOf(id))
        try {

            organizationInfoService.deleteOrganizationInfo(orgInfo)
        }
        catch (Exception exp) {
            jsonResponse = [success: 'false']
            render new JSON(jsonResponse)
            return
        }

        jsonResponse = [success: 'true']
        render new JSON(jsonResponse)
    }

    def checkConnection = {
        def orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
        def jsonResponse

        try {
            connectionService.getPartnerConnection(orgInfo)
        }
        catch (Exception ex) {
            ex.printStackTrace()
            render new JSON(jsonService.prepareErrorPostResponse(ex.getMessage()))
            return
        }

        render  new JSON([success: 'true'])

    }

    def save = {
        withFormat {
            json {
                def orgInfo
                if (params.id) {
                    orgInfo = OrganizationInfo.get(Long.valueOf(params.id))
                }
                else {
                    orgInfo = new OrganizationInfo()
                    orgInfo.user = domainService.getUserDomain()
                }

                orgInfo.name = params.name
                orgInfo.username = params.username
                orgInfo.password = params.password
                orgInfo.securityToken = params.securityToken

                if (params.sandbox) {
                    orgInfo.sandbox = true
                }
                else {
                    orgInfo.sandbox = false
                }

                orgInfo.save(flush: true)

                render new JSON(jsonService.preparePostResponse(orgInfo))
            }
        }
    }
}
