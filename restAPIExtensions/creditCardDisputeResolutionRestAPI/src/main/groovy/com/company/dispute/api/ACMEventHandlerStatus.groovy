/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ACMEventHandlerStatus implements RestApiController {

	private static final String ACM_EVENT_HANDLER_PROPERTY = 'com.bonitasoft.engine.plugin.acm.REGISTER_ACM_HANDLER'

	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		return responseBuilder.with {
			withResponseStatus(HttpServletResponse.SC_OK)
			withResponse(new JsonBuilder([eventHandlerEnabled: Objects.equals(System.getProperty(ACM_EVENT_HANDLER_PROPERTY),"true")]).toString())
			build()
		}
	}
}
