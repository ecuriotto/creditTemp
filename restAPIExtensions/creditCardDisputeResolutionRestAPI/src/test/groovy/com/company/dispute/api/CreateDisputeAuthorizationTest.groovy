package com.company.dispute.api

import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.impl.SearchResultImpl

import com.bonitasoft.engine.api.ProcessAPI

import spock.lang.Specification

class CreateDisputeAuthorizationTest  extends Specification {

    ProcessAPI processAPI = Mock()

    def "should a user be allowed to start a process case"() {
        given:
        def createDisputeAuthorization = new CreateDisputeAuthorization()

        when:
        def canStartProcess = createDisputeAuthorization.userCanStartProcess(processAPI,5L,'My Process')

        then:
        1 * processAPI.searchUsersWhoCanStartProcessDefinition(_, {
            assert it.filters[0].field == UserSearchDescriptor.ID
            assert it.filters[0].value == 5L
            it
        }) >> new SearchResultImpl(1,[Stub(User)])
        canStartProcess == true
    }

    def "should a user not be allowed to start a process case"() {
        given:
        def createDisputeAuthorization = new CreateDisputeAuthorization()

        when:
        def canStartProcess = createDisputeAuthorization.userCanStartProcess(processAPI,5L,'My Process')

        then:
        1 * processAPI.searchUsersWhoCanStartProcessDefinition(_, {
            assert it.filters[0].field == UserSearchDescriptor.ID
            assert it.filters[0].value == 5L
            it
        }) >> new SearchResultImpl(0,[])
        canStartProcess == false
    }
}
