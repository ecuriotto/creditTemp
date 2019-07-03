package com.company.dispute.api

import java.time.LocalDateTime

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.dispute.api.Case
import com.company.dispute.api.CaseDocument

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.bonitasoft.engine.bpm.comment.Comment
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor
import org.bonitasoft.engine.bpm.data.DataInstance
import org.bonitasoft.engine.bpm.document.Document
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ActivityInstance
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
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
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import spock.lang.Specification

class CaseDocumentTest extends Specification {

    ProcessAPI processAPI = Mock()
	IdentityAPI identityAPI = Mock()
    APIClient apiClient = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
		apiClient.getIdentityAPI() >> identityAPI
    }

    def "should return a bad request code when no caseId parameter is found"() {
        given:
        def caseDocument = new  CaseDocument()

		
        when:
        def restApiResponse = caseDocument.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
    }
	
	def "should query case comments with proper search options"() {
		given:
		def caseDocument = new CaseDocument()
		def SearchResult documentsResult = Mock()
		
		when:
		request.getParameter('caseId') >> 1L
		def restApiResponse = caseDocument.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.searchDocuments({ SearchOptions searchOptions ->
			
			assert searchOptions.filters[0].field == DocumentsSearchDescriptor.PROCESSINSTANCE_ID
			assert searchOptions.filters[0].value == 1L
			
			assert searchOptions.sorts[0].field == DocumentsSearchDescriptor.DOCUMENT_CREATIONDATE
			assert searchOptions.sorts[0].order == Order.DESC
			
			searchOptions
				
		}) >> documentsResult
	}
	
	def "should return the list of documents for a given case id"() {
		given:
		def caseDocument = new CaseDocument()
		def SearchResult documentsResult = Mock()
		def d1 = Stub(Document){
			it.name >> 'Hello'
			it.url >> 'some/url'
			it.creationDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45")
			it.contentFileName >> 'world.jpg'
			it.id >> 33L
			it.author >> 4L
		}
		def d2 = Stub(Document){
			it.name >> 'Foo'
			it.url >> 'another/url'
			it.creationDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-05 8:23:45")
			it.contentFileName >> 'bar.pdf'
			it.description >> 'desc\nsome text'
			it.id >> 34L
			it.author >> 5L
		}
		documentsResult.result >> [d1,d2]
		processAPI.searchDocuments(_) >> documentsResult
		def walter = Stub(User){
			it.id >> 4L
			it.firstName >> 'Walter'
			it.lastName >> 'Bates'
			it.username >> 'walter.bates'
		}
		def helen = Stub(User){
			it.id >> 5L
			it.firstName >> 'Helen'
			it.lastName >> 'Kelly'
			it.username >> 'helen.kelly'
		}
		identityAPI.getUser(4L) >> walter
		identityAPI.getUser(5L) >> helen
		
		
		when:
		request.getParameter('caseId') >> 1L
		def restApiResponse = caseDocument.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		def documents = new JsonSlurper().parseText(restApiResponse.response)
		assert documents.size() == 2
		
		assert documents[0].name == 'Hello'
		assert documents[0].fileName == 'world.jpg'
		assert documents[0].username == 'Walter Bates'
		
		assert documents[1].name == 'Foo'
		assert documents[1].description == 'desc<br>some text'
		assert documents[1].username == 'Helen Kelly'
	}
	
	
}