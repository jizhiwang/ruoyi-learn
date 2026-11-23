# 从零学 Spring Boot —— 以 RuoYi-Vue 3.9.2 为标准答案的复刻路线

## Context

你想从零学 Spring Boot，手上有三个仓库。先纠正一个前提：

| 目录 | 真实身份 | 在计划中的角色 |
|---|---|---|
| `E:\AI-Codex-Projects\RuoYi-Vue` | **后端**，若依 v3.9.2，266 个 Java 文件，Spring Boot 4.1.2 + JDK 17 | 学习主体 = **标准答案**，只读不改 |
| `E:\AI-Codex-Projects\RuoYi-Vue2` | **不是第二个后端**，是配套的**前端仓库**（Vue2 + Element UI，0 个 Java 文件、0 个 pom.xml）。README 第 15-16 行自述"本仓库为前端技术栈 Vue2 版本，配套后端为 RuoYi-Vue" | 后期"接口契约"教材 + 调试台 |
| `E:\AI-Codex-Projects\ruoyi-learn` | 你自己的练习工程，SB 4.1.2 / JDK 17，已有 `HelloController` + 复刻完成的 `Result` | **动手全在这里** |

所以学习方式是：**在 `ruoyi-learn` 里手写复刻若依的最小骨架**，每学一个机制就先自己实现一遍，卡住了再翻若依的对应文件。若依只读不改，保住"标准答案"。

你已确认：Java 熟练、Spring 懂一点（IoC/MVC 写过）、每天 ≤1 小时慢节奏、先后端后前端。

已核实的技术栈事实（很多网帖写错，本计划以此为准）：
- **MyBatis 4.1.0 + XML Mapper，全项目零 `baomidou` 依赖** → 不是 MyBatis-Plus
- **全项目 `@Cacheable`/`@CacheEvict`/`@CachePut` 命中 0** → 缓存全走 `RedisCache` 手工路径
- **启动类没有 `@EnableCustomConfig`/`@EnableRyAsync`** → 本版本已拆成普通 `@Configuration` 类
- Mapper 接口无 `@Mapper`，靠 `ruoyi-framework\...\config\ApplicationConfig.java` 上的 `@MapperScan`

## 总览：10 阶段，后端主线 55 天 + 选修 8 天 + 前端 5 天

慢节奏（每周五练）下 ≈ 3 个月主线 + 1 个月加餐。

| 阶段 | 主题 | 天 | 验收产出（在 ruoyi-learn 里） |
|---|---|---|---|
| 0 | 环境 | 2 | JDK17/Maven/MySQL/Redis 全绿，若依后端能跑 |
| S1 | Maven 多模块与 BOM | 5 | 拆成 4 模块，`mvn install` 全绿 |
| S2 | 配置体系 | 5 | `LearnConfig` + 多 profile + logback |
| S3 | 响应体/三层/异常/校验 | 7 | **不连库**，内存版字典 CRUD + 统一错误体 |
| S4 | MyBatis 落库 | 7 | `learn_dict_type` 真落 MySQL + 动态 SQL |
| S5 | 分页 | 5 | `startPage()/getDataTable()` 真生效 |
| S6 | Redis | 5 | `RedisCache` + `DictUtils` + `refreshCache` |
| S7 | AOP 与自定义注解 | 7 | `@LearnLog` 异步入库 + 防重 + **自调用失效实验** |
| S8 | Filter / Interceptor | 5 | XSS + 可重复读 + CORS + 静态资源，画出请求链 |
| S9 | Security + JWT | 9 | login/getInfo/logout + `@PreAuthorize("@ss")` |
| S10 | 选修加餐 | 8 | 数据权限 / Excel / Swagger / Quartz / 生成器 |
| F | 前端契约延伸 | 5 | **把 Vue 前端 baseUrl 指向你的 8081，登录成功** |

顺序调整说明：Redis 提到 Security 之前（`TokenService` 第 60 行就注入 `RedisCache`，否则只能抄）；分页独立成阶段并放在 MyBatis 之后（PageHelper 本质是 MyBatis 拦截器）；异常+校验并入 S3（不依赖数据库，且越晚补每个 CRUD 都要返工）；AOP 与 Filter/Interceptor 拆成两阶段（一个建"代理对象"模型，一个建"请求处理链"模型，混讲最容易糊）。

---

## 阶段 0：环境准备（D00-1 ~ D00-2）

**D00-1 后端工具链**
- JDK 17（`RuoYi-Vue\pom.xml` L19）。**Win 坑**：PATH 里若有 `...\Common Files\Oracle\Java\javapath`，会盖住你设的 `JAVA_HOME`，删掉或下移。
- Maven 3.9+，全局 `settings.xml` 配阿里云镜像（项目内声明的 `<repositories>` 管不到插件解析）。`mvn -v` 确认走的是 Java 17。
- IDEA：SDK=17、字节码=17、**File Encodings 全设 UTF-8**（若依注释是中文）。`RuoYi-Vue` 与 `ruoyi-learn` **各开一个窗口**，前者当只读参照。

**D00-2 MySQL + Redis**
- `E:\AI-Codex-Projects\RuoYi-Vue\sql\ry_20261117.sql` **里没有 `CREATE DATABASE`**，必须手工建，且库名要和 `application-druid.yml` 一致：`create database \`ry-vue\` default character set utf8mb4;`（带连字符，处处都要反引号）。再 source `sql\quartz.sql`。
- Redis on Windows 三选一：① WSL2 `apt install redis`（推荐）② Memurai ③ Docker `-p 6379:6379`。验证 `redis-cli ping`。
- 改 `ruoyi-admin\src\main\resources\application.yml` L10 `profile:` 为上传目录，**必须正斜杠** `D:/ruoyi/uploadPath`。
- 只跑后端时浏览器开 `http://localhost:8080` **404 是正常的**（后端无首页）。用 `curl http://localhost:8080/captchaImage` 验证活着。
- 端口排查：`netstat -ano | findstr :8080`。前端默认占 **80**，Windows 上常被 IIS/ISK 占用。

---

## S1：Maven 多模块与依赖治理（D1-D5）

靶子：`E:\AI-Codex-Projects\RuoYi-Vue\pom.xml` L12-L33 properties / **L40-L46 BOM import** / L183-L190 `<modules>` / L191 `<packaging>pom</packaging>`。依赖链 `admin → framework → system → common`。

- **D1** 只读不写。把 `ruoyi-learn\pom.xml` L7-L12 的 `<parent>` 换成若依式 `dependencyManagement` + `import spring-boot-dependencies`，`mvn dependency:tree` 验证版本仍在。**深挖点①：BOM import vs parent 继承 —— 两种机制的代价与收益**（若依不 parent 到 starter-parent，代价是插件要子模块显式声明、`java.version` 自己维护；收益是插件/仓库/编码不被 Spring 绑定）。
- **D2** 建 `learn-common` / `learn-system` / `learn-framework` / `learn-admin` 四空模块。注意**根 pom 用 BOM import、子模块用 parent 继承**，两种并存。验收 `mvn -pl learn-admin -am package`。
- **D3** 按职责分依赖：`common`←validation/utils/redis/jjwt；`system`←common；`framework`←system + **`spring-boot-starter-aspectj`** + druid；`admin`←framework + **mysql 驱动只在此模块**。Boot 4 改名点：`spring-boot-starter-webmvc`（不是 `-web`，你现有 pom L27 已踩对）、`spring-boot-starter-aspectj`（不是 `-aop`）。
- **D4** `LearnApplication` 迁入 `learn-admin`，`Result`/`HttpStatus` 落 `learn-common`，`HelloController` 落 `learn-admin`。
- **D5** 读 `RuoYiApplication.java` L12 + **L5 的 import 路径**。做破坏性实验：先不写 `exclude` 让它因缺 url 启动失败，再按若依 exclude。**深挖点②：为什么要 `exclude DataSourceAutoConfiguration`** —— 不是禁用数据源，而是让 `DruidConfig` 的 `@Primary DynamicDataSource` 全权接管。附 Boot 4 包名迁移坑（`org.springframework.boot.jdbc.autoconfigure`，网帖九成写错）。

*不复刻*：war 打包、私服、20 多个第三方版本管理项（只留 5 个）。

## S2：配置体系与 @ConfigurationProperties（D6-D10）

靶子：`ruoyi-admin\src\main\resources\application.yml`（148 行）L1-L14、L34-L38、L54-L55、L63-L70。

- **D6** 写 `learn:` 段 + `LearnConfig`，对照 `ruoyi-common\...\config\RuoYiConfig.java` L12 `@ConfigurationProperties(prefix="ruoyi")`。`/learn/config` 回显。
- **D7** **读 `RuoYiConfig.java` L25-L31 的 `private static String profile`** —— 静态字段 + setter 注入，为的是让 `getProfile()` 全局静态可调用。先写实例字段版体验到处注入的痛，再改静态版。笔记记一句"这是为便利牺牲可测性的权衡，不是 Spring 推荐"。
- **D8** 建 `application-dev.yml` / `application-prod.yml`（只差两个字段），练 profile 覆盖。**不碰多数据源**。
- **D9** 写 `logback-spring.xml`（**必须带 `-spring` 后缀才能用 `<springProfile>`**）：控制台 + 按天滚动 + dev/prod 不同 level。
- **D10** 扩写 `application.yml`：jackson 日期格式、Tomcat 线程、devtools。**保留 `server.port: 8081`**，别和若依 8080 撞。

*不复刻*：`user.password` 重试锁定（属 S9）、`referer` 防盗链（默认关）、`xss` 段（属 S8）、i18n、springdoc 分组（属 S10）。

## S3：统一响应 + 三层骨架 + 全局异常 + 参数校验（D11-D17）

**整阶段不连数据库**，用 `CopyOnWriteArrayList` + `AtomicLong` 模拟。靶子：范例 A「字典类型」—— `SysDictTypeController.java` → `ISysDictTypeService` → `SysDictTypeServiceImpl.java` → `SysDictTypeMapper.java`。

- **D11** 把你已写好的 `Result.java` 常量名对齐 `AjaxResult`（`CODE_TAG/MSG_TAG/DATA_TAG`）。你 L137-L141 的链式 `put` 正是 `extends LinkedHashMap` 的价值，笔记点明。
- **D12** 复刻 `TableDataInfo`（total/rows/code/msg 四字段）+ `BaseController` 的 `success()/error()/toAjax(int)/toAjax(boolean)`（对照 `BaseController.java` L147、L158）。
- **D13** 复刻 `BaseEntity`（含 `Map<String,Object> params`）；建 `LearnDictType extends BaseEntity`，字段抄 `SysDictType.java` L22-L35 但**先删 `@Excel`**。
- **D14** 建 4 层空壳（Controller→Service 接口→Impl→内存存储），对照 `SysDictTypeController.java` L30-L32 的三个类注解。5 个 REST 接口 Postman 全通。
- **D15** 复刻 `GlobalExceptionHandler.java`（L27 `@RestControllerAdvice`）的 5 个 handler：`ServiceException`(L58)、`HttpRequestMethodNotSupported`(L46)、`MissingPathVariable`(L69)、`TypeMismatch`(L80)、`Exception`。异常族只建 `ServiceException` + `BaseException`。**重点笔记：抛异常后 HTTP 状态仍是 200，业务码在 body 里 —— 若依的约定。**
- **D16** 补 `BindException`(L118) / `MethodArgumentNotValidException`(L129) 两个校验 handler；给实体加 `@NotBlank(message="字典名称不能为空")`，Controller 加 `@Validated`（对照 `SysDeptController.java` L78）。
- **D17** 补唯一性校验 `checkDictTypeUnique`（对应若依最近提交 `ce2072b8 校验字典数据在同类型内是否唯一`）。

*不复刻*：`@PreAuthorize`(留 S9)、`@Log`(留 S7)、`ExcelUtil` 导出(留 S10)、`getLoginUser()`(`BaseController.java` L174-L200，留 S9)。

## S4：MyBatis 接入与 CRUD 落库（D18-D24）

- **D18** 读 `ApplicationConfig.java` L16 `@MapperScan("com.ruoyi.**.mapper")`；在自己 framework 模块写同名配置。Mapper 接口**不加 `@Mapper`**。IDEA 会假告警"找不到 bean"，学会忽略。
- **D19** 读 `application.yml` L104-L111 三项 + `resources\mybatis\mybatis-config.xml`（**注意 `mapUnderscoreToCamelCase` 被注释掉了**，D20 要用）。先用**自动配置**接 MyBatis。
- **D20** 写 `LearnDictTypeMapper.xml`：先手工 resultMap 逐列映射版跑通，再删 resultMap 改 `resultType` + 打开驼峰开关，**对比两种写法**（对照 `SysDictTypeMapper.xml` L7-L16）。
- **D21** 动态 SQL：`<where>` + `<if>` + `like concat('%',#{x},'%')` + `params.beginTime/endTime` —— 这就是 `BaseEntity.params` 这个 Map 的真正用途。
- **D22** 深挖 `#{}` vs `${}`。全项目 `${}` 只有一处：`SysDeptMapper.xml` L46 `${params.dataScope}`（S10 才讲它为什么"被迫"）。写 insert 用 `useGeneratedKeys="true" keyProperty` 回填主键。**业务里绝不用 `${}`。**
- **D23** **只读不写**：读完 `MyBatisConfig.java` L40 typeAliases 通配展开 / L94 `resolveMapperLocations` / L116-L128 手工 `SqlSessionFactoryBean`，用自己的话写清"若依为什么不用自动配置"（多模块 `com.ruoyi.**.domain` 通配 + 多数据源）。回扣 D5 的 exclude。
- **D24** 只把连接池换成 Druid（**Boot 4 专用 artifact：`druid-spring-boot-4-starter`**），配 `initialSize/minIdle/maxActive`，打开 `/druid/index.html` 监控。**slave 不配，不做多数据源。**

*不复刻*：`DynamicDataSource` + `DataSourceAspect`、`DruidConfig.java` L85-L124 那个"从监控页抹掉密码明文"的 hack、MyBatis-Plus（若依根本没依赖）。

## S5：分页 PageHelper（D25-D29）

- **D25** 读 `application.yml` L113-L117，引 `pagehelper-spring-boot-starter` 4.1.1。
- **D26** 复刻 `PageDomain` + `TableSupport.java` L41 `getPageDomain()` / L52 `buildPageRequest()`。**关键认知：它从 `ServletUtils.getRequest()` 直接取参数，不是方法入参 —— 这就是 `startPage()` 能"无参"的原因。**
- **D27** 复刻 `PageUtils.java`（L13 `extends PageHelper`、L18-L26 `startPage()`、L31-L34 `clearPage()`）+ `SqlUtil.escapeOrderBySql`。**深挖点③：排序字段为什么必须走白名单** —— orderBy 最终是字符串拼进 SQL 的。传 `orderByColumn=;drop table` 必须被拦下。
- **D28** 把 `BaseController.java` L53 `startPage()` / L83 `getDataTable()`（内部 `new PageInfo(list).getTotal()`）接上；Controller 改成 `SysDictTypeController.java` L41-L43 的三行范式。验证 `total` 是数据库真实总数而非 `rows.size()`。
- **D29** **实验日**：同一 Service 方法里 `startPage()` 后连发两条查询，证明第二条不受影响；再故意制造一次 ThreadLocal 泄漏。笔记画出 `startPage → ThreadLocal → PageInterceptor 改写 SQL → 清理`。

## S6：Redis 缓存（D30-D34）

- **D30** 读 `application.yml` **L71-L93**（**Boot 3+ 路径是 `spring.data.redis`，Boot 2 的 `spring.redis` 已废弃**）。pool 参数生效需要 `commons-pool2`。
- **D31** 复刻 `RedisConfig.java` L24-L41 序列化四件套。value 先用更标准的 `GenericJackson2JsonRedisSerializer`，再看若依为何用 fastjson2（Redis 里可读 + 支持多态）。
- **D32** 复刻 `RedisCache.java`（只挑 `setCacheObject` L35/L48、`getCacheObject` L106、`deleteObject` L117/L128 五个方法）+ `CacheConstants` 四个 key 前缀。
- **D33** **事实纠偏：本版本 `@Cacheable` 全项目命中 0**，字典缓存走 `DictUtils.java` L31 `setDictCache` / L42 `getDictCache` / L193 `removeDictCache`。复刻之，并加 `@PostConstruct init()` 启动预热（对照 `SysDictTypeServiceImpl.java` L38-L42）。
- **D34** 把 `SysDictTypeController.java` L113-L120 的 `refreshCache` 接上；改一次数据观察缓存失效链路。

## S7：AOP 与自定义注解（D35-D41）

- **D35** 读 `ruoyi-common\...\annotation\` 目录（`Log`/`DataSource`/`RateLimiter`/`RepeatSubmit`/`Anonymous`/`DataScope`/`Excel`/`Sensitive`）。写 `@LearnLog` + `BusinessTypeEnum`。**核心认知：注解本身零逻辑，只是贴在代码上的元数据。**
- **D36** 复刻 `LogAspect.java` 三段通知：L59 `@Before`、L70 `@AfterReturning(returning="jsonResult")`、L82 `@AfterThrowing(throwing="e")`，绑 `@annotation(xxx)`。先用 `log.info` 代替入库。
- **D37** 建 `learn_oper_log` 真入库；对比 `@Around` 与 `@Before+@AfterReturning` 两种计时写法。验证"写日志没拖慢响应"。
- **D38** 复刻 `ThreadPoolConfig.java` L33 + `AsyncManager.java` L33 `me()`/L43 `execute` + `AsyncFactory`（对照 `LogAspect.java` L128 的异步入库调用）。关掉 Redis 确认不影响主流程。
- **D39** **本计划最高价值的一天 —— 深挖点④：AOP 自调用失效。** 靶子 `SysDeptServiceImpl.java` L60 `SpringUtils.getAopProxy(this).selectDeptList(dept)`（被调方法 L44-L49 带 `@DataScope`）。三步：① 先做破坏性实验 —— `methodA()` 内部 `this.methodB()`，B 的切面**不触发**；② 改 `AopContext.currentProxy()`，触发；③ 读 `SpringUtils.java` L122-L130 的兜底逻辑，和 `ApplicationConfig.java` L14 `@EnableAspectJAutoProxy(exposeProxy = true)` 这个前提。**结论可迁移到 `@Transactional`/`@Async`/`@Cacheable` 自调用失效全系列。**
- **D40** **只读不写**：`DataSourceAspect.java` L30 `@Pointcut` + L37 `@Around` 改 ThreadLocal 再 finally 清。回扣 D24 —— "ThreadLocal 开关 + `AbstractRoutingDataSource` 路由"就是 `@DS` 的实现。
- **D41** 实现 `@RepeatSubmit` 切面（key = `ip+url+参数hash`，`setIfAbsent`+TTL）。可选挑战 `RateLimiterAspect.java` L49 + `RedisConfig.java` L43 `limitScript()`，理解 **Lua 存在的理由是 get+incr+expire 必须原子**。

*不复刻*：`DataScopeAspect.java`（留 S10 专读）、`@Excel`/`@Sensitive` 的序列化侧。

## S8：Filter / Interceptor / WebMvcConfigurer（D42-D46）

- **D42** 复刻 `XssFilter.java` L22 + `XssHttpServletRequestWrapper`（只覆盖 `getParameter*`）；注册见 `FilterConfig.java` L35-L44（`FilterRegistrationBean` + **L44 `setOrder(HIGHEST_PRECEDENCE)`**）。POST 一段 `<script>` 验证被洗。
- **D43** 复刻 `RepeatableFilter` + `RepeatedlyRequestWrapper`。**先弄懂为什么需要它**：body 输入流只能读一次，而 `@RequestBody` 和切面都想读。
- **D44** 复刻 `RepeatSubmitInterceptor.java` L20（**抽象类** + `handler instanceof HandlerMethod`）+ `impl\SameUrlDataInterceptor.java`，注册在 `ResourcesConfig.java` L46-L48。**把 D41 用切面做的防重迁到拦截器**，体会差别。这是模板方法模式。
- **D45** `ResourcesConfig.java` L30-L37 静态资源映射 `/profile/**` → `LearnConfig.getProfile()`（回扣 S2 的 D6-D7）+ 一个上传接口 + L54 `corsFilter()`。
- **D46** 复刻 `common\xss\Xss.java` + `XssValidator.java`（`ConstraintValidator` 路线），和 D42 的 Filter 路线做**同一目标两种手段**对比。

> **本阶段必做一张图（深挖点⑤）**：`Filter（容器级，最早，无 HandlerMethod）→ DispatcherServlet → Interceptor.preHandle（能拿 handler 和注解）→ AOP 环绕 Controller（只能拿方法参数）→ @Validated → Controller → 逆向出栈`。并回答：若依为什么 XSS 用 **Filter**（要包住 request 让后续所有读取都干净）、防重用 **Interceptor**（要看方法上的 `@RepeatSubmit`）、操作日志用 **AOP**（要拿返回值和异常）。

## S9：Spring Security + JWT（D47-D55，最长阶段）

- **D47** 只加 `spring-boot-starter-security` 依赖，什么都不配 → 观察"全部 401 + 控制台随机密码"，建立"默认全拦"直觉。
- **D48** 复刻 `SecurityConfig.java` L85-L118 的 `SecurityFilterChain`：L90 `csrf.disable()`、**L98 `STATELESS`**、L100-L108 白名单、L113 `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`。注意 **L27 是 `@EnableMethodSecurity`，`@EnableGlobalMethodSecurity` 在 Security 6 已删**。
- **D49** 复刻 `AuthenticationEntryPointImpl`，把 401 也变成 `{code:401,msg:...}`（**HTTP 状态与业务码解耦**）。建 `learn_user`（从 `sys_user` 裁字段），L124 `BCryptPasswordEncoder` 生成哈希。**实验：同一明文每次加密结果不同。**
- **D50** 复刻 `UserDetailsServiceImpl.loadUserByUsername` + `LoginUser`（实现 `UserDetails`，权限先塞 `*:*:*`）。
- **D51** 复刻 `SysLoginService.java`：L68 `loginPreCheck` → `authenticationManager.authenticate(...)` → L99 `createToken`。跳过验证码（L110 `validateCaptcha` 用假开关）。Postman 拿到 token。
- **D52** 复刻 `TokenService.java`：L43-L51 读 `application.yml` L95-L102 `token:` 段、**L119 `createToken`**、L213 `parseToken`、L240 `getToken(request)`。**结构重点：JWT 里只放一个 uuid，真正的 `LoginUser` 在 Redis。** 把 token 粘到 jwt.io 自证。
- **D53** 复刻 `JwtAuthenticationTokenFilter.java` L25 `extends OncePerRequestFilter`，L34-L40 写 `SecurityContextHolder`。**验收：手动删掉 Redis 里那个 key → 请求立刻 401，这就是"JWT 无状态却能被服务端踢下线"的答案。**
- **D54** 复刻 `PermissionService.java` 并**把 bean 命名成 `ss`**；建 `learn_role`/`learn_menu`/`learn_role_menu`；给 Controller 加 `@PreAuthorize("@ss.hasPermi('learn:dict:list')")`。无权限 → 403，由 `GlobalExceptionHandler` L35 接住。**深挖点⑤补充：`@ss` 就是 SpEL 里的 bean 引用。**
- **D55** 复刻 L138 `verifyToken`（距过期 <20 分钟自动续期，L153）+ `PermitAllUrlProperties.java` L41-L53 **从 `RequestMappingHandlerMapping` 反扫 `@Anonymous` 生成白名单**。**这是"用代码反向配置 Security"的好教材**：白名单不是硬编码。

*不复刻*：密码重试锁定、kaptcha 验证码、Remember-Me、OAuth2。

## S10：选修加餐（D56-D63，每项独立可停）

按性价比排序：
1. **D56-D57 数据权限**（把 S7 欠的还上）：精读 `DataScopeAspect.java` L35/L67 五种 `DATA_SCOPE_*` 分支/**L149 "拼 SQL 前先清空 `params.dataScope` 防注入"** → 复刻只支持"本部门"和"本部门及子"的最小版 → 亲眼看到 `SysDeptMapper.xml` L46 `${params.dataScope}` 被填进去。**全计划最精彩的防御式设计。**
2. **D58 Excel 导出**：`SysDictTypeController.java` L46-L54 + `@Excel(readConverterExp="0=正常,1=停用")`。只读 `ExcelUtil.java` 的注解驱动反射骨架，**别通读上千行**。
3. **D59 springdoc**：`application.yml` L119-L131 —— **`packages-to-scan` 只扫 `com.ruoyi.web.controller.tool`，所以默认 Swagger 里看不到 system 接口**，这是常见困惑点。
4. **D60-D61 Quartz**：`ScheduleConfig.java`（独立 `qrtz_` 表）+ `JobInvokeUtil.java` —— **用反射按字符串调 bean 方法，是"配置驱动行为"的典型，也是 RCE 风险面**。
5. **D62-D63 代码生成器（只读）**：`VelocityUtils.java` + `resources\vm\**`。**把它生成的代码和你 S3-S9 手写的逐行对照 —— 如果里面有看不懂的行，说明前面某阶段没吃透。这是自检的最好方式。**

---

## F：前端契约延伸（F1-F5，主线走完后）

目标不是学 Vue，是**理解接口契约**。

- **F1** `npm install && npm run dev` 跑 `RuoYi-Vue2`。改端口/proxy 看 `vue.config.js` L12 `baseUrl`、L14（默认 **80**，Win 上常被占）。Node 版本无压力：用的是 dart-sass 不是 node-sass。连若依后端 8080，`admin/admin123` 登录成功。
- **F2** 抓一条"部门管理"请求，把 URL/Header/Response **逐字段手写映射**到后端类与行号：`src\api\system\dept.js` 6 个函数 ↔ `SysDeptController.java` L41-L95 6 个方法。搞清 `/dev-api/xxx` 经 pathRewrite 变成 `http://localhost:8080/xxx`。
- **F3** `src\utils\request.js` L31-L32 自动加 `Authorization: Bearer`；L41-L60 **前端也在做 repeatSubmit 判断**，和后端 `SameUrlDataInterceptor` 是双保险。抓包证明 `pageNum/pageSize/orderByColumn/isAsc` 正好对上 `TableSupport.java` L16-L36 五个常量名 —— **改一个字后端就静默不分页**。
- **F4** 权限闭环：后端 `SysLoginController.getRouters` → `SysMenuServiceImpl.buildMenus`（产出 `RouterVo`）→ 前端 `store\modules\permission.js` L56 `filterAsyncRouter` / L70/L113 `loadView(字符串→组件)`。把 `sys_menu` 里一条 `component` 改脏，观察路由生成失败。**并说出结论：`v-hasPermi` 只是把按钮 DOM 删掉，`@ss.hasPermi` 才是真防线。**
- **F5 终极验收**：把 `vue.config.js` L12 `baseUrl` 改成 **`http://localhost:8081`**，让你的 `ruoyi-learn` 提供前端必需的 `POST /login`、`GET /getInfo`、`GET /getRouters`、`GET /logout` + 字典分页接口。只求**登录 + 菜单渲染出来 + 字典列表能翻页**。

> F5 一定会失败好几次，这正是要的。失败点集中在四类：字段名对不上、`code` 语义不对、`data.rows`/`data.total` 结构不对、`getRouters` 少了 `children/meta`。比读十遍源码有效。

---

## 学习方法

**每阶段自检三动作**（各 5-10 分钟）
1. **闭卷重写**：次日不看任何代码从零写出该阶段核心类（S5 就是 `PageUtils.startPage()`）。写不出 = 没过，回补一天。
2. **一句话讲清**这阶段解决了什么问题。参考：S1="让多个 jar 共享版本而不打架"；S5="让分页不污染 Service 方法签名"；S7="把每个方法前后重复的代码搬进代理"。
3. **删代码实验**：故意删一行关键配置（`exposeProxy=true`、`@MapperScan`、`redis.host`），记录报错原文，积累"报错→病因"对照表。

**笔记**：放 `ruoyi-learn\docs\`，一阶段一个 md，固定五段模板 —— *我原本以为 / 若依实际做法（文件:行号）/ 为什么这样设计 / 我踩的坑（含报错原文）/ 一句话总结*。另维护 `myths.md` 记"网帖 vs 本版本真相"（已确认 4 条：MyBatis-Plus 无、`@Cacheable` 无、`@EnableCustomConfig` 无、`DataSourceAutoConfiguration` 包路径已迁移）。**不要抄若依代码进笔记，只记路径+行号，抄了就停止思考。**

**git 分段**（`ruoyi-learn` 现为 2 commits、0 tags、有 3 个未提交改动）
- 进 D1 前先提交一个 `baseline: hello + Result 复刻`，作为所有后续 diff 的起点。
- 每阶段一分支 + 一 tag：`git checkout -b stage/01-maven` → `git tag -a s01-maven -m "验收：mvn install 全绿"` → 回 main 开下一阶段（各阶段独立，方便重做）。
- 每日粒度提交，消息如 `s05-d2: TableSupport 复刻完成，未接 orderBy 白名单`。
- S10 后跑 `git diff s01-maven..s09-security --stat`，用行数量化"我写了多少"。

**止损**：卡住超过 25 分钟就跳过，记进 `docs/blocked.md` 继续往后 —— 很多困惑会在后续自然解开（D23 读 `MyBatisConfig` 的疑问，到 D47 配 Security 时会澄清一半）。每完成 3 阶段设一个"复习日"，不写新代码，只重读 `myths.md` + 闭卷重写一个最没底的类。

## Critical Files

- `E:\AI-Codex-Projects\RuoYi-Vue\pom.xml` — L19/L40-L46/L183-L191，S1 全部依据
- `E:\AI-Codex-Projects\RuoYi-Vue\ruoyi-admin\src\main\resources\application.yml` — 148 行覆盖 S2/S5/S6/S9 所有配置段
- `E:\AI-Codex-Projects\RuoYi-Vue\ruoyi-framework\src\main\java\com\ruoyi\framework\config\ApplicationConfig.java` — L14 `exposeProxy` + L16 `@MapperScan`，S4 与 S7-D39 的枢纽
- `E:\AI-Codex-Projects\RuoYi-Vue\ruoyi-system\src\main\java\com\ruoyi\system\service\impl\SysDeptServiceImpl.java` — L60 `getAopProxy(this)`，AOP 自调用失效教材
- `E:\AI-Codex-Projects\RuoYi-Vue\ruoyi-framework\src\main\java\com\ruoyi\framework\config\SecurityConfig.java` — S9 骨架
- `E:\AI-Codex-Projects\RuoYi-Vue\ruoyi-admin\src\main\java\com\ruoyi\web\controller\system\SysDictTypeController.java` — 范例 A，贯穿 S3/S5/S6/S9 的复刻靶子
- `E:\AI-Codex-Projects\RuoYi-Vue2\vue.config.js` — L12 `baseUrl`，F1/F5 唯一改动点