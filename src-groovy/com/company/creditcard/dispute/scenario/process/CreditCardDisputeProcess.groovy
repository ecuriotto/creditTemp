package com.company.creditcard.dispute.scenario.process

import com.bonitasoft.engine.api.APIAccessor
import com.company.creditcard.dispute.model.DisputeStates
import com.company.creditcard.dispute.scenario.util.IdAccessor

class CreditCardDisputeProcess implements IdAccessor {

    APIAccessor apiAccessor

    CreditCardDisputeProcess(APIAccessor apiAccessor){
        this.apiAccessor = apiAccessor
    }

    def long startCase(String customerId, long userId) {
        apiAccessor.getProcessAPI().startProcessWithInputs(userId, apiAccessor.getProcessAPI().getLatestProcessDefinitionId('Credit Card Dispute'), [customerId:customerId]).id
    }

    def gatherInformation(long userId, long processInstanceId, Map taskInputs) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Gather Dispute Details'), taskInputs)
    }

    def updateDispute(long userId, long processInstanceId, Map taskInputs) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Update Dispute'), taskInputs)
    }

    def investigateTransaction(long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Investigate transaction'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def chargeback(long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def resolveCase(long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:DisputeStates.RESOLVED])
    }

    def callCustomer(long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Call customer'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def dismissCase(long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:DisputeStates.INVALID])
    }


    def review(APIAccessor apiAccessor, long userId, long processInstanceId, String comment, boolean approveDisputedAmount) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [approveDisputedAmount:approveDisputedAmount])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
}
