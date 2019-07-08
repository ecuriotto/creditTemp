/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api

import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor

class ArchivedCase extends Case {

    @Override
    SearchResult searchInstances(ProcessAPI processAPI, p, c, searchIndex) {
        return processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(p * c, c).with {
            filter(ArchivedProcessInstancesSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
            if (searchIndex) {
                and()
                leftParenthesis()
                filter(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_1, searchIndex)
                or()
                filter(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_2, searchIndex)
                or()
                filter(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_3, searchIndex)
                rightParenthesis()
            }
            done()
        })
    }
}
