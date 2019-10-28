/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.creditcard.dispute.model.Customer
import com.company.creditcard.dispute.model.CustomerDAO

import groovy.json.JsonBuilder

class CustomerInfo implements RestApiController, CaseActivityHelper {

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        def caseId = request.getParameter "caseId"
        if (!caseId) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter caseId is missing"}""")
        }

        def processAPI = context.apiClient.getProcessAPI()
        def searcBusinessData = newSearchBusinessData(processAPI)
        def Customer customer  = searcBusinessData.search(caseId.toLong(), 'customer_ref', context.apiClient.getDAO(CustomerDAO))
        if(!customer) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"error" : "no customer found for case $caseId"}""")
        }

        return responseBuilder.with {
            withResponseStatus(HttpServletResponse.SC_OK)
            withResponse(new JsonBuilder(
                    [
                        customerId:customer.customerId,
                        firstName:customer.firstName,
                        lastName:customer.lastName,
                        email:customer.email,
                        phoneNumber:customer.phoneNumber,
                        accountId:customer.account.id
                    ]
                    ).toString())
            build()
        }
    }

    def SearchBusinessData newSearchBusinessData(ProcessAPI processAPI) {
        new SearchBusinessData(processAPI)
    }

    def buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder.with {
            withResponseStatus(httpStatus)
            withResponse(body)
            build()
        }
    }
}
