package com.company.dispute.api;

import java.time.format.DateTimeFormatter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.DisputeDAO

import groovy.json.JsonBuilder

class DisputeInfo implements RestApiController, CaseActivityHelper, BPMNamesConstants{

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def caseId = request.getParameter "caseId"
		if (!caseId) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter caseId is missing"}""")
		}

		def processAPI = context.apiClient.getProcessAPI()
		def searcBusinessData = newSearchBusinessData(processAPI)
		def com.company.model.Dispute dispute = searcBusinessData.search(caseId.toLong(), 'dispute_ref', context.apiClient.getDAO(DisputeDAO))
		if(!dispute) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"error" : "no dispute found for case $caseId"}""")
		}

		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder([
			creationDate:dispute.creationDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			lastUpdateDate: dispute.lastUpdateDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			amount:dispute.amount,
			txDate:dispute.txDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			merchantIdNumber:dispute.merchantIdNumber,
			currency:dispute.currency,
			status:dispute.status
		]).toString())
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
