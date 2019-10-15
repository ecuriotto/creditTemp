package com.company.scenario.util

import static com.company.scenario.util.Condition.loop

import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder

import com.bonitasoft.engine.api.APIAccessor

trait IdAccessor {
    
    def long withUser(APIAccessor apiAccessor, String username) {
        apiAccessor.getIdentityAPI().getUserByUserName(username).id
    }
    
    def long taskId(APIAccessor apiAccessor, long processInstanceId, String taskName) {
        long taskId = -1
        loop {
            def searchResult = apiAccessor.getProcessAPI().searchHumanTaskInstances(new SearchOptionsBuilder(0, 1)
                .filter(HumanTaskInstanceSearchDescriptor.NAME, taskName)
                .filter(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceId)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE)
                .done())
             taskId = searchResult.count == 1 ? searchResult.getResult()[0].id : -1
        } until { taskId >= 0 }
        return taskId
    }
    
}
