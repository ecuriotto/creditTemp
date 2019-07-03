package com.company.dispute.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import groovy.json.JsonBuilder
import javassist.bytecode.stackmap.BasicBlock.Catch

import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Case implements RestApiController, CaseActivityHelper, BPMNamesConstants{

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        def contextPath = request.contextPath
        def processAPI = context.apiClient.getProcessAPI()
        def searchOptions = new SearchOptionsBuilder(0, 9999)
			.filter(ProcessInstanceSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
			.done()
        def result = processAPI.searchProcessInstances(searchOptions).getResult()
                .collect {
            [id: it.id, state: asLabel(it.state.toUpperCase(), "info"), viewAction: viewActionLink(it.id, processAPI, contextPath)]
        }
        processAPI.searchArchivedProcessInstances(searchOptions).getResult()
                .collect {
            result << [id: it.sourceObjectId, state: asLabel(it.state.toUpperCase(), "default"), viewAction: viewActionLink(it.sourceObjectId, processAPI, contextPath)]
        }

        return responseBuilder.with {
            withResponseStatus(HttpServletResponse.SC_OK)
            withResponse(new JsonBuilder(result).toString())
            build()
        }
    }

    def asLabel(state, style) {
        """<span class="label label-$style">$state</span>"""
    }

    def String viewActionLink(long caseId, ProcessAPI processAPI, contextPath) {
		try {
			def instance = processAPI.getProcessInstance(caseId)
			instance ? """<a class="btn btn-primary btn-sm" href="$contextPath/apps/cases/case?id=$caseId" target="_target">Open</a>""" : ''
		}catch(ProcessInstanceNotFoundException e) {
			""
		}
    }
}
