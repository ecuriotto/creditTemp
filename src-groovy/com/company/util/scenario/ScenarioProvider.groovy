package com.company.util.scenario

import static com.company.util.Condition.loop

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.ThreadLocalRandom

import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder

import com.bonitasoft.engine.api.APIAccessor
import com.company.model.Account
import com.company.model.AccountTx
import com.company.model.Customer
import com.company.model.DisputeStates
import com.company.util.Condition

class ScenarioProvider {
	
	def static playScenarii(APIAccessor apiAccessor, List<Customer> customers) {
		new Scenario1().execute(apiAccessor, customers[0].customerId, pickTx(customers[0].account))
		new Scenario2().execute(apiAccessor, customers[1].customerId, pickTx(customers[1].account, 500, 0))
        new Scenario3().execute(apiAccessor, customers[2].customerId, pickTx(customers[2].account, 10000, 501))
        new Scenario4().execute(apiAccessor, customers[3].customerId, pickTx(customers[3].account, 500, 0))
        new Scenario5().execute(apiAccessor, customers[4].customerId, pickTx(customers[4].account))
	}
	
	def static long withUser(APIAccessor apiAccessor, String username) {
		apiAccessor.getIdentityAPI().getUserByUserName(username).id
	}
	
	def static long startCase(APIAccessor apiAccessor, String customerId, long userId) {
		apiAccessor.getProcessAPI().startProcessWithInputs(userId, apiAccessor.getProcessAPI().getLatestProcessDefinitionId('Credit Card Dispute'), [customerId:customerId]).id
	}
	
	def static gatherInformation(APIAccessor apiAccessor, long userId, long processInstanceId, Map taskInputs) {
		apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Gather Dispute Details'), taskInputs)
	}
	
	def static updateDispute(APIAccessor apiAccessor, long userId, long processInstanceId, Map taskInputs) {
		apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Update Dispute'), taskInputs)
	}
    
    def static investigateTransaction(APIAccessor apiAccessor, long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Investigate transaction'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
    
    def static chargeback(APIAccessor apiAccessor, long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
    
    def static resolveCase(APIAccessor apiAccessor, long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:DisputeStates.RESOLVED])
    }
    
    def static callCustomer(APIAccessor apiAccessor, long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Call customer'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
    
    def static dismissCase(APIAccessor apiAccessor, long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:DisputeStates.INVALID])
    }
    
    
    def static review(APIAccessor apiAccessor, long userId, long processInstanceId, String comment, boolean approveDisputedAmount) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [approveDisputedAmount:approveDisputedAmount])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
	
	def static long taskId(APIAccessor apiAccessor, long processInstanceId, String taskName) {
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
	
	def static AccountTx pickTx(Account account) {
		account.transactions.get(ThreadLocalRandom.current().nextInt(0, account.transactions.size()-1))
	}
    
    def static AccountTx pickTx(Account account, double maxValue, double minValue) {
        def tx = pickTx(account)
        while (!(tx.amount >= minValue && tx.amount < maxValue)) {
           tx = pickTx(account)
        }
        tx
    }
}
