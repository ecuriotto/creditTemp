<a name="1.0.0 RC"></a>
# [1.0.0 RC](https://github.com/bonitasoft/credit-card-dispute-resolution/compare/1.0.0-Beta...7.9.0) (2019-10-28)


### Bug Fixes

* **Dependencies:** [jfairy](https://github.com/Devskiller/jfairy) jar dependency has been updated to remove Guava from the uber jar. Guava 19.0 is provided in the parent Classloader.

### Features/Improvements

* **Tutorial:** Add an error toast if data initialization fails 
* **Tutorial:** If the DataInitialization process is not deployed, the _Create customers, accounts, transactions and disputes_ button is disabled
* **Tutorial:** Depending on the Bonita version, the deployment and handler activation instructions change.
* **Tutorial:** Bonita and application version is displayed.
* **Tutorial:** Do not display the _Data initialization_ section if data is already initialized.
* **Generic forms:** Increase text area default height.
* **Update parameters:** Use a word case for parameter names.
* **Update parameters:** Only display business parameters. Technical parameters must be prefixed by an `_`.
* **Dispute and Customer info fragments:** Use a custom Panel widget based on bootstrap panel that supports buttons (for customer account details)
* **Credit Card Dispute Process:** Add an email connector on the _Chargeback_ task to notify the customer.
* **Environment:** use the _Local_ environment instead of the _Development_ to ease the first Deploy operation.
* **Packages:** use a unique package name `com.company.creditcard.dispute` for BDM and sources

