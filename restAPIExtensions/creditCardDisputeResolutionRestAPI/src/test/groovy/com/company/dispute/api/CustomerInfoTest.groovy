package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.model.Account
import com.company.model.Customer
import com.company.model.CustomerDAO

import groovy.json.JsonSlurper
import spock.lang.Specification

class CustomerInfoTest extends Specification {

    ProcessAPI processAPI = Mock()
    APIClient apiClient = Mock()
	CustomerDAO customerDAO = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
		apiClient.getDAO(CustomerDAO) >> customerDAO
    }

    def "should return a bad request code when no caseId parameter is found"() {
        given:
        def customerInfo = new  CustomerInfo()

        when:
        def restApiResponse = customerInfo.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
    }
	
	def "should return a not found error when no customer found for this case"() {
		given:
		def customerInfo = new  CustomerInfo()
		def SimpleBusinessDataReferenceImpl customerDataRef = new SimpleBusinessDataReferenceImpl('customer_ref',Customer.getName(),3L)
		request.getParameter('caseId') >> 1L
		
		when:'no customer_ref found in the process context'
		processAPI.getProcessInstanceExecutionContext(1L) >> [:]
		def restApiResponse = customerInfo.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		0*customerDAO.findByPersistenceId(_)
		assert restApiResponse.httpStatus == 404
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'no customer found for case 1'
		
		when:'no customer found for the provided customer id'
		processAPI.getProcessInstanceExecutionContext(1L) >> [customer_ref:customerDataRef]
	    restApiResponse = customerInfo.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1*customerDAO.findByPersistenceId(3L) >> null
		assert restApiResponse.httpStatus == 404
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'no customer found for case 1'
	}
	
	def "should retrieve the customer info for the given caseId"() {
		given:
		def customerInfo = new CustomerInfo()
		def SimpleBusinessDataReferenceImpl customerDataRef = new SimpleBusinessDataReferenceImpl('customer_ref',Customer.getName(),3L)
		def  customer = Stub(Customer){
			it.persistenceId >> 3L
			it.firstName >> 'John'
			it.lastName >> 'Doe'
			it.email >> 'john.doe@acme.com'
			it.phoneNumber >> '000-434-344'
			it.account >> Stub(Account) { it.id >> '123434'}
		}
		
		when:
		def restApiResponse = customerInfo.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		request.getParameter('caseId') >> 1L
		1 * processAPI.getProcessInstanceExecutionContext(1L) >> [customer_ref:customerDataRef]
		1 * customerDAO.findByPersistenceId(3L) >> customer
		def result = new JsonSlurper().parseText(restApiResponse.response)
		assert result.firstName == 'John'
		assert result.lastName == 'Doe'
		assert result.email == 'john.doe@acme.com'
		assert result.phoneNumber == '000-434-344'
		assert result.accountId == '123434'
	}

}