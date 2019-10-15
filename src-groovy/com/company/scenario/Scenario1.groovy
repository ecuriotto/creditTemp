package com.company.scenario

import com.company.model.AccountTx
import com.company.model.DisputeStates
import com.company.scenario.process.CreditCardDisputeProcess
import com.company.scenario.util.IdAccessor

class Scenario1 implements IdAccessor {

    def execute(CreditCardDisputeProcess process, String customerId, AccountTx tx) {
        long processInstanceId = process.startCase(customerId, withUser(process.apiAccessor, 'misa.kumagai'))

        def disputeInput = [
            currency: tx.currency,
            merchantIdNumber: tx.merchantIdNumber,
            amount: tx.amount,
            status: DisputeStates.INVESTIGATING,
            txDate:tx.txDate
        ]

        process.gatherInformation(withUser(process.apiAccessor, 'misa.kumagai'),processInstanceId,[disputeInput:disputeInput])
    }
}
