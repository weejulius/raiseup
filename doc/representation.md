# Drinkup #1 

##Clojure开发流程

---


## ACUG (Alibaba Clojure User Group)

余乐 (行业-家居小商品)

---

# clojure特点

*  动态类型
*  不可变数据结构
*  Lisp风格
*  基于JVM
*  还有什么?

---

# rapid development

*  基于REPL (Read Eval Print Loop)
*  Lein
*  IDE (emacs/vim/eclipse/intellij ...)

---

#  例子

## Raiseup

https://github.com/weejulius/raiseup

*  Host on Open shift (PAAS)
*  目的是实践clojure,cqrs,以及event sourcing
*  一个简单的时间管理,类似http://todomvc.com/architecture-examples/angularjs/

以emacs做演示

---

# 项目结构

基于lein,和maven项目结构大致一样,略有区别

参考:　tree -d -L 2

---

#  Lein的使用

*  安装　https://github.com/technomancy/leiningen?source=c
*  基本使用
  *  lein new 新建一个项目
  *  lein test
  *  lein repl
  *  lein run
*  配置
  * profile.clj
  * project.clj 项目的配置 例子 https://github.com/technomancy/leiningen/blob/stable/sample.project.clj

---



# 常用库介绍

---


# 测试

*  clojure.test
*  midje (目前的使用)
*  speclj (偏向bdd风格)


---

#  运行

## 执行方式一般有两种,自动和手动

* 自动方式 : lein midje :autotest
* 手动方式 : 手动加载代码到repl 
https://github.com/marick/Midje/wiki/Repl-tools
https://github.com/marick/Midje/wiki/What%27s-new-in-midje-1.6


---

#  调试

*  print
  *  https://github.com/AlexBaranosky/print-foo
*  debugger
  *  尝试ritz

---

#  部署

*  lein uberjar
*  lein deploy clojars

---

* https://github.com/organizations/acug
* https://github.com/weejulius


---

# 谢谢!!!

---




