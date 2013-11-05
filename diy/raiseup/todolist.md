 
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
* [==]   the command and query is defined by protocol as part of the public api
* [DONE] the command/event bus utilize the core.asyn handle commands/events
* the code is divided into three module web/api/core/query
* [DONE] abstract the read model
* add lifecycle management
* replay the events
* snapshot of ar
* command/query can be passed in by means of http automatically
* event needs version and conflict resolver
* command can be composied by comamnds

Blocks
------
* the defmethod/extend-protocol stay different place with defmulti/defprotocol, throw no implentation.
** :use the extend-protocols/defmethod when ceating the record
** care about the order if there are two related defmulti/defprotocol




