package com.bonitasoft.rest.api

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.rest.api.Case
import com.bonitasoft.web.extension.rest.RestAPIContext
import groovy.json.JsonSlurper
import org.bonitasoft.engine.bpm.data.DataInstance
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityInstanceImpl
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskInstanceImpl
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import spock.lang.Specification

class CaseTest extends Specification {

    ProcessAPI processAPI = Mock()
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
        processAPI.searchProcessInstances(_) >> result
        processAPI.searchArchivedProcessInstances(_) >> EMPTY_RESULT
        processAPI.getProcessDefinition(_) >>  Mock(ProcessDefinition)
        processAPI.searchHumanTaskInstances(_) >> taskResult
        processAPI.getActivityTransientDataInstance('$activityState', _) >> dataInstance
        result.getResult() >> [[id: 45L, processDefinitionId: 56L, sourceObjectId: 78L, state: 'state']]

		def HumanTaskInstance taskInstance = Mock()
		taskInstance.name >> 'activity name'
		taskInstance.id >> 45L
		
        taskResult.getResult() >> [taskInstance]
    }

    def "should response contains a proper case url"() {
        given:
        def aCase = new Case()
		request.contextPath >> "myAppContext"

        when:
        def restApiResponse = aCase.doHandle(request, new RestApiResponseBuilder(), context)

        then:
        JsonSlurper slurper = new JsonSlurper()
        def response = slurper.parseText(restApiResponse.response)
		assert response.size() > 0
        response.each { c ->
            assert c.viewAction.contains("href=\"myAppContext/apps/cases/case?id=${c.id}\"")
        }
    }
}