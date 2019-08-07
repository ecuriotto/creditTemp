# credit-card-dispute-resolution

This repository contains a bonita project that leverage Adaptive Case Management for Bonita.
It is an example application of a credit card dispute resolution application.

## How to contribute

### Clone this repository

To setup a development environment you must have a Bonita Studio Enterprise edition (7.9.0 or above)
Clone this repository in your worksapce (Team menu -> Git -> Clone) and paste the [repository url](https://github.com/bonitasoft/credit-card-dispute-resolution.git)

### Enable ACM event handler

Go in the preference of your Studio, in the Server settings category, in Tomcat JVM arguments, set the `com.bonitasoft.engine.plugin.acm.REGISTER_ACM_HANDLER` property to `true` (`false` by default)

### Deploy the application

1. Set the default envrionment to `Development` (Right click on Environments > Development.xml file and Set as active environment)
1. Deploy the Rest API extension
1. Deploy all the processes
1. Open the Tutorial application descriptors and click on the overview url
1. Follow the instructions of the tutorial

### Contribute

If you want to contribute a new feature or a bugfix, create a branch and make a pull request that can be reviewed before its integration.
