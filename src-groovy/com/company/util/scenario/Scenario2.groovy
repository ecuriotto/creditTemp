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

class Scenario2 {

	def execute(APIAccessor apiAccessor, String customerId, AccountTx tx) {
        
        long danielaAngeloId = withUser(apiAccessor,'daniela.angelo')
		long processInstanceId = startCase(apiAccessor, customerId, danielaAngeloId)

		def disputeInput = [
			currency: tx.currency,
			merchantIdNumber: '',
			amount: tx.amount,
			status: DisputeStates.INVESTIGATING,
			txDate:tx.txDate
		]

		gatherInformation(apiAccessor, danielaAngeloId,processInstanceId,[disputeInput:disputeInput])

        long josephHovellId = withUser(apiAccessor,'joseph.hovell')
        
        investigateTransaction(apiAccessor, josephHovellId, processInstanceId, """Found the merchant identification number: $tx.merchantIdNumber
            It appears that the vendor is bankrupt.
            Processing chargeback.""")
        
        disputeInput.merchantIdNumber = tx.merchantIdNumber
        disputeInput.status = DisputeStates.PROCESSING_CHARGEBACK
		updateDispute(apiAccessor, josephHovellId, processInstanceId, [disputeInput:disputeInput])
	}
}
