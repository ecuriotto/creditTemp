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

import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder

class CreateDisputeAuthorization implements RestApiController, BPMNamesConstants{

    @Override
    public RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder,
            RestAPIContext context) {
        def processAPI = context.apiClient.getProcessAPI()
        def body = new JsonBuilder([ canCreateDispute:userCanStartProcess(processAPI, context.apiSession.userId, DISPUTE_PROCESS_NAME) ])
        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, body.toString())
    }

    def boolean userCanStartProcess(ProcessAPI processAPI, long userId, String processName) {
        return processAPI.searchUsersWhoCanStartProcessDefinition(processAPI.getLatestProcessDefinitionId(processName), new SearchOptionsBuilder(0,Integer.MAX_VALUE).with {
            filter(UserSearchDescriptor.ID, userId)
            done()
        }).count > 0
    }

    def RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder
                .withResponseStatus(httpStatus)
                .withResponse(body)
                .build()
    }
}
