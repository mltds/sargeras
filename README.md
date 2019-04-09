# sargeras


## 简介

#### 什么是 sargeras ？
~~萨格拉斯（Sargeras），暴雪娱乐公司出品的系列游戏《魔兽争霸》及《魔兽世界》中的重要人物。黑暗泰坦萨格拉斯，男性莞讷泰坦，燃烧军团的缔造者兼初代首领。曾经伟大的万神殿最强大的战士，万神殿的保卫者，诸神之最髙诘者，复仇者阿格拉玛的导师。......~~  

其实是一个根据 Saga 思想实现的一个支持长事务（LLT,Long Lived Transaction）的框架，说简单点就是一个自动补单的框架。
用于解决 LLT 事务在执行过程中可能碰到某些问题，例如重启、网络中断、发生不期望的异常等，导致业务流程无法继续进行的问题。
业务系统可以是分布式的也可以不是，如果是分布式的其实也解决了分布式事物问题，分布式事物问题方案有两大类：  
    1. 与业务系统耦合的，比如 TCC、Saga、可靠性消息  
    2. 与业务系统解耦的，比如 XA、fescar 等等  
这就是基于Saga理念实现的一个框架，发现目前市场上并没有一个成熟的产品，公司也有需要所以利用业余时间初步实现了一个。  
取名为 sargeras 是因为并没有完全的按照 Saga 的思想实现，所以在命名上也做了改变，即有关系又有差异，以示区分。
~~并且祭奠下逝去的青春，魔兽世界，另一个世界~~

## 快速启动

###### 方案的选择
目前 Sargeras 有两个版本 1.x.x（master）及 0.x.x
 * 1.x.x（master）  
 基于 Spring 实现，可以单机集成到业务应用中，也可以配置为Server-Model，依赖跟自己公司相关的RPC组件。
 * 0.x.x
 这个版本是以jar包形式嵌入到业务应用系统中并直连关系型数据库（RDBMS, 例如MySQL）做管理，简单实用，方便学习研究或小公司/团队使用。

###### 1.x.x（master）快速启动
 1. 添加 Maven 依赖到系统中
    ```xml
    <dependency>
        <groupId>org.mltds.sargeras</groupId>
        <artifactId>sargeras</artifactId>
        <version>{sargeras.version}</version>
    </dependency>
    ```
 
 1. 需要一个关系型数据库，支持 JDBC 的，例如 MySQL ，执行 init.sql 脚本。
 1. 配置 Spring 相关信息， 例如以 XML 文件方式。（参见 Example 模块的 example1.xml）
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
     <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:context="http://www.springframework.org/schema/context"
            xsi:schemaLocation="
            http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
           ">
     
         <context:component-scan base-package="org.mltds.sargeras.example.example1"/>
     
         <bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
             <property name="location">
                 <value>classpath:sargeras.properties</value>
             </property>
             <property name="fileEncoding">
                 <value>UTF-8</value>
             </property>
         </bean>
     
         <bean id="sagaDataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
             <property name="url" value="${manager.rdbms.datasource.url}"/>
             <property name="username" value="${manager.rdbms.datasource.username}"/>
             <property name="password" value="${manager.rdbms.datasource.password}"/>
         </bean>
     
     
         <import resource="classpath*:sargeras/spring/saga-context.xml"/>
     
     </beans>
     ```
 1. 启动应用，并执行一个 Saga 流程。（参见 Example 模块的 Example1）
     ```java
    public void service() {
        try {
            // 业务订单ID，唯一且必须先生成。
            String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

            // 家人信息
            FamilyMember member = new FamilyMember();
            member.id = "123456789012345678";
            member.name = "小乌龟";
            member.tel = "13100000000";
            member.travelDestination = "Croatia Plitvice Lakes National Park";

            BookResult bookResult = travelService.travel(bizId, member);// 一个 Saga 流程

            logger.info(JSON.toJSONString(bookResult, true));
        } catch (Exception e) {
            logger.error("旅行计划发生异常", e);
        }
    }
    ```

## 详细介绍

#### 框架理念
首先简单介绍下 Saga 的概念: 
> Saga 是由多个且有序的事务(Transaction)组成的一个长事务（LLT,Long Lived Transaction）。  

详细请参见[SAGAS论文翻译](https://github.com/mltds/sagas-report) ~~本人英语渣渣,敬请大神完善优化~~

在使用框架前，我们要把"事务"这个概念达成共识，本文中的一个 TX （Transaction）在这里指的并不是数据库层面的事务，而是一种业务。  
假设一个场景（Example1），我有一个亲人想要出去旅游，希望我能够帮他安排下行程、定一下票。这个时候出现了一个业务"旅行（Travel）"，并且是"长途旅行（LongTrip）"类型的业务。  
我分析了她的需求，她需要我帮忙预定汽车、预定飞机、预定酒店、并且将预定的结果告诉她。当然，如果没有合适的汽车、飞机、酒店，那么整个出行计划就要取消了。此时出现了四个小的事务：
* 预定汽车（BookCar）
* 预定飞机（BookAir）
* 预定酒店（BookHotel）
* 汇总结果（Summary）

这4个小事务，都是TX（Transaction），而由这4个TX组成了一个Saga。每一次出行Saga.start()，都是一笔业务（LLT）。这种类似的场景在生活中很常见也比较简单，但是在系统实现层面上会碰到一个很经典的分布式事物问题。  
假设我预定汽车成功，之后预定飞机也成功了，但预定酒店失败了，那么我需要取消之前预定的汽车和飞机。而在这个过程中，假设这些操作都涉及到远程通讯，都有可能因为网络抖动、服务重启等情况所影响，所以需要一个"东西"能够帮助你确保在碰到各种意外情况后，还能够将所有 TX 执行完或补偿需要补偿的TX。（PS：这里的补偿Compensate是回滚/回退的意思，而非重试）。  
Saga 就是这样的一个框架，用来帮助你解决一个长链路流程的分布式事物问题，在业务系统应用层面。

#### 概念\组件介绍

* Saga  
代表一种LLT、一种业务流程，每执行一次代表一笔业务。

* SagaTx  
是 Saga 中的一个业务节点，多个 TX 组成一个Saga，当然，TX 是可以复用的。

* SagaTxStatus  
是业务节点的状态，根据 SagaTx 运行情况来设置，具体有以下几种状态
    * SUCCESS：成功，执行下一个 TX
    * PROCESSING：处理中，流程挂起，计算下一次期望重试的时间点，等待轮询重试或手动触发。
    * FAILURE：执行失败，如果一个TX状态为失败，那么从这个TX开始逆向补偿
    * COMPENSATE_SUCCESS：补偿成功
    * COMPENSATE_PROCESSING：处理中，流程挂起，计算下一次期望重试的时间点，等待轮询重试或手动触发。
    * COMPENSATE_FAILURE：补偿失败，流程终止。
    
* SagaTxProcessControl
是 Saga 流程控制的依据，理论上 SagaTx 只有三种情况
    * 成功，则继续执行
    * 处理中，抛出 SagaTxProcessingException （或其他实现了 SagaTxProcessing 的 Exception)，等待后面重试。
    * 失败，抛出 SagaTxFailureException（或其他实现了 SagaTxFailure 的 Exception），转为补偿流程或终止。
    
* SagaStatus  
是这个笔业整体状态的体现，具体有以下几种状态
    * EXECUTING：处理中，正向执行中
    * COMPENSATING：处理中，逆向补偿中
    * EXECUTE_SUCC：所有 TX 都执行成功，Saga 最终执行成功
    * COMPENSATE_SUCC：所有需要补偿的 TX 都补偿成功，Saga 最终补偿成功。
    * COMPENSATE_FAIL：某个需要补偿的 TX 补偿失败，Saga 最终失败。
    * OVERTIME：流程一直处理中直到超过了既定的 biz_timeout，最终结果未知，不再继续轮询重试。
    * INCOMPATIBLE：不兼容，暂时没有使用

* SagaApplication  
Sargeras 的应用上下文，用于获取 Saga 等

* Saga SPI
是 Sargeras 的 SPI ，共有几种类型，并提供了默认实现
    * Manager 用于管理执行过程中相关的操作，比如保存一条执行记录，修改状态等等。
    * Retry 用于轮询重试处理中的业务。
    * Serializer 用于参数/结果的序列化。
    

## 一些七七八八的

#### 实现过程中的思考

* 为什么从 0.x.x 版本改为 1.x.x ？  
 0.x.x 是为了尽量减少依赖的实现方式，用于我自己理解Saga理念，快速实现的一个版本，学习研究可以用用。对已有的业务系统入侵很大。
 1.x.x 改为 Spring + AOP + Annotation 实现，对已有的业务系统入侵小一些。

* 为什么需要 SagaContext？  
    Context 用于维护Saga执行过程中所需的信息，可以在内存中缓存信息，以及作为要操作的Manager 前的部分准备工作。

* 是否要记录SagaTx  
    记录是为了可以免去执行已经执行过的SagaTx，从上一次执行中的TX开始执行。
    如果不记录则每次重试的时候都会完全重新执行一遍，且无法执行补偿流程，补偿流程依赖执行流程记录逆向补偿。
    这样等同于一个小的重试器，或许也可以用某种方式开启这种模式，比如注解里的属性。

* 是否要持久化 SagaTx 的 Result？  
    如果不持久化Result ，那TX要再执行一次，有可能第一次执行成功，再后面重试的过程中执行结果未知（比如调用第三方系统时遇到异常），
    这个时候数据库中的TX状态为成功，是否要修改呢？所以还是持久化Result比较好，而且是强制的。

* 如果持久化了这个Result， 则TX不再执行，这个TX在执行过程中改变了某个对象里的值，执行这个TX之后，进入到下一个TX之前，应用重启了，那么这个修改后的值将丢失。怎么办？  
    感觉无解！！！！

* SagaTx重试执行（非Compensate）的时候，要注入参数么？如果不注入，再次执行时，传入的和存储的不一致怎么办？  
    理论上是应该都是一致的，为了性能考虑（需要读取+反序列化），先不注入。但这里存在一个万一，万一本次执行的入参和之前执行持久化的内容不一致怎么办？

* 如果重试执行时不注入参数，那 SagaTX  到底是默认持久化参数，还是默认不持久化？  
    持久化的参数只对补偿时有用，如果假定补偿是少数情况， 或许应该默认不持久化。或者针对补偿方法所需参数进行持久化。  

* Saga的参数要持久化吗？  
    还是要持久化的，否则重试的时候没办法开始。
    
 * Saga 补偿的方案有2个，应该用哪个？  
    1. 是逆向有序补偿，且某个Tx补偿失败了，后续不再执行。  
    2. 是不承诺补偿顺序，所有执行过的 SagaTx，都会执行补偿方法，直到得到成功或失败的结果。（Saga 超时除外）  
    目前选择的是 1 ，但没想清楚。方案 1 是 Saga 理念中的想法，Saga 理念中逆向补偿是可以转为正向执行的，但我们并不是，或许方案 2 更合适。
    
 * 与 Saga 理念不一致的地方，为什么？  
    Saga 理念是一个 LLT 要有多个保存点（Save Point），当流程执行过程中发生中断（重启、异常等原因），可以补偿回滚至上一个最近的保存点，然后重新开始执行。    
    但个人思考下来，未必适合当今的业务，或者说从我熟悉的交易/支付业务场景考虑, 进入补偿流程再重新执行会对业务系统要求很高，成本也高。  
    让所有接口支持幂等就是有一定成本了， 如果寄望于所有接口能够做到Saga所希望的："每个TX可以支持 执行-补偿-执行、补偿-执行、甚至 补偿-执行-补偿-补偿-执行 等情况的最终结果是一致的"成效是认为是非常难的。  
    所以在这里我提出了一个设想，如果进入补偿流程，那么就不会再进入执行流程，当系统中断的时候引入中间状态 "处理中（Processing）"，来挂起流程。  
    
 
#### 怀味道
 * log 直接使用了 slf4j
 * 因某些代码比较繁琐，有的地方使用一点点 DDD 的思想，整体编码风格不是很一致










