package com.company.dispute.api

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.bpm.comment.Comment
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.Order
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.engine.api.APIClient
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.engine.api.ProcessAPI
import com.bonitasoft.web.extension.rest.RestAPIContext

import groovy.json.JsonSlurper
import spock.lang.Specification

class CaseCommentTest extends Specification {

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
        def caseComment = new CaseComment()

        when:
        def restApiResponse = caseComment.doHandle(request, new RestApiResponseBuilder() , context)

        then:
		assert restApiResponse.httpStatus == 400
		assert new JsonSlurper().parseText(restApiResponse.response).error == 'the parameter caseId is missing'
    }
	
	def "should query case comments with proper search options"() {
		given:
		def caseComment = new CaseComment()
		def SearchResult commentsResult = Mock()
		
		when:
		request.getParameter('caseId') >> 1L
		def restApiResponse = caseComment.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		1 * processAPI.searchComments({ SearchOptions searchOptions ->
			
			assert searchOptions.filters[0].field == SearchCommentsDescriptor.PROCESS_INSTANCE_ID
			assert searchOptions.filters[0].value == 1L
			
			assert searchOptions.sorts[0].field == SearchCommentsDescriptor.POSTDATE
			assert searchOptions.sorts[0].order == Order.DESC
			
			searchOptions
				
		}) >> commentsResult
	}
	
	def "should return the list of comments for a given case id"() {
		given:
		def caseComment = new CaseComment()
		def SearchResult commentsResult = Mock()
		def c1 = Stub(Comment){
			it.content >> 'Hello'
			it.postDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45").time
			it.userId >> 4L
		}
		def c2 = Stub(Comment){
			it.content >> 'Hello\nWorld'
			it.postDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-06 4:23:45").time
			it.userId >> 5L
		}
		commentsResult.result >> [c1,c2]
		processAPI.searchComments(_) >> commentsResult
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
		def restApiResponse = caseComment.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		def comments = new JsonSlurper().parseText(restApiResponse.response)
		assert comments.size() == 2
		
		assert comments[0].content == 'Hello'
		assert comments[0].postDate == Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45").time
		assert comments[0].username == 'Walter Bates'
		
		assert comments[1].content == 'Hello<br>World'
		assert comments[1].postDate == Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-06 4:23:45").time
		assert comments[1].username == 'Helen Kelly'
	}
	
	def "should filter system comments"() {
		given:
		def caseComment = new CaseComment()
		def SearchResult commentsResult = Mock()
		def systemComment = Stub(Comment){
			it.content >> 'Task finished'
			it.postDate >> Date.parse("yyyy-MM-dd hh:mm:ss", "2019-04-03 1:23:45").time
			it.userId >> -1
		}
		commentsResult.result >> [systemComment]
		processAPI.searchComments(_) >> commentsResult
		
		
		when:
		request.getParameter('caseId') >> 1L
		def restApiResponse = caseComment.doHandle(request, new RestApiResponseBuilder() , context)

		then:
		assert restApiResponse.httpStatus == 200
		def comments = new JsonSlurper().parseText(restApiResponse.response)
		assert comments.size() == 0
	}
	
}