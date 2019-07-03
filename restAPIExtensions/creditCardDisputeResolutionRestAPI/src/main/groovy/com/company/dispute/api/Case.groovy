package com.company.dispute.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.DisputeDAO

import groovy.json.JsonBuilder
import javassist.bytecode.stackmap.BasicBlock.Catch

import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Case implements RestApiController, CaseActivityHelper, BPMNamesConstants{

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def contextPath = request.contextPath
		def processAPI = context.apiClient.getProcessAPI()
		
		
		def searchData = newSearchBusinessData(processAPI)
		def searchOptions = new SearchOptionsBuilder(0, 9999)
				.filter(ProcessInstanceSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
				.done()
		def result = processAPI.searchProcessInstances(searchOptions).getResult()
				.collect {
					[
						id: it.id,
						status: searchData.search(it.id,'dispute_ref',context.getApiClient().getDAO(DisputeDAO))?.status,
						url: viewActionLink(it.id, processAPI, contextPath),
						target:'_self'
					]
				}
		processAPI.searchArchivedProcessInstances(searchOptions).getResult()
				.collect {
					result << [
						id: it.id,
						status: searchData.search(it.id,'dispute_ref',context.getApiClient().getDAO(DisputeDAO))?.status,
						url: viewActionLink(it.id, processAPI, contextPath),
						target:'_self'
					]
				}

		return responseBuilder.with {
			withResponseStatus(HttpServletResponse.SC_OK)
			withResponse(new JsonBuilder(result).toString())
			build()
		}
	}
	def SearchBusinessData newSearchBusinessData(ProcessAPI processAPI) {
		new SearchBusinessData(processAPI)
	}

	def String viewActionLink(long caseId, ProcessAPI processAPI, contextPath) {
		try {
			def instance = processAPI.getProcessInstance(caseId)
			instance ? "../../case/content/?id=$caseId" : ''
		}catch(ProcessInstanceNotFoundException e) {
			def instance = processAPI.getArchivedProcessInstance(caseId)
			def pDef = processAPI.getProcessDefinition(instance.processDefinitionId)
			"$contextPath/portal/resource/processInstance/$pDef.name/$pDef.version/content/?id=$instance.sourceObjectId"
		}
	}
}
