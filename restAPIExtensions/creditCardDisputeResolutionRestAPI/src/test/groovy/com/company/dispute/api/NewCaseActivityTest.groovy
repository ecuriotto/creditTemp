package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator.ManualTaskField
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonSlurper
import spock.lang.Specification

class NewCaseActivityTest extends Specification {

    ProcessAPI processAPI = Mock()
	IdentityAPI identityAPI = Mock()
    APIClient apiClient = Mock()
	APISession session = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
		apiClient.getIdentityAPI() >> identityAPI
		context.apiSession >> session
    }

    def "should return a bad request code when no name parameter is found"() {
        given:
        def newCaseActivity = new NewCaseActivity()
		def reader = new BufferedReader(new StringReader("""{ "caseId" : "1" }"""))
		request.getReader() >> reader
		
        when:
        def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter name is missing'
    }
	
	def "should return a bad request code when no caseId parameter is found"() {
		given:
		def newCaseActivity = new NewCaseActivity()
		def reader = new BufferedReader(new StringReader("""{ "name" : "Hello" }"""))
		request.getReader() >> reader
		
		when:
		def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
	}
	
	def "should return a not found error code when Dynamic Activity Container instance is not found"() {
		given:
		def newCaseActivity = Spy(NewCaseActivity)
		def reader = new BufferedReader(new StringReader("""{ "name" : "Hello", "caseId" : "1" }"""))
		request.getReader() >> reader
		
		when:
		def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * newCaseActivity.findTaskInstance(1L, 'Dynamic Activity Container', processAPI) >> null
		assert restApiResponse.httpStatus == 404
		assert restApiResponse.response == 'No Dynamic Activity Container found'
	}
	
	def "should return a not found error code when Create Activity instance is not found"() {
		given:
		def newCaseActivity = Spy(NewCaseActivity)
		def reader = new BufferedReader(new StringReader("""{ "name" : "Hello", "caseId" : "1" }"""))
		request.getReader() >> reader
		newCaseActivity.findTaskInstance(1L, 'Dynamic Activity Container', processAPI) >> Stub(HumanTaskInstance)
		
		when:
		def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * newCaseActivity.findTaskInstance(1L, 'Create Activity', processAPI) >> null
		assert restApiResponse.httpStatus == 404
		assert restApiResponse.response == 'No Create Activity found'
	}
	
	def "should add a manual task to the Dynamic Activity Container instance"() {
		given:
		def newCaseActivity = Spy(NewCaseActivity)
		def reader = new BufferedReader(new StringReader("""{ "name" : "Hello", "caseId" : "1" }"""))
		request.getReader() >> reader
		newCaseActivity.findTaskInstance(1L, 'Dynamic Activity Container', processAPI) >> Stub(HumanTaskInstance){
			it.id >> 23L
		}
		newCaseActivity.findTaskInstance(1L, 'Create Activity', processAPI) >>  Stub(HumanTaskInstance)
		session.userId >> 5L
		
		when:
		def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.addManualUserTask({
			assert it.fields[ManualTaskField.TASK_NAME] == 'Hello'
			assert it.fields[ManualTaskField.PARENT_TASK_ID] == 23L
			assert it.fields[ManualTaskField.ASSIGN_TO] == 5L
			it
		})
		assert restApiResponse.httpStatus == 201
	}
	
	def "should execute a Create Activity instance"() {
		given:
		def newCaseActivity = Spy(NewCaseActivity)
		def reader = new BufferedReader(new StringReader("""{ "name" : "Hello", "caseId" : "1" }"""))
		request.getReader() >> reader
		newCaseActivity.findTaskInstance(1L, 'Dynamic Activity Container', processAPI) >> Stub(HumanTaskInstance)
		newCaseActivity.findTaskInstance(1L, 'Create Activity', processAPI) >>  Stub(HumanTaskInstance){
			it.id >> 23L
		}
		session.userId >> 5L
		
		when:
		def restApiResponse = newCaseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.assignAndExecuteUserTask( 5L, 23L, [name:'Hello'])
	}
	

}