# Raiseup

Raiseup is to be a full stack of web development using clojure and clojurescript, it is not a framework or library, it is just a lab project wants to practise functional,cqrs,reative and so on. the demo is shown [here](http://red-raiseup.rhcloud.com/notes).

# Stack

Raiseup is inspired by CQRS ,event sourcing and clojure, thus the command is separated from the queries, everything is simpe as it should be, data is the core. it includes the following parts.

[archtecture image](https://www.dropbox.com/s/i91t6zra3k8h5jq/raiseup-arch.jpg)

##CQRS

- Command    : a map has the data order the system to do something
- Handler    : functions to handle commands or events, the commands/events are distrubted by the bus.
- Read Model : entries to be queried
- Query      : a map has the data tell the query engine what will be retreived from the read model. 
- Domain     : functions apply to the domain and create the final event

##WEB
- Control : function to handle the actions or requests.
- View    : the UI user will face.

It has several components, is inspired by the Reload flow work, the components have side effect but are self-managed. Now including:

- Http Server component (imp by httpkit)
- ReadModel component (imp by elastic search)
- Bus component (impl by vert.x)
- Storage component (impl by leveldb)
- Logging component (impl by timbre)
- Recoverable Id component (impl by leveldb)

# Functional programming

## Why function programming

* HOF (high order function)
* Composition f.g
* Partial
* Pattern match

## License

Copyright © 2014 FIXME
