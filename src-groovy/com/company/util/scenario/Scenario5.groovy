package com.company.util.scenario

import static com.company.util.scenario.ScenarioProvider.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

import com.bonitasoft.engine.api.APIAccessor
import com.company.model.AccountTx
import com.company.model.DisputeStates

class Scenario5 {

	def execute(APIAccessor apiAccessor, String customerId, AccountTx tx) {
        long giovannaAlmeidaId = withUser(apiAccessor,'giovanna.almeida')
		long processInstanceId = startCase(apiAccessor, customerId, giovannaAlmeidaId)

		def disputeInput = [
			currency: tx.currency,
			merchantIdNumber: tx.merchantIdNumber,
			amount: tx.amount,
			status: DisputeStates.PENDING,
			txDate:tx.txDate
		]

		gatherInformation(apiAccessor, giovannaAlmeidaId,processInstanceId,[disputeInput:disputeInput])

        long marcMarseauId = withUser(apiAccessor,'marc.marseau')
        disputeInput.status = DisputeStates.INVESTIGATING
        updateDispute(apiAccessor, marcMarseauId, processInstanceId, [disputeInput:disputeInput])
        
        callCustomer(apiAccessor, marcMarseauId, processInstanceId, """Ask customer to send receipt
            Putting request in pending until them.""")

        disputeInput.status = DisputeStates.PENDING
		updateDispute(apiAccessor, marcMarseauId, processInstanceId, [disputeInput:disputeInput])
        
	}
}
