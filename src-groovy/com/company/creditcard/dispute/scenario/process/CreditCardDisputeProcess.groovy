package com.company.creditcard.dispute.scenario.process

import static com.company.creditcard.dispute.scenario.process.Condition.loop

import java.util.concurrent.TimeoutException

import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder

import com.bonitasoft.engine.api.APIAccessor
import com.company.creditcard.dispute.model.DisputeStates


class CreditCardDisputeProcess {

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
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status: DisputeStates.RESOLVED])
    }

    def callCustomer(long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Call customer'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def dismissCase(long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status: DisputeStates.INVALID])
    }


    def review(APIAccessor apiAccessor, long userId, long processInstanceId, String comment, boolean approveDisputedAmount) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [approveDisputedAmount:approveDisputedAmount])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def long taskId(APIAccessor apiAccessor, long processInstanceId, String taskName) {
        long taskId = -1
        loop {
            def searchResult = apiAccessor.getProcessAPI().searchHumanTaskInstances(new SearchOptionsBuilder(0, 1)
                    .filter(HumanTaskInstanceSearchDescriptor.NAME, taskName)
                    .filter(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceId)
                    .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE)
                    .done())
            taskId = searchResult.count == 1 ? searchResult.getResult()[0].id : -1
        } until { taskId >= 0 }
        return taskId
    }
}


class Condition {

    private Closure code

    int timeout = 0;

    static Condition loop( Closure code ) {
        new Condition(code:code)
    }

    void until( Closure test ) {
        if(timeout > 30000) {
            throw new TimeoutException("Condition cannot be evaluated to true before the timeout expires.")
        }
        Thread.sleep(100)
        code()
        while (!test()) {
            Thread.sleep(100)
            timeout += 100
            code()
        }
    }
}
