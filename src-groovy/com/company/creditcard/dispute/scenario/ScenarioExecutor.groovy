package com.company.creditcard.dispute.scenario

import static com.company.creditcard.dispute.scenario.Condition.loop

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeoutException

import org.bonitasoft.engine.bpm.flownode.ActivityStates
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import com.bonitasoft.engine.api.APIAccessor
import com.company.creditcard.dispute.model.Account
import com.company.creditcard.dispute.model.AccountTx
import com.company.creditcard.dispute.model.Customer

class ScenarioExecutor {

    def run(APIAccessor apiAccessor, List<Customer> customers) {
        new Scenario1().execute(new CreditCardDisputeProcess(apiAccessor), customers[0].customerId, pickTx(customers[0].account))
        new Scenario2().execute(new CreditCardDisputeProcess(apiAccessor), customers[1].customerId, pickTx(customers[1].account, 500, 0))
        new Scenario3().execute(new CreditCardDisputeProcess(apiAccessor), customers[2].customerId, pickTx(customers[2].account, 10000, 501))
        new Scenario4().execute(new CreditCardDisputeProcess(apiAccessor), customers[3].customerId, pickTx(customers[3].account, 500, 0))
        new Scenario5().execute(new CreditCardDisputeProcess(apiAccessor), customers[4].customerId, pickTx(customers[4].account))
    }

    def AccountTx pickTx(Account account, double maxValue, double minValue) {
        def tx = pickTx(account)
        while (!(tx.amount >= minValue && tx.amount < maxValue)) {
            tx = pickTx(account)
        }
        tx
    }

    def AccountTx pickTx(Account account) {
        account.transactions.get(ThreadLocalRandom.current().nextInt(0, account.transactions.size()-1))
    }
    
}

class Scenario1 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long processInstanceId = process.startCase(customerId, withUser(process.apiAccessor, 'misa.kumagai'))

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: 'INVESTIGATING',
            txDate:tx.txDate
        ]

        process.gatherInformation(withUser(process.apiAccessor, 'misa.kumagai'),processInstanceId,[disputeInput:disputeInput])
    }
}

class Scenario2 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: '',
            amount: tx.amount,
            status: 'INVESTIGATING',
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long josephHovellId = withUser(process.apiAccessor, 'joseph.hovell')

        process.investigateTransaction(josephHovellId, processInstanceId, """Found the merchant identification number: $tx.merchantIdNumber
            It appears that the vendor is bankrupt.
            Processing chargeback.""")

        disputeInput.merchantIdNumber = tx.merchantIdNumber
        disputeInput.status = 'PROCESSING CHARGEBACK'
        process.updateDispute(josephHovellId, processInstanceId, [disputeInput:disputeInput])
    }
}

class Scenario3 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: 'INVESTIGATING',
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, "Waiting for supervisor approval")

        disputeInput.status = 'PENDING'
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
}


class Scenario4 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: 'INVESTIGATING',
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, null)

        disputeInput.status = 'PROCESSING CHARGEBACK'
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.chargeback(marcMarseauId, processInstanceId, "Chargeback request aprroved")
    }
}

class Scenario5 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: 'PENDING',
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')
        disputeInput.status ='INVESTIGATING'
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.callCustomer(marcMarseauId, processInstanceId, """Ask customer to send receipt
            Putting request in pending until then.""")

        disputeInput.status ='PENDING'
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
}

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
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:'RESOLVED'])
    }

    def callCustomer(long userId, long processInstanceId, String comment) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Call customer'), [:])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }

    def dismissCase(long userId, long processInstanceId) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Close'), [status:'INVALID'])
    }


    def review(APIAccessor apiAccessor, long userId, long processInstanceId, String comment, boolean approveDisputedAmount) {
        apiAccessor.getProcessAPI().assignAndExecuteUserTask(userId, taskId(apiAccessor,processInstanceId, 'Chargeback'), [approveDisputedAmount:approveDisputedAmount])
        if(comment) {
            apiAccessor.getProcessAPI().addProcessCommentOnBehalfOfUser(processInstanceId, comment, userId)
        }
    }
}

trait IdAccessor {

    def long withUser(APIAccessor apiAccessor, String username) {
        apiAccessor.getIdentityAPI().getUserByUserName(username).id
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
