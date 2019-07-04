package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonSlurper
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