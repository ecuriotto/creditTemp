package com.company.scenario

import com.company.model.AccountTx
import com.company.model.DisputeStates
import com.company.scenario.process.CreditCardDisputeProcess
import com.company.scenario.util.IdAccessor

class Scenario3 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long danielaAngeloId = withUser(process.apiAccessor, 'daniela.angelo')
        long processInstanceId = process.startCase(customerId, danielaAngeloId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: DisputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, "Waiting for supervisor approval")

        disputeInput.status = DisputeStates.PENDING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
}
