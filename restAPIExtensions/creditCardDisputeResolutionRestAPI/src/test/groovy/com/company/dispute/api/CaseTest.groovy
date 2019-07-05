package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance
import org.bonitasoft.engine.bpm.process.ProcessDefinition
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.model.Customer
import com.company.model.Dispute

import spock.lang.Specification


class CaseTest extends Specification {

    ProcessAPI processAPI = Mock()
    APIClient apiClient = Mock()
    APISession session = Stub(userId:5L)
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()
    SearchResult EMPTY_RESULT =  Mock()

    def "setup"() {
        context.apiClient >> apiClient
        context.apiSession >> session
        apiClient.getProcessAPI() >> processAPI
        processAPI.searchArchivedProcessInstances(_) >> EMPTY_RESULT
        processAPI.getProcessDefinition(_) >>  Mock(ProcessDefinition)
        processAPI.searchUsersWhoCanStartProcessDefinition(_, _) >> new SearchResultImpl(0,[])
    }

    def "should response contains a proper case url when case is opened"() {
        given:
        def aCase = new Case()
        processAPI.getProcessInstance(_) >> Stub(ProcessInstance)

        when:
        def url = aCase.caseUrl(1L, processAPI, 'myAppContext')

        then:
        url == 'myAppContext/apps/cases/case?id=1'
    }

    def "should response contains a proper case url when case is archived"() {
        given:
        def aCase = new Case()
        processAPI.getProcessInstance(1L) >> { throw new ProcessInstanceNotFoundException('not found') }

        when:
        def url = aCase.caseUrl(1L, processAPI, 'myAppContext')

        then:
        processAPI.getArchivedProcessInstance(1L) >> Stub(ArchivedProcessInstance){
            it.processDefinitionId >> 3L
            it.sourceObjectId >> 1L
        }
        processAPI.getProcessDefinition(3L) >> Stub(ProcessDefinition) {
            it.name >> 'MyProcess'
            it.version >> '1.0'
        }
        url == 'myAppContext/portal/resource/processInstance/MyProcess/1.0/content/?id=1'
    }


    def "should aggregate dispute info on a case"() {
        given:
        def aCase = new Case()
        def searchBusinessData = Mock(SearchBusinessData)
        aCase.newSearchBusinessData(processAPI) >> searchBusinessData
        def aDispute = Mock(Dispute)
        aDispute.status >> 'RESOLVED'

        def aCustomer = Stub(Customer)
        searchBusinessData.search(_, 'customer_ref', _) >> aCustomer

        when:
        def caseData = aCase.toCase([
            processAPI:processAPI,
            apiClient: context.apiClient,
            searchData:searchBusinessData,
            isOpen:false,
            contextPath:'myAppContext',
            caseId:1L
        ])

        then:
        1 * searchBusinessData.search(_, 'dispute_ref', _) >> aDispute
        with(caseData) {
            id == 1L
            open == false
            with(dispute) {
                status == 'RESOLVED'
            }
        }
    }

    def "should aggregate customer info on a case"() {
        given:
        def aCase = new Case()
        def searchBusinessData = Mock(SearchBusinessData)
        aCase.newSearchBusinessData(processAPI) >> searchBusinessData
        def aDispute = Mock(Dispute)
        searchBusinessData.search(_, 'dispute_ref', _) >> aDispute

        def aCustomer = Stub(Customer){
            it.firstName >> 'John'
            it.lastName >> 'Doe'
        }

        when:
        def caseData = aCase.toCase([
            processAPI:processAPI,
            apiClient: context.apiClient,
            searchData:searchBusinessData,
            isOpen:false,
            contextPath:'myAppContext',
            caseId:1L
        ])

        then:
        1 * searchBusinessData.search(_, 'customer_ref', _) >> aCustomer
        with(caseData) {
            id == 1L
            open == false
            customer == 'John Doe'
        }
    }

    def "should build caseUrl on a case"() {
        given:
        def aCase = Spy(Case)
        def searchBusinessData = Mock(SearchBusinessData)
        aCase.caseUrl(1L, processAPI, 'myAppContext') >> 'some/url/to/case'
        aCase.newSearchBusinessData(processAPI) >> searchBusinessData
        searchBusinessData.search(_, 'dispute_ref', _) >> Mock(Dispute)
        searchBusinessData.search(_, 'customer_ref', _) >> Stub(Customer)

        when:
        def caseData = aCase.toCase([
            processAPI:processAPI,
            apiClient: context.apiClient,
            searchData:searchBusinessData,
            isOpen:false,
            contextPath:'myAppContext',
            caseId:1L
        ])

        then:
        with(caseData) {
            with(caseUrl){
                href == 'some/url/to/case'
                target == '_self'
            }
        }
    }
}