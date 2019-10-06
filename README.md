# Credit card dispute resolution

This repository contains a Bonita project that leverage Adaptive Case Management with Bonita.
It is an example application of a credit card dispute resolution application.

## How to contribute

### Clone this repository

To setup a development environment you must have a Bonita Studio Enterprise edition (7.9.0 or above)
Clone this repository in your workspace (Team menu -> Git -> Clone) and paste the [repository URL](https://github.com/bonitasoft/credit-card-dispute-resolution.git)

### Enable ACM event handler

Go in the preference of your Studio, in the Server settings category, in Tomcat JVM arguments, set the `com.bonitasoft.engine.plugin.acm.REGISTER_ACM_HANDLER` property to `true` (`false` by default)

### Deploy the application

1. Set the default environment to `Development` (Right click on Environments > Development.xml file and Set as active environment)
1. Deploy the Rest API extension
1. Deploy all the processes
1. Open the credit-card-dispute-resolution.xml application descriptors and click on the overview URL
1. Follow the instructions of the tutorial

### Contribute

If you want to contribute a new feature or a bugfix, create a branch and make a pull request that can be reviewed before its integration.

## Project content

### Organization

Reuse the default ACME organization.

### Business Data Model

* Dispute : A simple representation of a dispute. A dispute instance is always bound to a `Credit Card Dispute` case. An instance can be updated using the `Update Dispute` discretionary task.
* Account: The account of the customer claiming a dispute.
	* AccountTx: Represents a transaction on an _Account_.
* Customer: Customer informations

### Profiles

Reuse the default profiles

### Application descriptors

* _credit-card-dispute-resolution.xml_ : The application descriptor for the application. The default Bonita layout is used for this app. A _Tutorial_ page is used as landing page to present the application example.

### Diagrams

* Credit Card Dispute: The main process for this example.
* Data Initialization: A helper process used for demonstration and testing purposes. It is a fully automated process that can be start from the _Tutorial_ page instantiating fake _Customers_ and their _Accounts_


### Groovy scripts

* ACMStates.groovy: Utility class with the ACM states constants
* DisputeStates.groovy: Utility class with the Dispute states constants
* DataUpdateRecorder.groovy: Utility class used when updating Dispute data or Parameters to track the values changes.

### Forms and Pages

#### Pages

* CaseList : List active and archived dispute cases. Process initiator actor (_Customer Service_) can start a new Dispute from this page.
* Case: An overhaul case view of a Dispute case.
* ExecuteManualTask: A generic page use to execute _Manual_ task. _Manual_ tasks are tasks created during the case execution.
* Tutorial: The tutorial page presenting the application and how to use it.

#### Forms

* AddCommentForm: A generic form submitting the task and attaching a comment on the case.
* EditDispute: The form used to update the Dispute information.
* DisputeInfo: The form used by _Customer Service_ to create a new _Dispute_.
* UpdateParameters: The form used by the _Supervisor_ to update the parameters of the Credit Card Dispute process (currently the _reviewAmountThreshold_ parameter).
* ValidateDisputeForm: The form used by the _Supervisor_ to approve or reject the dispute.

### Custom widgets

* HTMLDataTable: The default DataTable with HTML support added in cells.
* Timeline: A timeline widget used in the _Case_ page.
* ToggleButtons: Toggle buttons used in the _CaseList_ page to switch between active and archived cases.

### Themes

* creditCardTheme: 
	* Customize the application `logo.png` and `favicon.ico`
	* Customize the link button color
	* Add custom css rule to define a light background container style
	* Add custom css rules to constrained maximum page with in `_layout.scss`
	* Add custom css rules for card style `_card.scss`
	* Add custom css rules for timeline style `_timeline.scss`
	* Add custom css rules for the button bar style `_button-bar.scss`

### Rest API Extensions

* **GET** `API/extension/case?p=<INTEGER>&c=<INTEGER>&s=<STRING>`: Retrieves the list of active Dispute cases (Dispute information are aggregated to case information). `s` parameter is used to filter result using search indexes.
* **GET** `API/extension/archivedCase?p=<INTEGER>&c=<INTEGER>&s=<STRING>`: Retrieves the list of archived Dispute cases (Dispute information are aggregated to case information). `s` parameter is used to filter result using search indexes.
* **GET** `API/extension/caseActivity?caseId=<LONG>`: Retrieves the list of the tasks for a given case.
* **POST** `API/extension/caseActivity`: Create a new Task in a case. Payload: ```{ "name", "caseId"}```
* **GET** `API/extension/caseComment`: Retrieves the of comments for a given case.
* **GET** `API/extension/caseHistory?caseId=<LONG>`: Retrieves the case history for a given case.
* **GET** `API/extension/caseDocument?caseId=<LONG>`: Retrieves the documents for a given case.
* **DELETE** `API/extension/caseDocument?documentId=<LONG>`: Deletes the document with the given id.
* **GET** `API/extension/dispute?caseId=<LONG>`: Retrieves the Dispute informations for a given case.
* **GET** `API/extension/customerInfo?caseId=<LONG>`: Retrieves the Customer informations for a given case.
* **GET** `API/extension/createDisputeAuthorization`: Return if the logged user can start a new dispute case.
* **POST** `API/extension/userTask`: Submit the task with `taskId` and add a comment to the case `content` is provided. Payload: ```{ "taskId", "content" }```
		
### Environments

Development.xml : a configured environment with example values (actor mapping, parameters...)
