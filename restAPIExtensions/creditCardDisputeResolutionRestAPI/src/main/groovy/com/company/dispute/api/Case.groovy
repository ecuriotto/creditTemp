/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api

import java.time.format.DateTimeFormatter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.company.creditcard.dispute.model.CustomerDAO
import com.company.creditcard.dispute.model.DisputeDAO

import groovy.json.JsonBuilder

class Case implements RestApiController, CaseActivityHelper, BPMNamesConstants{

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        def contextPath = request.contextPath
        def processAPI = context.apiClient.getProcessAPI()
        def searchData = newSearchBusinessData(processAPI)

        def p = request.getParameter "p"
        def c = request.getParameter "c"

        if(!p) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, "Parameter `p` is mandatory")
        }
        if(!c) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, "Parameter `c` is mandatory")
        }

        def searchIndex = request.getParameter("s")

        def pInt = p as int
        def cInt = c as int
        def searchResult = searchInstances(processAPI, pInt, cInt, searchIndex)
        def result = searchResult
                .result
                .collect {
                    toCase([
                        processAPI:processAPI,
                        apiClient: context.apiClient,
                        searchData:searchData,
                        isOpen:true,
                        contextPath:contextPath,
                        caseId:it.id
                    ])
                }

        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString(), pInt, cInt, searchResult.count)
    }

    SearchResult searchInstances(ProcessAPI processAPI, p, c, searchIndex) {
        return processAPI.searchProcessInstances(new SearchOptionsBuilder(p * c, c).with {
            filter(ProcessInstanceSearchDescriptor.NAME, DISPUTE_PROCESS_NAME)
            if (searchIndex) {
                and()
                leftParenthesis()
                filter(ProcessInstanceSearchDescriptor.STRING_INDEX_1, searchIndex)
                or()
                filter(ProcessInstanceSearchDescriptor.STRING_INDEX_2, searchIndex)
                or()
                filter(ProcessInstanceSearchDescriptor.STRING_INDEX_3, searchIndex)
                rightParenthesis()
            }
            done()
        })
    }

    def toCase(caseInput) {
        def caseId = caseInput.caseId
        def dispute = caseInput.searchData.search(caseId,'dispute_ref', caseInput.apiClient.getDAO(DisputeDAO))
        def customer = caseInput.searchData.search(caseId,'customer_ref', caseInput.apiClient.getDAO(CustomerDAO))
        return [
            id: caseId,
            open: caseInput.isOpen,
            dispute:[
                status: dispute?.status,
                merchantIdNumber: dispute?.merchantIdNumber,
                lastUpdateDate:dispute?.lastUpdateDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            ],
            caseUrl:[
                href: caseUrl(caseId, caseInput.processAPI, caseInput.contextPath),
                target: caseInput.isOpen ? '_target' : '_self',
            ],
            customer: "$customer.firstName $customer.lastName",
        ]
    }

    def SearchBusinessData newSearchBusinessData(ProcessAPI processAPI) {
        new SearchBusinessData(processAPI)
    }

    def String caseUrl(long caseId, ProcessAPI processAPI, contextPath) {
        try {
            def instance = processAPI.getProcessInstance(caseId)
            instance ? "$contextPath/apps/cases/case?id=$caseId" : ''
        }catch(ProcessInstanceNotFoundException e) {
            def instance = processAPI.getArchivedProcessInstance(caseId)
            def pDef = processAPI.getProcessDefinition(instance.processDefinitionId)
            "$contextPath/portal/resource/processInstance/$pDef.name/$pDef.version/content/?id=$instance.sourceObjectId"
        }
    }

    def RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder
                .withResponseStatus(httpStatus)
                .withResponse(body)
                .build()
    }

    def RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body, int p, int c, long totalSize) {
        return responseBuilder
                .withContentRange(p, c, totalSize)
                .withResponseStatus(httpStatus)
                .withResponse(body)
                .build()
    }
}
