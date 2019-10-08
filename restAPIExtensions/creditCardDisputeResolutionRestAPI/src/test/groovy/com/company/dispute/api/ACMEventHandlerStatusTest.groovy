package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonSlurper
import spock.lang.Specification

class ACMEventHandlerStatusTest extends Specification {

    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

	def setup() {
		System.clearProperty(ACMEventHandlerStatus.ACM_EVENT_HANDLER_PROPERTY)
	}
	
    def "should return true when ACM event handler sytem property is true"() {
        given:
        def acmEventHandlerStatus = new ACMEventHandlerStatus()
		System.setProperty(ACMEventHandlerStatus.ACM_EVENT_HANDLER_PROPERTY, "true")

        when:
        def restApiResponse = acmEventHandlerStatus.doHandle(request, new RestApiResponseBuilder() , context)

        then:
        assert restApiResponse.httpStatus == 200
        assert new JsonSlurper().parseText(restApiResponse.response).eventHandlerEnabled == true
    }

	def "should return false when ACM event handler sytem property is not set"() {
		given:
		def acmEventHandlerStatus = new ACMEventHandlerStatus()

		when:
		def restApiResponse = acmEventHandlerStatus.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		assert new JsonSlurper().parseText(restApiResponse.response).eventHandlerEnabled == false
	}
	
	def "should return false when ACM event handler sytem property is set to false"() {
		given:
		def acmEventHandlerStatus = new ACMEventHandlerStatus()
		System.setProperty(ACMEventHandlerStatus.ACM_EVENT_HANDLER_PROPERTY, "false")

		when:
		def restApiResponse = acmEventHandlerStatus.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		assert new JsonSlurper().parseText(restApiResponse.response).eventHandlerEnabled == false
	}
   
}