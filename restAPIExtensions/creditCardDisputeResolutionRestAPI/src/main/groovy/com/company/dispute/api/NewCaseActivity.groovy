/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class NewCaseActivity implements RestApiController, CaseActivityHelper, BPMNamesConstants {

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        def jsonBody = new JsonSlurper().parse(request.getReader())
        if(!jsonBody.name) {
            return responseBuilder.with {
                withResponse("""{"error" : "the parameter name is missing"}""")
                withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
                build()
            }
        }
        if(!jsonBody.caseId) {
            return responseBuilder.with {
                withResponse("""{"error" : "the parameter caseId is missing"}""")
                withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
                build()
            }
        }

        def processAPI = context.apiClient.getProcessAPI()

        def activityContainerInstance = findTaskInstance(jsonBody.caseId.toLong(), ACTIVITY_CONTAINER, processAPI)
        if(!activityContainerInstance) {
            return responseBuilder.with {
                withResponseStatus(HttpServletResponse.SC_NOT_FOUND)
                withResponse("No $ACTIVITY_CONTAINER found")
                build()
            }
        }

        processAPI.assignUserTask(activityContainerInstance.id, context.apiSession.userId)
        processAPI
                .addManualUserTask(new ManualTaskCreator(activityContainerInstance.id, jsonBody.name).with{
                    setDisplayName( "&#x1f4a1; $jsonBody.name" )
                    setDescription(jsonBody.description)
                    setAssignTo(context.apiSession.userId)
                    setDueDate(null)
                })


        def createActivityInstance = findTaskInstance(jsonBody.caseId.toLong(), CREATE_ACTIVITY, processAPI)
        if(!createActivityInstance) {
            return responseBuilder.with {
                withResponseStatus(HttpServletResponse.SC_NOT_FOUND)
                withResponse("No $CREATE_ACTIVITY found")
                build()
            }
        }
        processAPI.assignAndExecuteUserTask(context.apiSession.userId, createActivityInstance.id, [name:jsonBody.name])

        return responseBuilder.with {
            withResponse(new JsonBuilder([name:jsonBody.name]).toString())
            withResponseStatus(HttpServletResponse.SC_CREATED)
            build()
        }
    }
}
