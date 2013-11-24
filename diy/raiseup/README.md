# raiseup

raiseup is a set of projects, applying the event souring and cqrs, and is built upon the clojure.

## CQRS + Event sourcing

CQRS is used to separate the read model from write model, and ES (event sourcing) is to track the events
produced and the state of domains can be replayed by reprocessing the events.

Raiseup uses a simple implementation of CQRS and ES, the events will be stored in the level db
and the command/event bus utilizes a map as a router to dispath the event/command stream.

In order to optimize the performance to read events for an aggregate root, the event ids referred to an aggregate root is stored.

domian.clj is a collection of aggregate root and entities and also produce the events.
cqrsroutes.clj is the map between the command/events with the listeners/handlers.
readmodel.clj is the read model along with the listeners to update the read model.
commands.clj is the command handlers.
control.clj is reposibile for producing commands.

The read model now uses the hazelcast as a in memory read model, the problem here is not convecience to query models.

## Convention

* the name of read model cache is single, like :user-slot rather than :user-slots


## License

Copyright © 2013 FIXME
