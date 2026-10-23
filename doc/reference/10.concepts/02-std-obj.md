---
title: 标准对象（一切皆对象）
module: api.io
keywords: [WnObj, 对象, 字段缩写, race, mode, 元数据, pvg, 一切皆对象]
source:
  - api/io/WnObj.java
  - core/bean/WnIoObj.java
  - api/io/WnRace.java
related: [io-indexer-bm, domain-home, io-model, privilege]
---

# 标准对象（一切皆对象）

Walnut 中**所有数据都是树形空间里的对象（WnObj）**：文件、目录、会话、支付单、任务、验证码……没有例外。
理解 WnObj 的字段体系，就是理解 Walnut 的数据观。

## 种族（race）

- `DIR` — 目录，可有子对象
- `FILE` — 文件，有内容（内容存于桶，见 [IO / Indexer / BM](01-io-indexer-bm.md)）
- `LINK` — 链接，指向另一个对象（`ln` 字段）

## 字段缩写体系

与 Unix 神似但全部缩写，这是 Walnut 最重要的「识字表」：

```bash
id  : ID        # 对象唯一 ID（UUID32）
pid : ID        # 父对象 ID
nm  : "abc.txt" # 名称（同级唯一）
race: DIR|FILE|LINK
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
mnt : "dao(x/t)://lbm(A)"   # 存储映射（见 io-indexer-bm）
ln  : "id:xxx"              # 链接目标
#--------------------------
# 时间
ct   : AMS      # 创建时间（绝对毫秒）
lm   : AMS      # 最后修改时间
expi : AMS      # 过期时间，到期系统自动删除（0=永不过期）
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

- **`mnt`** — 存储映射（值示例 `dao(demo/t_news)://lbm(A)`） — 详见 [io-indexer-bm](01-io-indexer-bm.md)
- **`data`** — 对象在桶管理器内的数据段 ID
- **`expi`** — 过期绝对毫秒，到期自动删除 — 详见 [过期清理](../20.io-storage/expiration.md)
- **`tp` / `mime` / `thumb`** — 类型后缀 / 内容类型 / 缩略图对象引用
- **`lbls` / `d0` / `d1`** — 标签数组（可查询）/ 一二级目录名（加速范围查询）
- **`title` / `brief`** — 显示名与摘要（业务通用）
- **`http-header-*` / `api-access-token` 等 `http-*`** — regapi 专用约定 — 详见 [API 对象](../50.regapi/api-object.md)

## 注意事项

- `id` 全库唯一；路径由树推导，对象移动后路径即变
- 大字段内容不要放元数据，写入文件内容（桶）并用 `sha1` 关联
- `expi` 一旦设置，到期即被系统删除（不可逆）

## 深入阅读

- [IO / Indexer / BM](01-io-indexer-bm.md)：对象的存储模型
- [域与主目录](03-domain-home.md)：对象树的全局布局
- [IO 模型](../20.io-storage/io-model.md)：WnIO 读写流程
