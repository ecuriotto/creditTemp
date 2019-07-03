package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchFilterOperation
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.dispute.api.BPMNamesConstants
import com.company.dispute.api.CaseActivity

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Specification


class CaseActivityTest extends Specification {

    ProcessAPI processAPI = Mock()
	IdentityAPI identityAPI = Mock()
    APIClient apiClient = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()
	APISession session = Stub(){ it.userId >> 5L}
	SearchResult emptyResult = new SearchResultImpl(0, [])

    def "setup"() {
        context.apiClient >> apiClient
		context.apiSession >> session
        apiClient.getProcessAPI() >> processAPI
		apiClient.getIdentityAPI() >> identityAPI
    }

    def "should return a bad request code when no caseId parameter is found"() {
        given:
        def caseActivity = new CaseActivity()

		
        when:
        def restApiResponse = caseActivity.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
    }
	
	def "should query pending human tasks for current user"() {
		given:
		def caseActivity = Spy(CaseActivity)
		def SearchResult taskResult = Mock()
		
		when:
		request.getParameter('caseId') >> 1L
		def restApiResponse = caseActivity.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.searchHumanTaskInstances(_) >> emptyResult
		1 * processAPI.searchArchivedHumanTasks(_) >> emptyResult
		1 * processAPI.getPendingHumanTaskInstances(5L,0,Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC) >> []
	}
	
	
	def "should create activity object from  a HumanTaskInstance and a state"() {
		given:
		def caseActivity = new CaseActivity()
		def UserTaskInstance task = Stub(){ UserTaskInstance task ->
			task.name >> 'MyTask'
			task.displayName >> 'My task'
			task.description >> 'A small desc'
			task.state >> ActivityStates.READY_STATE
			task.id >> 2L
		}
		def ProcessDefinition pDef = Stub(){
			it.name >> 'MyProcess'
			it.version >> '1.0'
		}
		
		when:
		def activity = caseActivity.toActivity(task, BPMNamesConstants.REQUIRED_STATE, pDef, '/myAppContext')

		then:
		with(activity){
			 name == task.displayName
			 bpmState == 'Ready'
			 url == '/myAppContext/portal/resource/taskInstance/MyProcess/1.0/MyTask/content/?id=2&displayConfirmation=false'
			 target == '_self'
			 acmState == BPMNamesConstants.REQUIRED_STATE
		}
	}
	
	def "should forge an url to execute manual tasks"() {
		given:
		def caseActivity = new CaseActivity()
		def ManualTaskInstance task = Stub(){ ManualTaskInstance task ->
			task.name >> 'MyTask'
			task.displayName >> 'My task'
			task.description >> 'A small desc'
			task.state >> ActivityStates.READY_STATE
			task.id >> 2L
		}
		def ProcessDefinition pDef = Stub(){
			it.name >> 'MyProcess'
			it.version >> '1.0'
		}
		
		when:
		def activity = caseActivity.toActivity(task, BPMNamesConstants.REQUIRED_STATE, pDef, '/myAppContext')

		then:
		with(activity){
			 url == '/myAppContext/apps/cases/do?id=2'
			 target == '_parent'
		}
	}
	
	def "should sort tasks be acmState values"() {
		given:
		def caseActivity = Spy(CaseActivity)
		def UserTaskInstance t1 = Stub(){ UserTaskInstance task ->
			task.name >> 'MyTask'
			task.displayName >> 'My task'
			task.description >> 'A small desc'
			task.state >> ActivityStates.READY_STATE
			task.id >> 2L
			task.parentProcessInstanceId >> 1L
		}
		caseActivity.getACMStateValue(t1, processAPI) >> BPMNamesConstants.DISCRETIONARY_STATE
		def UserTaskInstance t2 = Stub(){ UserTaskInstance task ->
			task.name >> 'MyTask2'
			task.displayName >> 'My task2'
			task.description >> 'A small desc'
			task.state >> ActivityStates.READY_STATE
			task.id >> 3L
			task.parentProcessInstanceId >> 1L
		}
		caseActivity.getACMStateValue(t2, processAPI) >> BPMNamesConstants.REQUIRED_STATE
		def ArchivedHumanTaskInstance t3 = Stub(){ ArchivedHumanTaskInstance task ->
			task.name >> 'MyTask2'
			task.displayName >> 'My task2'
			task.description >> 'A small desc'
			task.state >> ActivityStates.COMPLETED_STATE
			task.id >> 3L
		}
		processAPI.getPendingHumanTaskInstances(_, 0, Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC) >> [t1,t2]
		processAPI.searchHumanTaskInstances(_) >> new SearchResultImpl(2, [t1,t2])
		processAPI.searchArchivedHumanTasks(_) >>  new SearchResultImpl(1,[t3])
		def ProcessDefinition pDef = Stub(){
			it.name >> 'MyProcess'
			it.version >> '1.0'
		}
		processAPI.getProcessDefinition(_) >> pDef
		request.getParameter('caseId') >> 1L
		
		when:
		def result = caseActivity.doHandle(request, new RestApiResponseBuilder(), context)

		then:
		with(result){
			 httpStatus == 200
			 new JsonSlurper().parseText(response).activities.acmState == [BPMNamesConstants.REQUIRED_STATE,BPMNamesConstants.DISCRETIONARY_STATE,null]
		}
	}
	
	
}