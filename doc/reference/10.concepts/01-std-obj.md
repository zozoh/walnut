---
title: 标准对象（一切皆对象）
module: api.io
keywords: [WnObj, 对象, 字段缩写, race, ln, 链接, expi, mode, 元数据, pvg, 一切皆对象]
source:
  - api/io/WnObj.java
  - core/bean/WnIoObj.java
  - api/io/WnRace.java
  - core/io/WnIoHookedWrapper.java
  - web/clean/WnExpiObjTableCleaner.java
related: [02-io-indexer-bm.md, 03-domain-home.md, ../20.io-storage/01-io-model.md, privilege]
---

# 标准对象（一切皆对象）

Walnut 中**所有数据都是树形空间里的对象（WnObj）**：文件、目录、会话、支付单、任务、验证码……没有例外。
理解 WnObj 的字段体系，就是理解 Walnut 的数据观。

## 种族（race）

`WnRace` 枚举只有两种取值，没有第三种：

- `DIR` — 目录，可有子对象
- `FILE` — 文件，有内容（内容存于桶，见 [IO / Indexer / BM](02-io-indexer-bm.md)）

初学者常见的疑惑：「链接是不是第三种 race？」**不是**。链接不是一个 race，
它只是**对象的一个元数据**——详见下文[链接（ln）](#链接ln)。

## 链接（ln）

一个对象只要带上了 `ln` 元数据，它就成了**链接对象**：`isLink()` 的判定就是
「`ln` 字段非空」（`core/bean/WnIoObj.java`）。除此之外它和普通对象没有任何区别——
创建链接时它的 race 继承目标对象的 race（`cmd_ln`：`sys.io.create(p, dst, oSrc.race())`），
所以它仍是 DIR 或 FILE，只是读它的时候，系统会顺着 `ln` 找到真正的目标。

`ln` 的值有三种形态（`util/Wn.java` 的 `Wn.real` 负责解析）：

```bash
ln = "id:xxxxx"      # 按对象 ID 定位（最可靠，目标移动后仍然有效）
ln = "/home/demo/a"  # 绝对路径定位
ln = "sub/a.txt"     # 相对路径定位（相对链接对象所在的父目录）
```

解析规则（`Wn.real(o, io, memo)`）：

- `id:` 前缀 → `io.get(id)`；绝对路径 → 从根 fetch；相对路径 → 从链接对象的父目录 fetch
- **递归展开**：目标本身也是链接就继续展开，直到找到非链接对象；用 memo 记录展开过的对象 ID，
  若重复出现说明成环，返回 null（不会死循环）
- 目标不存在时抛 `e.io.obj.noexists`
- 展开后真实对象上会记下 `fromLink`（原链接路径），用于缓存包装层及时清理链接目录的缓存

日常用法：

```bash
ln -s ~/docs/readme.md ~/links/rm      # 软链接（默认，ln 值为路径）
ln -s id:9mXkVf ~/links/rm              # 也可以直接写 id，更抗移动
rm ~/links/rm                           # 删除链接不影响目标
```

## 字段缩写体系

与 Unix 神似但全部缩写，这是 Walnut 最重要的「识字表」：

```bash
id  : ID        # 对象唯一 ID（UUID32）
pid : ID        # 父对象 ID
nm  : "abc.txt" # 名称（同级唯一）
race: DIR|FILE
#--------------------------
# 权限（三段八进制，第一段是 admin 不是 owner！见下）
c : "root"      # 创建者(creater)
g : "root"      # 所属组(grp)——对象属于哪个域
m : 448         # mode 权限位
md: {...}       # 扩展元数据（通常并入对象本身，少见独立）
#--------------------------
# 内容
tp    : "jpg"       # 类型后缀（决定默认缩略图和 MIME）
mime  : "image/jpeg"
sha1  : "4e8a..."   # 内容指纹（目录无）
len   : 237370      # 内容字节数
#--------------------------
# 结构辅助
d0  : "home"    # 所属一级目录名
d1  : "demo"    # 所属二级目录名（通常=域名）
lbls: ["bug","A"]# 自由标签，可查询
#--------------------------
# 映射与链接
mnt : "sql(x:t)"              # 存储映射（见 io-indexer-bm）
ln  : "id:xxx"              # 链接目标（见上文「链接（ln）」）
#--------------------------
# 时间
ct   : AMS      # 创建时间（绝对毫秒）
lm   : AMS      # 最后修改时间
expi : AMS      # 过期时间，到期系统自动删除（0=永不过期，见下文「expi 删除机制」）
```

对象上还可以有**任意自定义元数据**：业务字段直接放对象上（MongoDB 索引支持查询），
不用建表——这是 Walnut 作为业务基座的关键能力。

## mode：三段八进制，第一段是 admin

```bash
  admin    member   others
[r][w][x] [r][w][x] [r][w][x]
```

与 Unix 的关键差异：**第一段不是 owner 而是 admin**（属组的管理员）。
对象没有 owner 概念，只有 `c`（创建者，仅记录）和 `g`（所属组）。
设计动机是强调**组内协作**——一个域的数据由多人共同维护。`/home` 权限 `rwxrwxr-x`：
任何人可进入，但列目录只能看到自己有 `r` 的。`x` 对文件是可执行，对目录是可进入。

## 扩展权限 pvg

对象可声明 `pvg` 元数据做更细的授权（向上查祖先，父级优先）：

```js
{
  pvg: {
    "xiaobai" : 5,     // 八进制 101 = r-x
    "@operator": 7     // @开头表示业务角色
  }
}
```

详见 [三层权限](../30.auth/privilege.md)与[业务权限](../30.auth/biz-privilege.md)。

## expi 删除机制

`expi`（过期时间，绝对毫秒）是对象的「自毁装置」：`expi <= 0` 永不过期；否则到期后由系统**自动、不可逆地删除**。
临时文件、验证码、短期会话都靠它回收，不需要任何手工清理。完整链路分四步（源码：`core/io/WnIoHookedWrapper.java`、
`core/io/AbstractWnIoWrapper.java`、`web/clean/WnExpiObjTableCleaner.java`）：

**1. 注册**——所有写路径自动登记

系统的 IO 是一层包装链：`rawIo` → `hookedIo`（钩子 + expi 登记）→ 缓存包装（`conf/ioc/io.js`）。
每次创建对象或修改元数据（`setBy` / `move`），只要对象带 `expi > 0`，
`tryAddExpiObj` 就把 `(对象ID, expi)` 写入**过期记录表** `WnExpiObjTable`（Mongo 实现，
`WnSafeExpiObjTable` 加锁包装，保证多节点安全）。`expi <= 0` 的写法不会登记。

**2. 候命**——过期记录表只记两件事

`WnExpiObjTable` 是一张「id → 过期时间」的登记表，按过期时间排序，最老的在前。
它不参与日常读写，只给清理线程当工单。

**3. 清理**——后台线程定期接管并删除

`WnExpiObjTableCleaner` 是启动时创建的后台线程（`web/clean/`），循环逻辑：

```bash
每 expi-obj-clean-interval（默认 60000ms）醒一次：
  1. takeover(节点名, cleanHold, cleanLimit)
     ── 借助锁服务集群安全地「接管」最老的一批记录（默认 100 条）
  2. 逐条处理：
     对象还在 + 已过期   → io.delete(o, true) 递归删除（真正清理）
     对象还在 + 未过期   → 从表里移除（有人设了又撤了 expi，撤单）
     对象已不存在        → 仅记日志（残留工单）
  3. 睡一个间隔，回到 1
```

配置项（`conf/web.properties`）：`expi-obj-clean-interval`（轮询间隔）、
`expi-obj-clean-limit`（每批数量）、`expi-obj-clean-hold`（接管持有时间，默认与间隔相同）。

**4. 注销**——删除对象时同步清表

任何路径删除对象（`WnIoImpl2.whenDelete` 回调）都会把它的过期记录一并从表里移除，
避免残留工单。

实用提示：

```bash
# 查看当前毫秒（date -ms），再加上偏移量得到目标过期时间
date -ms

# 给对象设置过期时间（写绝对毫秒，例如 1 小时后就加 3600000）
o ~/tmp/a.txt -u 'expi:1730265600000'

# 撤销过期（设回 0），清理线程下次扫描时会把登记表里的工单撤掉
o ~/tmp/a.txt -u 'expi:0'
```

更完整的机制说明见 [过期清理](../20.io-storage/05-expiration.md)。

## 常用操作示例

```bash
# 查看对象元数据（-c 紧凑 -q 不加引号 -n 忽略 null）
o /home/demo/abc.txt -cqn
# → {id:"...",pid:"...",nm:"abc.txt",race:"FILE",sha1:"...",len:237370,...}

# 创建目录/文件
mkdir ~/docs
echo "hello" > ~/docs/a.txt        # 重定向即写入

# 改元数据
o ~/docs/a.txt -u 'lbls:["x","y"],brief:"一个文件"'

# 按标签查询
o ~/docs -match 'lbls:"x"' -cqn
```

## 元数据约定速查

模块间协作靠**元数据约定**，看懂这些键就能看懂任何域的配置：

- **`mnt`** — 存储映射（值示例 `sql(demo:t_news)`） — 详见 [io-indexer-bm](02-io-indexer-bm.md)
- **`data`** — 对象在桶管理器内的数据段 ID
- **`expi`** — 过期绝对毫秒，到期自动删除 — 机制见上文 [expi 删除机制](#expi-删除机制)，更多见 [过期清理](../20.io-storage/05-expiration.md)
- **`tp` / `mime` / `thumb`** — 类型后缀 / 内容类型 / 缩略图对象引用
- **`lbls` / `d0` / `d1`** — 标签数组（可查询）/ 一二级目录名（加速范围查询）
- **`title` / `brief`** — 显示名与摘要（业务通用）
- **`http-header-*` / `api-access-token` 等 `http-*`** — regapi 专用约定 — 详见 [API 对象](../50.regapi/api-object.md)

## 注意事项

- `id` 全库唯一；路径由树推导，对象移动后路径即变
- 大字段内容不要放元数据，写入文件内容（桶）并用 `sha1` 关联
- `expi` 一旦设置，到期即被系统删除（不可逆），机制见上文[expi 删除机制](#expi-删除机制)

## 深入阅读

- [IO / Indexer / BM](02-io-indexer-bm.md)：对象的存储模型
- [域与主目录](03-domain-home.md)：对象树的全局布局
- [IO 模型](../20.io-storage/01-io-model.md)：WnIO 读写流程
