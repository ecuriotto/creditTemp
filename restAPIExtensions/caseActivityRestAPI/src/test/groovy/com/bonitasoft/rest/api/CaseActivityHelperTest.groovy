package com.bonitasoft.rest.api

import java.time.LocalDateTime

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator.ManualTaskField
import com.bonitasoft.rest.api.Case
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.fasterxml.jackson.databind.cfg.ContextAttributes.Impl

import groovy.io.LineColumnReader
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.bonitasoft.engine.bpm.data.DataInstance
import org.bonitasoft.engine.bpm.data.DataNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance
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
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.omg.CORBA.DataInputStream

import spock.lang.Specification
import spock.lang.Unroll

class CaseActivityHelperTest extends Specification implements CaseActivityHelper {

	ProcessAPI processAPI = Mock()

	@Unroll
	def "should #state has a valid executable state"() {
		expect:
		canExecute(state) == isValid

		where:
		state           || isValid
		'N/A'           || false
		'completed'     || false
		'aborted'       || false
		'failed'        || false
		'Required'      || true
		'Optional'      || true
		'Discretionary' || true
	}

	@Unroll
	def "should #state return a proper state integer value"(){
		expect:
	    value == valueOfState(state)

		where:
		state           || value
		'Required'      || 1
		'Optional'      || 2
		'Discretionary' || 3
		'N/A'           || 4
		'completed'     || 5
		'aborted'       || 6
		'failed'        || 6
	}

	def "should check if an archived human task is a loop task instance"(){
		given:
		def archivedTask = Stub(ArchivedHumanTaskInstance){ it.parentActivityInstanceId >> 3L }

		when:
		def result = isAnArchivedLoopInstance(archivedTask, processAPI)

		then:
		processAPI.getActivityInstance(3L) >> Stub(LoopActivityInstance)
		result == true

		when:
		result = isAnArchivedLoopInstance(archivedTask, processAPI)

		then:
		processAPI.getActivityInstance(3L) >> Stub(ActivityInstance)
		result == false
	}
	
	def "should get the ACM state value from task data"(){
		given:
		def task = Stub(UserTaskInstance){ it.id >> 3L }

		when:
		def state = getACMStateValue(task, processAPI)

		then:
		processAPI.getActivityTransientDataInstance(BPMNamesConstants.ACTIVITY_STATE_DATA_NAME,task.id) >> Stub(DataInstance){it.value >> 'Required' }
		state == 'Required'
		
		when:
	    state = getACMStateValue(task, processAPI)

		then:
		processAPI.getActivityTransientDataInstance(BPMNamesConstants.ACTIVITY_STATE_DATA_NAME,task.id) >> { throw new DataNotFoundException() }
		state == null
	}
	
	def "should find a unique task instance by name in a case"(){
		given:
		def task = Stub(UserTaskInstance){ it.id >> 3L }

		when:
		def instance = findTaskInstance(1, 'MyTask', processAPI)

		then:
		processAPI.searchHumanTaskInstances({
			assert it.filters[0].field == HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID
			assert it.filters[0].value == 1
			
			assert it.filters[1].field == HumanTaskInstanceSearchDescriptor.NAME
			assert it.filters[1].value == 'MyTask'
			
			it
		}) >> new SearchResultImpl(1,[Stub(HumanTaskInstance)])
		assert instance != null
		
		when:
	    instance = findTaskInstance(1, 'MyTask', processAPI)

		then:
		processAPI.searchHumanTaskInstances(_) >> new SearchResultImpl(0,[])
		assert instance == null
		
	}
}