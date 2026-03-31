## 项目结构说明

---

```
src
└── main
    ├── java
    │   └── org
    │       └── hit
    │           └── monitor
    │               ├── bo          查询类
    │               ├── common      公共类、定义全局配置、数据返回等
    │               ├── controller  控制器
    │               ├── dao         持久化层
    │               ├── interceptor 拦截器
    │               ├── model       数据对象
    │               ├── service     服务层接口
    │               │   └── impl    服务接口的实现
    │               ├── timetask    定时任务
    │               ├── utils       工具类
    │               └── vo          视图类
    │
    ├── resources                   系统所有的配置文件
    │   ├── context                 非Web相关Spring Bean的定义
    │   ├── dbconfig                数据库配置
    │   └── mybatis-mappers         MyBatis
    │
    │                               // 以上为服务端内容
    └── webapp                      // 以下为前端内容
        ├── asset
        │   ├── common              公共前端文件，包含网站主题、顶栏、侧边栏、公共CSS、JS、IMG
        │   ├── module              各个具体模块的前端内容
        │   │   ├── datanode        DataNode
        │   │   ├── history         历史数据分析
        │   │   ├── index           首页
        │   │   ├── namenode        NameNode
        │   │   └── node            节点状态
        │   └── plugins             项目中所依赖的前端插件
        │       ├── bootstrap
        │       ├── datatables
        │       ├── daterangepicker
        │       ├── datetime
        │       ├── echarts
        │       ├── fastclick
        │       ├── jQuery
        │       ├── other
        │       ├── pace
        │       └── slimScroll
        └── doc                     项目文档
```

## 开发说明

---

项目使用 `Maven` 构建，具体依赖信息定义在 `pom.xml` 文件中，主要的构成框架为 `Spring` 和 `MyBatis`

***目前整个系统主要依赖以下服务：***

- `Ganglia Gmond`  - 集群的每一台主机都安装了 `Gmond` ，用于收集系统本身、HDFS、Yarn、MapReduce的指标
- `Ganglia Gmetad` - 集群的主节点上安装，用于汇总所有子节点发来的指标数据
- `MySQL` - 存储所有的指标信息（聚合信息，非单独节点信息）及平台持久化的内容
- `Resource Manager API` - 用于获得节点信息、任务信息等
- `Job History Server API` - 用于历史任务信息，是参数自动调优的数据来源
- `Ganglia API` - Ganglia的python插件，用于提供REST API，本系统依赖其获取某一节点的指标信息

***注意:*** 
 - `Ganglia Gmetad` 的默认行为是将指标数据以RRD文件的形式进行持久化
 - 本系统对所采用的 `Gemtad` 进行了修改，使其能够将指标数据写入配置文件所指定的MySQL数据库
 - 修改过程及源码请查看 `Ganglia Gmetad` 分支项目

***关于MySQL指标数据的存储：***

- 考虑到系统可能需要保存两个星期的实时数据，每个指标都会累积大量的数据，为了减少单表的压力
- 系统为每一个指标都以 `m_指标名` 的方式从创建了一张表，而所有表的定义都可以在表 `metrics_define` 中找到
- 表创建以及指标定义信息添加的过程定义在 `MySQL` 的存储过程中，存储过程名为 `push_metrics`
- `Gmeted` 在写入指标时会调用此存储过程
- `timetask` 目录下的定时任务会定时清理数据中过期的指标数据

***源码阅读说明***

- 由于系统基于Spring MVC，因此所有的后端代码的入口皆为 `controller` 包下的控制器类
- 进行源码修改时，只需从页面中找到其数据来源 `Controller` ，然后按照`Controller` > `Service` > `DAO` 的顺序即可

