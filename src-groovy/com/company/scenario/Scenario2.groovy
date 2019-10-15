package com.company.scenario

import com.company.model.AccountTx
import com.company.model.DisputeStates
import com.company.scenario.process.CreditCardDisputeProcess
import com.company.scenario.util.IdAccessor

class Scenario2 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: '',
            amount: tx.amount,
            status: DisputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long josephHovellId = withUser(process.apiAccessor, 'joseph.hovell')

        process.investigateTransaction(josephHovellId, processInstanceId, """Found the merchant identification number: $tx.merchantIdNumber
            It appears that the vendor is bankrupt.
            Processing chargeback.""")

        disputeInput.merchantIdNumber = tx.merchantIdNumber
        disputeInput.status = DisputeStates.PROCESSING_CHARGEBACK
        process.updateDispute(josephHovellId, processInstanceId, [disputeInput:disputeInput])
    }
}
