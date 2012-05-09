package com.chrishurd.utils

/**
 * Created with IntelliJ IDEA.
 * User: churd
 * Date: 5/8/12
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 * Mostly stolen from Gregg Bolinger
 */
class GridUtils {
    static getGridModel(count, rows, page, sidx, sord) {
        int totalPages
        if (count > 0) {
            totalPages = Math.ceil(count / rows)
        } else {
            totalPages = 0
        }

        if (page.toInteger() > totalPages) {
            page = totalPages
        }

        def start = rows.toInteger() * page.toInteger() - rows.toInteger()


        def gridModel = new GridModel()
        gridModel.gridOptions = [offset: start, max: rows, sort: sidx, order: sord]

        gridModel.grid = new Grid(page: page, total: totalPages, records: count)
        return gridModel
    }
}
