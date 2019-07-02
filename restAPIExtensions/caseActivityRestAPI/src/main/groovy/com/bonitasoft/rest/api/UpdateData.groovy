package com.bonitasoft.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.DisputeDAO

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class UpdateData implements RestApiController, CaseActivityHelper, BPMNamesConstants{

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
			def jsonBody = new JsonSlurper().parse(request.getReader())
			def processAPI = context.apiClient.getProcessAPI()
			if(!jsonBody.caseId) {
				return responseBuilder.with {
					withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
					withResponse("No caseId in payload")
					build()
				}
			}
			if(!jsonBody.taskName) {
				return responseBuilder.with {
					withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
					withResponse("No taskName in payload")
					build()
				}
			}
			if(!jsonBody.fieldsToUpdate) {
				return responseBuilder.with {
					withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
					withResponse("No fieldsToUpdate in payload")
					build()
				}
			}
			def task = findTaskInstance(jsonBody.caseId.toLong(), jsonBody.taskName, processAPI)
			processAPI.assignAndExecuteUserTask(context.apiSession.userId, task.id, jsonBody)
			return responseBuilder.with {
				withResponseStatus(HttpServletResponse.SC_OK)
				withResponse(new JsonBuilder([lastUpdateDate:System.currentTimeMillis().toString()]).toString())
				build()
			}
	}

	
}
