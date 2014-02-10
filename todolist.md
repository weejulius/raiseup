 
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
* add performance evaluation for the app

Blocks
------
* the defmethod/extend-protocol stay different place with defmulti/defprotocol, throw no implentation.
* :use the extend-protocols/defmethod when ceating the record
* care about the order if there are two related defmulti/defprotocol



CQRS实现回顾
-----

2014-01-29

## 目前实现

- 用core.async实现event bus和command bus，无法处理1000+消息同时
- 将event和最近快照写入文件系统
- 将readmodel写入elasticsearch,读写速度慢相比hazelcast，但后者不能全文搜索

## 计划

- 用hazelcast替代leveldb做kv存储
- [DONE]引用vert.x作用消息bus
- ar及快照存储可配置化
- [DONE]使用clojure写shell启动依赖,生命周期直接启动shell


- note编辑时行高调高
- 支持上传图片
- 自动保存
- SEO

## 完成NOTE



#为什么Clojure

## FP

当初是想学习scala，发现用scala和用java差不多，无法对fp有更深的了解，因此想换个角度， 其实第一眼看到clojure觉得它是个怪胎，当时也不知道什么是LISP，一切皆是缘。

## Lisp

数据是代码，代码是数据， 强大，可是有很多的括号，不过现在编辑器基本能很方便处理这些括号，目前对于我也不是大问题。
Macro最佳实践就是不用，最近深有体会，滥用的话代码可读性会降低。

## REPL

基本所有的语言都有REPL，但将REPL用的如此飘逸，目前只发现Clojure。 REPL可以快速验证自己的代码。

## 数据结构的语法

```
{:a 1  :b 2}
```

```
[a b c]
```

这些数据结构的定义方便直接。

## 动态

动态是双刃剑， 可以让代码很灵活，但是也经常让人陷入类型错误的泥潭中。 不过目前typed.clojure,schema这些都能解决一部分问题，
这个问题将来可能不是大问题，包括让人诟病的错误异常堆栈，社区已经有项目在改进这些。

## 性能

相比scala，没有优化前，估计整体性能是scala的1/3. 对于一般的应用，还是可以接受的。

## 社区

社区还是很活跃 的，从witter和github上就能看出来。库也是比较丰富，如果没有直接用java库或者将其封装一下。

## 开发工具

emacs用的应该是最多，其次是vim,然后lightable，sublime之类的，我是用cursive，基本能满足我的需求，可惜不开源。

## 其它

比如 multi-method,protocol,stm,core.asyn,clojurescript都是很实用的工具或特性。

