package com.bonitasoft.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class NewCaseActivity implements RestApiController, BPMNamesConstants {

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
					setDisplayName( "&#x2795 $jsonBody.name" )
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
	
	def HumanTaskInstance findTaskInstance(long caseId, String name, ProcessAPI processAPI) {
		def result = processAPI.searchHumanTaskInstances(new SearchOptionsBuilder(0, 1).with {
			filter(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, caseId)
			filter(HumanTaskInstanceSearchDescriptor.NAME, name)
			done()
		}).getResult()
		return result.isEmpty() ? null : result[0]
	}
	
}
