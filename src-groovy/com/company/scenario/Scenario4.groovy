package com.company.scenario

import com.company.model.AccountTx
import com.company.model.DisputeStates
import com.company.scenario.process.CreditCardDisputeProcess
import com.company.scenario.util.IdAccessor

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
