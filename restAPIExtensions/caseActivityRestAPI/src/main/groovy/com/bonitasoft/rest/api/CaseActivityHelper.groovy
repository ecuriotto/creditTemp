package com.bonitasoft.rest.api;


import org.bonitasoft.engine.bpm.data.DataNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.search.SearchOptionsBuilder

import com.bonitasoft.engine.api.ProcessAPI

trait CaseActivityHelper {
	
	def canExecute(String state) {
		return state != "N/A" &&
				state != ActivityStates.COMPLETED_STATE &&
				state != ActivityStates.FAILED_STATE &&
				state != ActivityStates.ABORTED_STATE
	}

	def getState(ActivityInstance activityInstance, ProcessAPI processAPI) {
		try {
			def defaultState = activityInstance.getState()
			if (defaultState == ActivityStates.ABORTED_STATE || defaultState == ActivityStates.FAILED_STATE) {
				return [name: defaultState, id: idOfState(defaultState)]
			}
			if(activityInstance instanceof ManualTaskInstance) {
				return [name: 'Optional', id: idOfState('Optional')]
			}
			def instance = processAPI.getActivityTransientDataInstance('$activityState', activityInstance.id)
			return [name: instance.value, id: idOfState(instance.value)]
		} catch (DataNotFoundException e) {
			println e.getMessage()
			return [name: "Optional", id: idOfState("Optional")]
		}
	}

	def idOfState(String state) {
		switch (state) {
			case "Required": return 1
			case "Optional": return 2
			case "Discretionary": return 3
			case "N/A": return 4
			case "completed": return 5
			default: return 6
		}
	}
	
	def HumanTaskInstance findTaskInstance(long caseId, String name, ProcessAPI processAPI) {
		def result = processAPI.searchHumanTaskInstances(new SearchOptionsBuilder(0, 1).with {
			filter(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, caseId)
			filter(HumanTaskInstanceSearchDescriptor.NAME, name)
			done()
		}).getResult()
		return result.isEmpty() ? null : result[0]
	}

}
