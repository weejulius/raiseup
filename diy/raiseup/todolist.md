 
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
