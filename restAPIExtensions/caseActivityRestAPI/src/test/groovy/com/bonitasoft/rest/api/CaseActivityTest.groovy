package com.bonitasoft.rest.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchFilterOperation
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Specification

class CaseActivityTest extends Specification {

    ProcessAPI processAPI = Mock()
	IdentityAPI identityAPI = Mock()
    APIClient apiClient = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()
	APISession session = Stub(){ it.userId >> 5L}
	SearchResult emptyResult = Stub()

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
		1 * caseActivity.findTaskInstance(1L, BPMNamesConstants.ACTIVITY_CONTAINER, processAPI) >> Stub(HumanTaskInstance)
		1 * processAPI.searchHumanTaskInstances(_) >> emptyResult
		1 * processAPI.searchArchivedHumanTasks(_) >> emptyResult
		1 * processAPI.getPendingHumanTaskInstances(5L,0,Integer.MAX_VALUE, ActivityInstanceCriterion.EXPECTED_END_DATE_ASC) >> []
	}
	
}