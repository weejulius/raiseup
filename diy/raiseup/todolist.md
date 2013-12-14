 
* [DONE] time slot associates with a specific user
* [DONE] remove the estimation and start time input box in the area to add slot
* [DONE] delete the slot
* [DONE] list the user's slots in the refresh index page
* [DONE] css the slots list in the index page
* [DONE] divide the slots into time planned and unplanned
* start performing the slot
* stop performing the slot
* remove event-id etc keys when cast to read model from event
* refactor to abstract the read model cache interface
* [DONE] support configurations in different env,like production,dev,test

* [DONE] replay the events
* [DONE] move commandbus event store eventbus to a standalone module and abstract them


configuration rd
---
* aligned by module
* switch mode
* override config
* comment is must
* validate items


Validation rd
---

* validate a chain of params
* at least two strategy , one is failed once any one of param is invalid, the other is go through all of params.
* convert params like string to long and so on


Improvements
------------
* [DONE] the command protocol do the validation
* [DONE] add fun to new event or command
* [DONE] the command and query is defined by protocol as part of the public api
* [DONE] the command/event bus utilize the core.asyn handle commands/events
* the code is divided into three module web/api/core/query
* [DONE] abstract the read model
* [DONE] add lifecycle management
** the tests has problem to refresh, as well as the command handler cannot be found
* [DONE] replay the events
* snapshot of ar
* command/query can be passed in by means of http automatically
* [DONE] event needs version and conflict resolver
* command can be composied by comamnds
* [DONE] emit(cmd,timeout) will wait for the result until timeout otherwise exception will be thrown
* the handlers in the same topic should process the events in order
* [DONE] instead of saving the events id for each ar, keep the final state of ar, as a benefit that we need not replay events 
for ar when retrieving one ar
* [DONE] page the index :est 1d
* [DONE] use elasticsearch as read model instead of hazelcast as the query performance :est 3d
* [DONE] delete the note functionality :est 1d
* css the index :est 2d
* [DONE] support markdown syntax to add note
* learn or introduce optimus for static files
* null exception when replay events
* optimize the event which has long text
* http interface to replay events for ar 
* [DONE] add index link to menu nav
* how to fix the data if data has some problem
* monitor the system, like traffic and system usage on so on
* add error message box in the pages
* add performance evaludation for the app

Blocks
------
* the defmethod/extend-protocol stay different place with defmulti/defprotocol, throw no implentation.
* :use the extend-protocols/defmethod when ceating the record
* care about the order if there are two related defmulti/defprotocol




