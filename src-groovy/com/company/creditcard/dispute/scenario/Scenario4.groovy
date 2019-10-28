package com.company.creditcard.dispute.scenario

import com.company.creditcard.dispute.model.AccountTx
import com.company.creditcard.dispute.model.DisputeStates
import com.company.creditcard.dispute.scenario.process.CreditCardDisputeProcess
import com.company.creditcard.dispute.scenario.util.IdAccessor

class Scenario4 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: DisputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')

        process.investigateTransaction(marcMarseauId, processInstanceId, null)

        disputeInput.status = DisputeStates.PROCESSING_CHARGEBACK
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.chargeback(marcMarseauId, processInstanceId, "Chargeback request aprroved")
    }
}
