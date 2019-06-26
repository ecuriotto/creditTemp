package com.bonitasoft.rest.api

import java.time.LocalDateTime

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.rest.api.Case
import com.bonitasoft.web.extension.rest.RestAPIContext

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
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import spock.lang.Specification

class CaseHistoryTest extends Specification {

    ProcessAPI processAPI = Mock()
	IdentityAPI identityAPI = Mock()
    APIClient apiClient = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()
    SearchResult<ProcessInstance> result = Mock()
    SearchResult<HumanTaskInstance> taskResult = Mock()
    DataInstance dataInstance = Mock()
	SearchResult EMPTY_RESULT =  Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
		apiClient.getIdentityAPI() >> identityAPI
    }

    def "should return a bad request code when no caseId parameter is found"() {
        given:
        def caseHistory = new CaseHistory()

		
        when:
        RestApiResponse restApiResponse = caseHistory.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
    }
	
	def "should query archived tasks with proper search options"() {
		given:
		def caseHistory = new CaseHistory()
		def SearchResult archivedTaskResult = Mock()
		
		when:
		request.getParameter('caseId') >> 1L
		RestApiResponse restApiResponse = caseHistory.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.searchArchivedHumanTasks({ SearchOptions searchOptions ->
			
			assert searchOptions.filters[0].field == ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID
			assert searchOptions.filters[0].value == 1L
			
			assert searchOptions.filters[1].field == ArchivedActivityInstanceSearchDescriptor.NAME
			assert searchOptions.filters[1].value == CaseHistory.ACTIVITY_CONTAINER
			
			assert searchOptions.filters[1].field == ArchivedActivityInstanceSearchDescriptor.NAME
			assert searchOptions.filters[1].value == CaseHistory.ACTIVITY_CONTAINER
			assert searchOptions.filters[1].operation == SearchFilterOperation.DIFFERENT
			
			assert searchOptions.sorts[0].field == ArchivedActivityInstanceSearchDescriptor.REACHED_STATE_DATE
			assert searchOptions.sorts[0].order == Order.DESC
			
			searchOptions
				
		}) >> archivedTaskResult
	}
	
	def "should return a formatted list of archived activities"() {
		given:
		def caseHistory = new CaseHistory()
		def SearchResult archivedTaskResult = Mock()
		def t1 = Stub(ArchivedHumanTaskInstance){
			it.displayName >> 'Hello'
			it.reachedStateDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45")
			it.executedBy >> 4L
		}
		def t2 = Stub(ArchivedHumanTaskInstance){
			it.displayName >> 'World'
			it.displayDescription >> 'a description'
			it.reachedStateDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-06 4:23:45")
			it.executedBy >> 5L
		}
		archivedTaskResult.result >> [t1,t2]
		processAPI.searchArchivedHumanTasks(_) >> archivedTaskResult
		def walter = Stub(User){
			it.id >> 4L
			it.firstName >> 'Walter'
			it.lastName >> 'Bates'
			it.username >> 'walter.bates'
		}
		def helen = Stub(User){
			it.id >> 5L
			it.firstName >> 'Helen'
			it.lastName >> 'Kelly'
			it.username >> 'helen.kelly'
		}
		identityAPI.getUser(4L) >> walter
		identityAPI.getUser(5L) >> helen
		
		
		when:
		request.getParameter('caseId') >> 1L
		RestApiResponse restApiResponse = caseHistory.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		def history = new JsonSlurper().parseText(restApiResponse.response)
		assert history.size() == 2
		
		assert history[0].displayName == 'Hello'
		assert history[0].displayDescription == ''
		assert history[0].reached_state_date == new JsonSlurper().parseText(new JsonBuilder(Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45")).toString())
		assert history[0].executedBy.id == 4L
		
		assert history[1].displayName == 'World'
		assert history[1].displayDescription == 'a description'
		assert history[1].reached_state_date == new JsonSlurper().parseText(new JsonBuilder(Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-06 4:23:45")).toString())
		assert history[1].executedBy.id == 5L
	}
	
}