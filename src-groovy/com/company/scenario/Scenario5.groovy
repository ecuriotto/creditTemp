package com.company.scenario

import com.company.model.AccountTx
import com.company.model.DisputeStates
import com.company.scenario.process.CreditCardDisputeProcess
import com.company.scenario.util.IdAccessor

class Scenario5 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(process.apiAccessor, 'giovanna.almeida')
        long processInstanceId = process.startCase(customerId, giovannaAlmeidaId)

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: DisputeStates.PENDING,
            txDate:tx.txDate
        ]

        process.gatherInformation(giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(process.apiAccessor, 'marc.marseau')
        disputeInput.status = DisputeStates.INVESTIGATING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])

        process.callCustomer(marcMarseauId, processInstanceId, """Ask customer to send receipt
            Putting request in pending until then.""")

        disputeInput.status = DisputeStates.PENDING
        process.updateDispute(marcMarseauId, processInstanceId, [disputeInput:disputeInput])
    }
}
