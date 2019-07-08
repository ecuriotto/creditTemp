/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api;


import org.bonitasoft.engine.bpm.data.DataNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException
import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance
import org.bonitasoft.engine.search.SearchOptionsBuilder

import com.bonitasoft.engine.api.ProcessAPI

trait CaseActivityHelper {

    def canExecute(String state) {
        return state != BPMNamesConstants.NOT_AVAILABLE_STATE &&
                state != ActivityStates.COMPLETED_STATE &&
                state != ActivityStates.FAILED_STATE &&
                state != ActivityStates.ABORTED_STATE
    }

    def valueOfState(String state) {
        switch (state) {
            case BPMNamesConstants.REQUIRED_STATE: return 1
            case BPMNamesConstants.OPTIONAL_STATE: return 2
            case BPMNamesConstants.DISCRETIONARY_STATE: return 3
            case BPMNamesConstants.NOT_AVAILABLE_STATE: return 4
            case ActivityStates.COMPLETED_STATE: return 5
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

    def boolean isAnArchivedLoopInstance(ArchivedHumanTaskInstance instance, ProcessAPI processAPI) {
        try {
            def parent = processAPI.getActivityInstance(instance.parentActivityInstanceId)
            return parent instanceof LoopActivityInstance
        }catch(ActivityInstanceNotFoundException e) {
            return false
        }
    }

    def getACMStateValue(HumanTaskInstance task, ProcessAPI processAPI) {
        if(task instanceof UserTaskInstance) {
            try {
                def instance = processAPI.getActivityTransientDataInstance(BPMNamesConstants.ACTIVITY_STATE_DATA_NAME, task.id)
                return instance ? instance.value : null
            }catch(DataNotFoundException e) {
                //no $activityState data defined on HumanTask
                return null
            }
        }else {
            return BPMNamesConstants.OPTIONAL_STATE
        }
    }
}
