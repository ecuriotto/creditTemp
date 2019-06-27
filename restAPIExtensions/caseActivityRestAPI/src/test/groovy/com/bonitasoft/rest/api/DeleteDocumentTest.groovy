package com.bonitasoft.rest.api

import java.time.LocalDateTime

import javax.servlet.http.HttpServletRequest

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.rest.api.Case
import com.bonitasoft.web.extension.rest.RestAPIContext

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

class DeleteDocumentTest extends Specification {

    ProcessAPI processAPI = Mock()
    APIClient apiClient = Mock()
    HttpServletRequest request = Mock()
    RestAPIContext context = Mock()

    def "setup"() {
        context.apiClient >> apiClient
        apiClient.getProcessAPI() >> processAPI
    }

    def "should return a bad request code when no documentId parameter is found"() {
        given:
        def deleteDocument = new  DeleteDocument()

		
        when:
        def restApiResponse = deleteDocument.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter documentId is missing'
    }
	
	def "should delete the document with the given id"() {
		given:
		def deleteDocument = new DeleteDocument()
		def SearchResult documentsResult = Mock()
		
		when:
		request.getParameter('documentId') >> 1L
		def restApiResponse = deleteDocument.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.removeDocument(1L)
		def removedDoc = new JsonSlurper().parseText(restApiResponse.response)
		assert removedDoc.id == "1"
	}

}