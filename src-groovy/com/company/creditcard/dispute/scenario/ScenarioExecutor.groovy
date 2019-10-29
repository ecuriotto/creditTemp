package com.company.creditcard.dispute.scenario


import java.util.concurrent.ThreadLocalRandom

import com.bonitasoft.engine.api.APIAccessor
import com.company.creditcard.dispute.model.Account
import com.company.creditcard.dispute.model.AccountTx
import com.company.creditcard.dispute.model.Customer
import com.company.creditcard.dispute.model.DisputeStates
import com.company.creditcard.dispute.scenario.process.CreditCardDisputeProcess

class ScenarioExecutor {

    def static final PENDING = 'PENDING'
    def static final INVALID = 'INVALID'
    def static final INVESTIGATING = 'INVESTIGATING'
    def static final PROCESSING_CHARGEBACK = 'PROCESSING CHARGEBACK'
    def static final CHARGED_BACK = 'CHARGED BACK'
    def static final RESOLVED = 'RESOLVED'
    
    def DisputeStates disputeStates
    def List<Customer> customers
    
    def ScenarioExecutor(DisputeStates disputeStates, List<Customer> customers) {
        this.disputeStates = disputeStates
        this.customers = customers
    }
    
    def runScenario1(CreditCardDisputeProcess process) {
        scenario1(process, customers[0].customerId, pickTx(customers[0].account))
    }
    
    def runScenario2(CreditCardDisputeProcess process) {
        scenario2(process, customers[1].customerId, pickTx(customers[1].account, 500, 0))
    }
    
    def runScenario3(CreditCardDisputeProcess process) {
        scenario3(process, customers[2].customerId, pickTx(customers[2].account, 10000, 501))
    }
    
    def runScenario4(CreditCardDisputeProcess process) {
        scenario4(process, customers[3].customerId, pickTx(customers[3].account, 500, 0))
    }
    
    def runScenario5(CreditCardDisputeProcess process) {
        scenario5(process, customers[4].customerId, pickTx(customers[4].account))
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
    
    def scenario1(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long processInstanceId = process.startCase(customerId, withUser(process.apiAccessor, 'misa.kumagai'))

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: disputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(withUser(process.apiAccessor, 'misa.kumagai'),processInstanceId,[disputeInput:disputeInput])
    }
    
    def scenario2(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: '',
            amount: tx.amount,
            status: disputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long josephHovellId = withUser(process.apiAccessor, 'joseph.hovell')

        process.investigateTransaction(josephHovellId, processInstanceId, """Found the merchant identification number: $tx.merchantIdNumber
            It appears that the vendor is bankrupt.
            Processing chargeback.""")

        disputeInput.merchantIdNumber = tx.merchantIdNumber
        disputeInput.status = disputeStates.PROCESSING_CHARGEBACK
        process.updateDispute(josephHovellId, processInstanceId, [disputeInput:disputeInput])
    }
    
    def scenario3(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
              long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: disputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, "Waiting for supervisor approval")

        disputeInput.status = disputeStates.PENDING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
    
    
    def scenario4(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: disputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, null)

        disputeInput.status = disputeStates.PROCESSING_CHARGEBACK
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.chargeback(marcMarseauId, processInstanceId, "Chargeback request aprroved")
    }
    
    def scenario5(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: disputeStates.PENDING,
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')
        disputeInput.status = disputeStates.INVESTIGATING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.callCustomer(marcMarseauId, processInstanceId, """Ask customer to send receipt
            Putting request in pending until then.""")

        disputeInput.status = disputeStates.PENDING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
    
    def long withUser(APIAccessor apiAccessor, String username) {
        apiAccessor.getIdentityAPI().getUserByUserName(username).id
    }

}
