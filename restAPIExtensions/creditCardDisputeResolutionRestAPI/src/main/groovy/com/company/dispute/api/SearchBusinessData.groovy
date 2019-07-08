/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api

import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException

import com.bonitasoft.engine.api.ProcessAPI

class SearchBusinessData {

    private ProcessAPI processAPI;

    SearchBusinessData(ProcessAPI processAPI){
        this.processAPI = processAPI;
    }

    def search(long caseId, String dataRef, BusinessObjectDAO dao) {
        def processInstanceContext
        try {
            processInstanceContext = processAPI.getProcessInstanceExecutionContext(caseId)
        }catch(ProcessInstanceNotFoundException e) {
            processInstanceContext = processAPI.getArchivedProcessInstanceExecutionContext(caseId)
        }
        if(processInstanceContext) {
            def ref = processInstanceContext[dataRef]
            if(ref) {
                return dao.findByPersistenceId(ref.storageId)
            }
        }
        return null
    }
}
