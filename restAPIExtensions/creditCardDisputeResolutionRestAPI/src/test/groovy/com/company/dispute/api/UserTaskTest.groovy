package com.company.dispute.api

import java.time.LocalDateTime

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator.ManualTaskField
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.dispute.api.Case
import com.company.dispute.api.UserTask

import groovy.io.LineColumnReader
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.bonitasoft.engine.bpm.data.DataInstance
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityInstanceImpl
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskInstanceImpl
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchFilterOperation
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.search.impl.SearchFilter
import org.bonitasoft.engine.search.impl.SearchOptionsImpl
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
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