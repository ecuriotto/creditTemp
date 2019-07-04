package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import spock.lang.Specification

class UserTaskTest extends Specification {

    ProcessAPI processAPI = Mock()
    APIClient apiClient = Mock()
	APISession session = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
		context.apiSession >> session
    }

    def "should return a bad request code when no taskId parameter is found"() {
        given:
        def userTask = new UserTask()
		def reader = new BufferedReader(new StringReader("""{ "content" : "" }"""))
		request.getReader() >> reader
		
        when:
        def restApiResponse = userTask.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert restApiResponse.response == 'No taskId in payload'
    }
	
	def "should return a bad request code when no processInstanceId parameter is found"() {
		given:
		def userTask = new UserTask()
		def reader = new BufferedReader(new StringReader("""{ "taskId" : "1" }"""))
		request.getReader() >> reader
		
        when:
        def restApiResponse = userTask.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert restApiResponse.response == 'No processInstanceId in payload'
    }
	
	def "should execute task with the given taskId"() {
		given:
		def userTask = new UserTask()
		def reader = new BufferedReader(new StringReader("""{ "processInstanceId" : "42", "taskId" : "1", "content" : "" }"""))
		request.getReader() >> reader
		session.userId >> 5L
		
        when:
        def restApiResponse = userTask.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		1 * processAPI.assignAndExecuteUserTask(5L, 1L, [processInstanceId: "42", taskId: "1", content : ""])
		0 * processAPI.addProcessComment(42L, _)
		assert restApiResponse.httpStatus == 201
    }
	
	def "should add a process comment when content is not empty"() {
		given:
		def userTask = new UserTask()
		def reader = new BufferedReader(new StringReader("""{ "processInstanceId" : "42", "taskId" : "1", "content" : "some notes" }"""))
		request.getReader() >> reader
		session.userId >> 5L
		
        when:
        def restApiResponse = userTask.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		1 * processAPI.assignAndExecuteUserTask(5L, 1L, [processInstanceId: "42", taskId: "1", content : "some notes"])
		1 * processAPI.addProcessComment(42L, "some notes")
		assert restApiResponse.httpStatus == 201
    }

}