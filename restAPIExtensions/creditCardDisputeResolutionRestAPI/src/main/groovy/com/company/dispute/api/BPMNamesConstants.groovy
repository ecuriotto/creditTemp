/*******************************************************************************
 * Copyright (C) 2019 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel � 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/

package com.company.dispute.api

interface BPMNamesConstants {

    def static final DISPUTE_PROCESS_NAME = 'Credit Card Dispute'

    def static final ACTIVITY_CONTAINER = 'Dynamic Activity Container'
    def static final CREATE_ACTIVITY = 'Create Activity'
    def static final CLOSE_ACTIVITY = 'Close'
    def static final HIDDEN_ACTIVITIES = [
        ACTIVITY_CONTAINER,
        CREATE_ACTIVITY,
        CLOSE_ACTIVITY
    ]
    def static final ACTIVITY_STATE_DATA_NAME = '$activityState'

    def static final REQUIRED_STATE = 'Required'
    def static final OPTIONAL_STATE = 'Optional'
    def static final DISCRETIONARY_STATE = 'Discretionary'
    def static final NOT_AVAILABLE_STATE = 'N/A'
}
