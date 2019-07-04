package com.company.dispute.api

import java.time.format.DateTimeFormatter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.CustomerDAO
import com.company.model.DisputeDAO

import groovy.json.JsonBuilder

class Case implements RestApiController, CaseActivityHelper, BPMNamesConstants{

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def contextPath = request.contextPath
		def processAPI = context.apiClient.getProcessAPI()
		def searchData = newSearchBusinessData(processAPI)

		def result = processAPI.searchProcessInstances(new SearchOptionsBuilder(0, Integer.MAX_VALUE).with {
			filter(ProcessInstanceSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
			done()
		})
		.result
		.collect {  toCase([
			processAPI:processAPI,
			apiClient: context.apiClient,
			searchData:searchData,
			isOpen:true,
			contextPath:contextPath,
			caseId:it.id
			])}

		result.addAll(processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, Integer.MAX_VALUE).with {
			filter(ArchivedProcessInstancesSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
			done()
		}).result
		.collect { toCase([
			processAPI:processAPI,
			apiClient: context.apiClient,
			searchData:searchData,
			isOpen:false,
			contextPath:contextPath,
			caseId:it.id
			]) })

		return responseBuilder.with {
			withResponseStatus(HttpServletResponse.SC_OK)
			withResponse(new JsonBuilder([
				cases:result,
				canCreateDispute:userCanStartProcess(processAPI, context.apiSession.userId, DISPUTE_PROCESS_NAME)
			]).toString())
			build()
		}
	}
	
	def boolean userCanStartProcess(ProcessAPI processAPI, long userId, String processName) {
		return processAPI.searchUsersWhoCanStartProcessDefinition(processAPI.getLatestProcessDefinitionId(processName), new SearchOptionsBuilder(0,Integer.MAX_VALUE).with {
			filter(UserSearchDescriptor.ID, userId)
			done()
		}).count > 0
	}

	def toCase(caseInput) {
		def caseId = caseInput.caseId
		def dispute = caseInput.searchData.search(caseId,'dispute_ref', caseInput.apiClient.getDAO(DisputeDAO))
		def customer = caseInput.searchData.search(caseId,'customer_ref', caseInput.apiClient.getDAO(CustomerDAO))
		return [
			id: caseId,
			open: caseInput.isOpen,
			dispute:[
				status: dispute?.status,
				merchantIdNumber: dispute?.merchantIdNumber,
				lastUpdateDate:dispute?.lastUpdateDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			],
			caseUrl:[
				href: caseUrl(caseId, caseInput.processAPI, caseInput.contextPath),
				target: caseInput.isOpen ? '_target' : '_self',
			],
			customer: "$customer.firstName $customer.lastName",
		]
	}

	def SearchBusinessData newSearchBusinessData(ProcessAPI processAPI) {
		new SearchBusinessData(processAPI)
	}

	def String caseUrl(long caseId, ProcessAPI processAPI, contextPath) {
		try {
			def instance = processAPI.getProcessInstance(caseId)
			instance ? "$contextPath/apps/cases/case?id=$caseId" : ''
		}catch(ProcessInstanceNotFoundException e) {
			def instance = processAPI.getArchivedProcessInstance(caseId)
			def pDef = processAPI.getProcessDefinition(instance.processDefinitionId)
			"$contextPath/portal/resource/processInstance/$pDef.name/$pDef.version/content/?id=$instance.sourceObjectId"
		}
	}
}
