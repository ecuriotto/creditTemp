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
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
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
import org.omg.CORBA.DataInputStream

import spock.lang.Specification

class CaseActivityHelperTest extends Specification implements CaseActivityHelper {

	ProcessAPI processAPI = Mock()
	
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
	
	def "should return state for a given activityInstance"(){
		given:
		def task = Stub(ActivityInstance){
			it.id >> 1L
		}
		
		when:
		def state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'N/A'}
		assert state == [name:'N/A',id:4]
		
		when:
	    state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'Required'}
		assert state == [name:'Required',id:1]
		
		when:
		state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'Optional'}
		assert state == [name:'Optional',id:2]
		
		when:
		state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'Discretionary'}
		assert state == [name:'Discretionary',id:3]
		
		when:
		state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'completed'}
		assert state == [name:'completed',id:5]
		
		when:
		task.getState() >> ActivityStates.ABORTED_STATE
		state = getState(task, processAPI)
		
		then:
		processAPI.getActivityTransientDataInstance('$activityState', task.id) >> Stub(DataInstance)  { it.value >> 'completed'}
		assert state == [name:ActivityStates.ABORTED_STATE, id:6]
		
		
		when:
		task = Stub(ManualTaskInstance){
			it.id >> 1L
		}
		state = getState(task, processAPI)
		
		then:
		assert state == [name:'Optional',id:2]
		
	}
	

}