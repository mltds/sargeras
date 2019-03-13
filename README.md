# sargeras


## 简介

#### 什么是 sargeras ？
~~萨格拉斯（Sargeras），暴雪娱乐公司出品的系列游戏《魔兽争霸》及《魔兽世界》中的重要人物。黑暗泰坦萨格拉斯，男性莞讷泰坦，燃烧军团的缔造者兼初代首领。曾经伟大的万神殿最强大的战士，万神殿的保卫者，诸神之最髙诘者，复仇者阿格拉玛的导师。......~~  

其实是一个根据 Saga 思想实现的一个分布式事物框架，取名为 sargeras 是因为并没有完全的按照 Saga 的思想实现，所以在命名上也做了改变，即有关系又有差异，以示区分。

## 快速启动

###### 假设依赖默认实现
 1. 添加 Maven 依赖到系统中
    ```xml
    <dependency>
        <groupId>org.mltds.sargeras</groupId>
        <artifactId>sargeras</artifactId>
        <version>0.1.1-SNAPSHOT</version>
    </dependency>
    ```
 
 1. 需要一个关系型数据库，支持 JDBC 的，例如 MySQL ，执行 init.sql 脚本。
 1. 配置文件一枚，命名为：sargeras.properties ，放在 classpath 目录下。（参见test模块的配置）
    > repository.rdbms.datasource.url=jdbc:mysql://mydb.com:3306/sargeras  
      repository.rdbms.datasource.username=sunyi  
      repository.rdbms.datasource.password=sunyi
 1. 使用 SagaBuilder 配置一个 Saga 流程，使用 SagaLauncher 启动 Saga 框架。（参见 test 模块的 Example1）
     ```java
    SagaBuilder.newBuilder(appName, bizName)// 定义一个业务
            .addTx(new BookCar())// 订汽车
            .addTx(new BookAir())// 订机票
            .addTx(new BookHotel(true))// 订酒店，false为强制失败
            .addTx(new NotifyFamily()) // 告诉家人
            .addListener(new LogListener()) // 增加一些log输出方便跟踪
            .build();
    
    SagaLauncher.launch(); // 需要先 Build Saga
    ```
 1. 使用 Saga.start() 启动  一个Saga流程，传入对应的 BizId 和 BizParam。（参见 test 模块的 Example1）
 
    ```java
    // 业务订单ID，唯一且必须先由业务系统生成并落库，这里举个栗子。
    String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

    // 家人信息
    FamilyMember member = new FamilyMember();
    member.id = "123456789012345678";
    member.name = "小乌龟";
    member.tel = "13100000000";
    member.travelDestination = "Croatia Plitvice Lakes National Park";

    // 获取业务流程（模板）
    Saga saga = SagaApplication.getSaga(appName, bizName); // 任何地方都可以获取到这个Saga

    // 执行业务
    SagaResult result = saga.start(bizId, member);

    SagaStatus status = result.getStatus(); // 获取任务执行状态
    Object bizResult = result.getBizResult(Object.class); // 获取任务执行结果

    ```
 
 


## 详细介绍


## 其他

#### 没想清楚的地方
 * Saga的核心，应该是嵌入在业务系统里还是一个独立系统？  
    目前SNAPSHOT版本是嵌入在业务系统里，比较简单、稳定、实用。但弊端也很明显，不容易升级维护，不方便做监控，对业务系统有入侵。
 * Listener 是否应该有多个？  
    如果只允许有一个Listener，那么 onError 方法应当返回状态，现在默认为处理中
 * 当某个 TX execute 返回 EXE_FAIL_TO_COMP 时，这个 TX 是否需要补偿？  
    现在是补偿的，选择补偿的原因是考虑到，虽然执行失败了，但可能还是有需要消除一些影响。
 * 轮询重试次数、间隔频率该如何控制？  
    如果下游业务宕机了，可能很多重试是无用的，可以定义轮询间隔，比如 {1,5,10,20.30,60} 依次作为轮询间隔
 * 如果因为发布，导致代码里的TX变化了，可能无法向下兼容，怎么办？  
    现在的考虑是向下兼容要业务系统考虑，如果某个流程在重试的时候，发现完全不能进行下去，比如找不到重试起始点的TX则尽量报错。
    
 
 
#### 怀味道
 * log 直接使用了 slf4j
 * 锁的实现依赖 Repository，感觉不是很合理。
 * 因某些代码比较繁琐，有的地方使用一点点 DDD 的思想，整体编码风格不是很一致
 * 现在必须先 Build Saga ，才能启动Application，感觉顺序有些奇怪
 * 都是硬编码的风格，或许可以支持注解式变成

#### 版本记录

###### 0.1.1-SNAPSHOT
* 支持轮询重试
* 支持轮询重试间隔配置
* 支持轮询重试工作线程数配置

###### 0.1.0-SNAPSHOT
* 支持一个Saga正向执行、逆向回滚
* 完成基本的配置、存储、序列化、管理、异常等组件
* 定义基本的 API，支持配置文件指定组件的实现 












