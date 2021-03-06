
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

## auth 计划

- 用hazelcast替代leveldb做kv存储
- [DONE]引用vert.x作用消息bus
- ar及快照存储可配置化
- [DONE]使用clojure写shell启动依赖,生命周期直接启动shell


- note编辑时行高调高
- 支持上传图片
- [DONE] 自动保存
- SEO
- 评论
- 用户登陆
  1. [DONE] 用户名及密码长度，合法字符验证
  2. [DONE] 密码加密
  3. [DONE] 权限验证
  4. [DONE] 登陆/注册输入样式调优
  5. [DONE] send command 异常处理
  6. 剔除重复代码
  7. [DONE] note显示作者
  8. [DONE] 现在当前登陆用户
  9. [DONE] 登出

- 标签
- [DONE] 编辑时可以preview
- 访问量
- [DONE] 用户个人首页
  1. 左边菜单


## 问题

* note-his should be removed
* [DONE] 注册成功应该更新session
* identity为什么不在req？
* notes应该是倒序排
* 个人页面不需要权限验证


## refactor to be functional

## 即将要做

* use hazelcast along with leveldb
* command style
* everything is data
* note version history appears in note

## cmd-ui

* 回退功能
* 直接uri打开
* 现在功能移植


## simpify the component

--
reduct the complexility

* replace elastic search: [Delay] no appropriate replacement
* replace vert.x







