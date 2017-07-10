# Notifier

The Notifier is necessary to replace `System.out.println` in all places of our system.
To use message sending for cases when it's necessary to notify a user of the system of some events.

## Components diagram

![Diagram](https://www.planttext.com/plantuml/img/fLFBReCm4BpxA_ROIeLJZmYffQg4b3uaFg1otGrrQcnaI9keoR_Nu9Y7JN8f1mIxipFhsJKLGlsyLNp7PKa8PQtIa0cEiYMafkoBbWXIjNpDKSSRgk6N430F4xHfuFcKWIGfK7zpXh1IZ1gID3L8E3qDoe-h2fl6iDq0xXZMBLVoUX5WeqShOrk1A1sa-d3_uh95lQP6Cn_w-AVcm3qG6kKybBQdByJwTXCcnswIGnITGpbp-_GOdg4xTI_lobQgV3UVNc5Nu3BaE4xG4JuYRRSQvqDyiXuKne-XpQM3BE4yHrpyhjkZIL9LjPBT87l0FlkaN7fcs-1qqXx9WZn43kvJpjjv5TjocXqtno7kZL66sEnn_fB10zRc1oNRfscu4trVe_HOVNQAqU42TjuanF6I0m4e3XcxWTiBFB7AtUKW19vrv0y0)

## Principles of operation

There are two entry points:

* `Notifier` service locator. Any code may call `Notifier.send()`.
* When you have MessageProcessor and ability to send messages, you can send a message to a NotifyChain or put a NotifierActor to your chain, or use the NotifierActor in `exceptional` clause of your chain.

The actual notification is made by the [slf4j](https://www.slf4j.org/) library, and [Logback](https://logback.qos.ch/).

On initial phase of the server start, when no chains and actors are available, slf4j is used directly.
When the NotifierChain and NotifierActor become available, the messages are sent to them.
It's the responsibility of `Notifier` service locator to choose the implementation available currently.

The NotifierActor internally uses slf4j again.

## Extension points

The NotifierActor uses a NotifierBackend registered in IOC to perform the actual notification.
While by default the slf4j logging is used, this implementation may be override.

Slf4j can use another backend, not Logback. Logback also can be configured by tons of customized appenders.

## Deploy units

Notifier service locator, slf4j implementation and Logback are put together in a single `notifier.jar`.
It's allows to easy add notifications to the server.

However, it's possible to take only Notifier service and interfaces to slf4j, provide another slf4j backend and configuration to make customized logging.

NotifierFeature is deployed as usual feature zip archive. It includes NotifierActor and NotifyChain, i.e. it allows to use the second entry point.
