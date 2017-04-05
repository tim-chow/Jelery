# Jelery

一个任务队列

---

# 主要API概览

* `AbstractTaskBase` 会扫描 指定的基类包及其子包下 所有使用了 `@Task` 注解并继承该类的类。   
`AbstractTaskBase` 会把这些子类当作 `task` 注册到 `taskRecords`；    
并且 `AbstractTaskBase` 为其子类提供了 `apply` 方法用于发送任务。   
* 在 `AbstractTaskBase` 扫描完并注册完 `task` 之后，可以调用它的 `run` 方法开始监听队列。

---

# 架构概览  

![arch](http://timd.cn/content/images/2017/04/Jelery.png)
