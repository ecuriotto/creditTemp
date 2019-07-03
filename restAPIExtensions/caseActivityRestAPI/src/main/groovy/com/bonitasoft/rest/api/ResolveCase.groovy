package com.bonitasoft.rest.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.DisputeDAO

import groovy.json.JsonSlurper

class ResolveCase implements RestApiController,CaseActivityHelper {

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def jsonBody = new JsonSlurper().parse(request.getReader())
		if(!jsonBody.caseId){
			return responseBuilder.with {
				withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
				withResponse("""{"error" : "the parameter caseId is missing"}""")
				build()
			}
		}
		if(!jsonBody.status){
			return responseBuilder.with {
				withResponseStatus(HttpServletResponse.SC_BAD_REQUEST)
				withResponse("""{"error" : "the parameter status is missing"}""")
				build()
			}
		}
		
		def processAPI = context.apiClient.getProcessAPI()
		def id = jsonBody.caseId.toLong()
		
		def processInstanceContext = processAPI.getProcessInstanceExecutionContext(id)
		def dispute_ref = processInstanceContext['dispute_ref']
		def com.company.model.Dispute dispute
		if(dispute_ref) {
			def disputeDAO = context.apiClient.getDAO(DisputeDAO)
			dispute = disputeDAO.findByPersistenceId(dispute_ref.storageId)
		}
		if(!dispute) {
			return  responseBuilder.with {
				withResponseStatus(HttpServletResponse.SC_NOT_FOUND)
				withResponse("""{"error" : "no dispute found for case $caseId"}""")
				build()
			}
		}
		
		def contract = [
			disputeInput:[
				status:jsonBody.status,
				txDate:dispute.txDate,
				amount:dispute.amount,
				currency:dispute.currency,
				merchantIdNumber:dispute.merchantIdNumber
			]
		]
		executeUpdateDataTask(processAPI, id, 'Update Dispute', contract, context.apiSession.userId)
		
		processAPI.cancelProcessInstance(id)
	
		def result = processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 1).with {
			filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, id)
			done()
		}).getResult()
		if(result && result.size() > 0) {
			id = result[0]
		}
		return responseBuilder.with {
			withResponseStatus(HttpServletResponse.SC_OK)
			withResponse(id)
			build()
		}
	}
	
	def executeUpdateDataTask(ProcessAPI processAPI, long caseId, String taskName, Object contract, long userId) {
		def task = findTaskInstance(caseId, taskName, processAPI)
		processAPI.assignAndExecuteUserTask(userId, task.id, contract)
	}
}
