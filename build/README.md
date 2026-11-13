# build/ — 构建 Walnut 测试服务（dist/wn4j）

> 这份文件是写给**人**的：照着做，一条命令就能得到一份可以直接启动的 Walnut。
> 机制细节见 `wn-doc/public/reference/102.auto-test/10-at-build.md`；这里只讲"怎么做"。

## 一、这是什么

`build/` 是构建的全部家当。跑 `build/wn4j-make.py`，它会：

```text
mvn package -DskipTests dependency:copy-dependencies   # 编译 + 拷依赖
  → 按 build/ 里的模板生成 conf/sbin/ROOT
  → 输出到 dist/wn4j/
  → 逐项校验产物布局（缺件即非零退出）
```

产物 `dist/wn4j/` 是**程序目录**：删了重建不心疼。持久化数据（Mongo 库、Redis RDB、桶文件、日志）全在**仓库的 `.at/`** 下，不在 dist 里。

## 二、前置条件

| 项 | 说明 |
|---|---|
| **JDK** | 17+（`java -version` 能跑；`JAVA_HOME` 可选，`make.json` 里也能指定） |
| **Maven** | `mvn -v` 能跑；首次构建需要网络拉依赖，之后可离线（`mvn -o`） |
| **`build/build.rs.json`** | **必需**，缺失直接报错。里面有 Mongo/Redis/桶目录的指向 |
| **Docker（可选）** | 只有 `make.json` 的 `REDIS_SERVER` 填了 `Docker:…` 才需要 |
| **磁盘** | dist 约几百 MB（依赖 jar 占大头） |

`build.rs.json` 里这几项**必须与本机其它 Walnut 实例不同**，否则两个实例共库会报 `LostBucket`：

```jsonc
{
  "mongo": { "db": "walnut_wn4j" },      // 别用开发者日常实例的库
  "redis": { "port": 6390, "db": 9 },    // 端口与 db 都要错开
  "buck": "auto",                        // auto = 仓库 .at/walnut/buck
  "swap": "auto"
}
```

## 三、一条命令构建

在 **Walnut 仓库根**执行：

```powershell
python build/wn4j-make.py            # 默认：编译 + 打包 + 校验
```

看到最后输出产物校验通过（`ok`）就可以信任这个目录，不需要人工检查每个子目录。

### 只做某一层

```powershell
python build/wn4j-make.py --mvn      # 只编译（改了 Java 代码，先单独确认能编过）
python build/wn4j-make.py --dist     # 只重新打包（依赖已编译好的 jar；改 conf 模板时用）
python build/wn4j-make.py --clear    # 删掉 dist/ 与 target/（从零开始）
python build/wn4j-make.py --config build/make.json   # 指定配置文件（默认就是它）
```

> 不要用 `--docker` / `--upload` / `--push`：那是给发布镜像的人用的，日常测试用不到。

## 四、产物长什么样

```text
dist/wn4j/
|-- deps/                    # 依赖 jar（mvn copy-dependencies 的产物）
|-- conf/
|   |-- web_local.properties # 覆盖层配置（PASSWD_ONLY、端口、mongo/redis、桶目录）
|   `-- log4j2.xml           # 日志：@LOG_DIR@ 已指到 <Walnut>/.at/walnut/log/
|-- ROOT/
|   |-- app/wn-term/         # 内置终端应用（挂到 /app/wn-term）
|   `-- etc/hosts.d/default/ # 默认登录页（挂到 /etc/hosts.d/default/）
|-- core/
|   |-- conf/                # 内核配置（web.properties、mime、hostmap…）
|   `-- walnut.jar           # 编译产物
|-- sbin/
|   |-- start.cmd            # 启动（Windows）
|   |-- stop.cmd             # 停止（Windows）
|   |-- start_with_redis.cmd # 可选：先拉私有 redis 再启动（REDIS_SERVER 非空才生成）
|   `-- stop_with_redis.cmd  # 可选：先停 Walnut 再关私有 redis
`-- run/                     # 运行锁：pid-<port>.json、redis-<port>.json
```

数据家（不在 dist 里）：

```text
<Walnut>/.at/
|-- walnut/{buck,swap,log}/  # 桶文件、交换区、日志
`-- redis/data/             # 私有 redis 的 RDB
```

## 五、启动与停止（2 级实例）

```powershell
# 启动：指定端口（缺省由 conf 的 app-port 决定，模板值是 7709）
dist\wn4j\sbin\start.cmd

# 或者用带私有 redis 的版本（产物里有这个脚本时）
dist\wn4j\sbin\start_with_redis.cmd

# 停止
dist\wn4j\sbin\stop.cmd
dist\wn4j\sbin\stop_with_redis.cmd
```

- 端口：`build/make.json` 的 **`APP_PORT`**（缺省 `7709`，管理口自动 `7710`）；
  故意避开 8080——那是 IDE 里 1 级实例的常用端口。
- 启动**会校验成败**：轮询端口约 40 秒，JVM 中途退出（如端口被占）或超时未就绪，
  会打印日志尾部、杀残留进程、不写运行锁、非零退出。
- 启动脚本接受 `--conf <目录>` 与 `--pid-dir <目录>`，给级别 3 的私有实例用；
  日常 2 级实例不用传。

### 私有 Redis 的两种形态（在哪配）

分两层，别混：

**构建期**（决定用哪种方式拉起 redis）——`build/make.json` 的 `REDIS_SERVER`：

```jsonc
"D:/opt/redis/redis-server.exe"                                        // A. 本机 exe
"Docker:redis<sha256:bdff4838…>=wn4j-redis-L2"                          // B. Docker（当前）
""                                                                      // 留空：不生成 with_redis 脚本
```

构建脚本把它烘进产物（`sbin/start_with_redis.cmd` 里的 `RD_SERVER` /
`RD_DOCKER_IMAGE` / `RD_DOCKER_IMAGE_ID` / `RD_DOCKER_CONTAINER`），
脚本运行时按「有没有 docker id」二选一。改了要重新 `--dist`。

**运行期**（决定连哪个端口/库/密码、RDB 放哪）——生效的 `web_local.properties`：

| 键 | 含义 |
|---|---|
| `redis-port` | 私有 redis 端口（当前 6390，别和开发者实例的 6379 撞） |
| `redis-database` | 库号（当前 9） |
| `redis-password` | 密码（空 = 无密码） |

改了不用重建，重启实例即可生效。RDB 目录：级别 2 = `<Walnut>\.at\redis\data`；
级别 3 = `<conf>\..\..\redis\data`。

## 六、常见问题

**构建报 `mvn` 失败**
先在仓库根手动跑 `mvn -o -q compile`：能过说明是依赖下载问题（联网重试一次即可），
不能过就是代码没编过，先解决编译错误。

**构建报 `AccessDeniedException: …\mvn\cache\…`**
运行账户没有 Maven 本地仓库的写权限（受限账户、沙箱、服务账户都会这样）。
Maven 即使不下载也要写 `resolver-status.properties` 之类的跟踪文件。两种解法：

- 用你自己的账户在终端里跑（推荐）；
- 或者把 `build/make.json` 的 `MVN_CMD` 改成 `"mvn -o"`（纯离线，不查元数据、不写跟踪文件）。
  2026-11 在受限环境里实测：改成 `mvn -o` 后完整构建通过。

**构建报"有实例正在从本产物运行"**
`dist/wn4j/run/` 里有活着的运行锁（`pid-<port>.json` 且进程还在）。先停掉那个实例：
`dist\wn4j\sbin\stop.cmd`，再构建。脚本这么拦是为了避免删到一半撞上 jar 文件锁。

**启动报 `BindException`**
端口被占。确认 7709/7710 没被别的进程用（`netstat -ano | findstr 7709`），
或改 `make.json` 的 `APP_PORT` 后重新 `--dist`。

**启动后连不上、日志报 `LostBucket`**
两个实例用了同一个库或同一个桶目录。检查 `build.rs.json` 的 mongo `db` / redis `port`+`db` /
`buck`+`swap` 是否与开发者日常实例冲突。

**`sbin/start.cmd` 反复消失**
杀毒软件的启发式规则误杀（它被识别成"投放器"）。把 `dist/` 与 `build/` 加进信任区，
再从 `build/start.cmd` 拷回产物。

**Docker 相关报错**
`make.json` 的 `REDIS_SERVER` 用了 `Docker:redis<sha256:…>=wn4j-redis-L2`，
要求本机**已有**这个镜像（脚本不会自动 pull）。没有 Docker 就把 `REDIS_SERVER` 清空，
重新 `--dist`——产物里不会生成 `*_with_redis.cmd`，改用 `start.cmd`/`stop.cmd`，
redis 走 `build.rs.json` 里配的那个（通常是开发者本机的 6379）。

## 七、和 1 级实例的关系

| | 1 级实例 | 2 级实例（本产物） |
|---|---|---|
| 谁启动 | 开发者在 IDE 里 | 开发者用 `sbin/*.cmd` |
| 端口 | 通常 8080 | `make.json` 的 `APP_PORT`（7709） |
| 数据 | 开发者自己的库 | `build.rs.json` 指定的独立库 |
| 用途 | 边改边调、看日志 | 智能体跑集成测试、建基线 |
| 智能体 | 只连接，不启停 | 只连接，不启停 |

集成测试（`atest/run.py`）通过 `.wn-instances.json` 找实例：

```json
{ "L1": { "host": "127.0.0.1", "port": 8080 },
  "L2": { "host": "127.0.0.1", "port": 7709 } }
```

## 八、更多

- 构建机制与改动清单：`wn-doc/public/reference/102.auto-test/10-at-build.md`
- 启动/建域/探活：`20-at-setup.md`、`30-at-tools.md`
- 集成测试怎么跑：`walnut/atest/README.md`
