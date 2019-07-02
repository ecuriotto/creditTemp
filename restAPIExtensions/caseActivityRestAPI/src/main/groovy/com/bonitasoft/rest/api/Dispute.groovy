package com.bonitasoft.rest.api;

import java.time.format.DateTimeFormatter

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

class Dispute implements RestApiController, CaseActivityHelper, BPMNamesConstants{

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def caseId = request.getParameter "caseId"
		if (!caseId) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter caseId is missing"}""")
		}

		def processAPI = context.apiClient.getProcessAPI()
		def processInstanceContext = processAPI.getProcessInstanceExecutionContext(caseId.toLong())
		def dispute_ref = processInstanceContext['dispute_ref']
		def com.company.model.Dispute dispute
		if(dispute_ref) {
			def disputeDAO = context.apiClient.getDAO(DisputeDAO)
			dispute = disputeDAO.findByPersistenceId(dispute_ref.storageId)
		}
		if(!dispute) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"error" : "no dispute found for case $caseId"}""")
		}

		return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder([
			creationDate:dispute.creationDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			lastUpdateDate: dispute.lastUpdateDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			amount:dispute.amount,
			txDate:dispute.txDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
			merchandIdNumber:dispute.merchantIdNumber,
			currency:dispute.currency,
			status:dispute.status
		]).toString())
	}

	def buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
		return responseBuilder.with {
			withResponseStatus(httpStatus)
			withResponse(body)
			build()
		}
	}
}
