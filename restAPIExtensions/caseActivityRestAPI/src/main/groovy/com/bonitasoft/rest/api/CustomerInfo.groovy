package com.bonitasoft.rest.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.model.CustomerDAO

import groovy.json.JsonBuilder
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomerInfo implements RestApiController, CaseActivityHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerInfo.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        def contextPath = request.contextPath
		
		def caseId = request.getParameter "caseId"
		if (!caseId) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter caseId is missing"}""")
		}
		
		def processAPI = context.apiClient.getProcessAPI()
		def processInstanceContext = processAPI.getProcessInstanceExecutionContext(caseId.toLong())
		def customer_ref = processInstanceContext['customer_ref']
		def customerDAO = context.apiClient.getDAO(CustomerDAO)
		def customer = customerDAO.findByPersistenceId(customer_ref.storageId)
		if(!customer) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"error" : "no customer found with id $customer_ref.storageId"}""")
		}

        return responseBuilder.with {
            withResponseStatus(HttpServletResponse.SC_OK)
            withResponse(new JsonBuilder( 
				[
					firstName:customer.firstName,
					lastName:customer.lastName,
					phoneNumber:customer.phoneNumber,
					accountId:customer.account.id
				]
			).toString())
            build()
        }
    }
	
	def buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
		return responseBuilder.with {
			withResponseStatus(httpStatus)
			withResponse(body)
			build()
		}
	}

  
}
