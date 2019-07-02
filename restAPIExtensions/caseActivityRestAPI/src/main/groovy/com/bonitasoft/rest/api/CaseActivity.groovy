package com.bonitasoft.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.bpm.data.DataInstance
import org.bonitasoft.engine.bpm.data.DataNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.bpm.flownode.StandardLoopCharacteristics
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder
import groovy.sql.DataSet
import javassist.bytecode.stackmap.BasicBlock.Catch

class CaseActivity implements RestApiController,CaseActivityHelper,BPMNamesConstants {

	private static final String PREFIX = '$'

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def caseId = request.getParameter "caseId"
		if (!caseId) {
			return buildResponse(responseBuilder, 
				HttpServletResponse.SC_BAD_REQUEST,
				"""{"error" : "the parameter caseId is missing"}""")
		}

		def ProcessAPI processAPI = context.apiClient.getProcessAPI()
		def pDef = processAPI.getProcessDefinition(processAPI.getProcessDefinitionIdFromProcessInstanceId(caseId.toLong()))
		
		//Retrieve pending activities
		def result = processAPI.getPendingHumanTaskInstances(context.apiSession.userId,0, Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC)
				.findAll{ it.name != ACTIVITY_CONTAINER && it.name != CREATE_ACTIVITY && it.parentProcessInstanceId ==  caseId.toLong() }
				.collect{ toActivity(it, getACMStateValue(it,processAPI), pDef, request.contextPath) }


		def containerInstance = findTaskInstance(caseId.toLong(), ACTIVITY_CONTAINER, processAPI)
		
		//Retrieve Manual tasks
		result.addAll(processAPI.searchHumanTaskInstances(new SearchOptionsBuilder(0, Integer.MAX_VALUE)
				.filter(HumanTaskInstanceSearchDescriptor.PARENT_CONTAINER_ID, containerInstance.id)
				.done())
				.result
				.collect{ toActivity(it, OPTIONAL_STATE, pDef, request.contextPath) })
				
		result = result.sort{ a1,a2 -> valueOfState(a1.acmState) <=> valueOfState(a2.acmState) }

		//Append finished tasks
		result.addAll(processAPI.searchArchivedHumanTasks(new SearchOptionsBuilder(0, Integer.MAX_VALUE).with {
			filter(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, caseId)
			differentFrom(ArchivedHumanTaskInstanceSearchDescriptor.NAME, ACTIVITY_CONTAINER)
			differentFrom(ArchivedHumanTaskInstanceSearchDescriptor.NAME, CREATE_ACTIVITY)
			done()
		}).getResult()
		.findAll{ //remove finished loop task instances
			it.parentActivityInstanceId == 0 || !isAnArchivedLoopInstance(it, processAPI)
         }
		.collect{ ArchivedHumanTaskInstance task ->
			[
				name:task.displayName ?: task.name,
				description:task.description,
				bpmState:task.state.capitalize()
			]
		})

		buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
	}
	
	def toActivity(HumanTaskInstance task, String acmState, ProcessDefinition pDef, String contextPath) {
		[
			name:task.displayName ?: task.name,
			url: canExecute(acmState) ? forge(pDef.name,pDef.version,task, contextPath) : null,
			description:task.description,
			target:linkTarget(task),
			bpmState:task.state.capitalize(),
			acmState:acmState
		]
	}
	

	def String forge(String processName,String processVersion,ActivityInstance instance, contextPath) {		
			if(instance instanceof UserTaskInstance) {
				"$contextPath/portal/resource/taskInstance/$processName/$processVersion/$instance.name/content/?id=$instance.id&displayConfirmation=false"
			}else if(instance instanceof ManualTaskInstance) {
				"$contextPath/apps/cases/do?id=$instance.id"
			}
	}

	def String linkTarget(ActivityInstance instance) {
		if(instance instanceof UserTaskInstance) {
			'_self'
		}else if(instance instanceof ManualTaskInstance) {
			'_parent'
		}
	}

	RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
		return responseBuilder.with {
			withResponseStatus(httpStatus)
			withResponse(body)
			build()
		}
	}


}
