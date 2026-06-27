# 学生课堂随机点名系统

## 项目简介

本项目是《面向对象程序设计（Java）》课程设计，实现了一个基于 Java 的课堂随机点名系统。

系统采用 Java 面向对象思想开发，使用 Java 自带的 HttpServer 提供简单的 Web 页面，实现课堂随机点名、学生回答统计以及数据持久化等功能。

## 功能

- 随机抽取学生
- 统计点名次数和回答正确次数
- 自动计算答对率
- 公平随机点名
- 连续答错后切换点名策略
- 本地文件保存学生数据

## 项目结构

```text
src/
├── model/
│   └── Student.java
├── service/
│   └── RollCallService.java
├── web/
│   └── WebServer.java
└── students.txt
```

## 运行方式

1. 使用 IntelliJ IDEA 打开项目。
2. 运行 `WebServer.java`。
3. 浏览器访问：

http://localhost:8080

> 注意：程序运行期间才能访问网页，关闭 `WebServer` 后网页将无法访问。

## 开发环境

- JDK 8+
- IntelliJ IDEA
