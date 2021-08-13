# 过滤器简介

`@config` 管理上下文的配置。

其中，加载的配置文件是`json`格式，由一个列表构成，每个列表元素会指明下面的信息：

- `test` : **判断条件** : 根据上下文变量，决定是否执行
- `actions` : **动作列表** : 其中每个列表元素会有下面的信息：
   - `type` : **动作类型** : 动作的类型，支持：
      - `thing_create` : 创建数据集对象
      - `thing_update` : 更新数据集对象
      - `thing_delete` : 删除数据集对象
      - `thing_clear` : 删除一组符合条件的数据集对象
      - `obj_create`  : 创建对象
      - `obj_update` : 更新对象
      - `obj_delete` : 删除对象
      - `obj_clear` : 删除一组符合条件的对象
      - `exec` : 执行一段命令脚本模板
      - `jsc` : 执行一段 JS 脚本
   - `path` : **目标路径** : 不同的动作类型对其有各自的理解
   - `params` : **动作参数** : 不同的动作类型对其有各自的理解
   - `targetId` : **目标ID** : 某些动作类型需要它
   - `meta` : **动作元数据** : 不同的动作类型对其有各自的理解
   - `input` : **动作输入** : `exec` 有用
   - `sort`  : **排序方式** : `thing|obj_clear` 有用
   - `limit` : **最多限制** : `thing|obj_clear` 有用
   - `skip`  : **跳过数据** : `thing|obj_clear` 有用
 

# 用法

```bash
o @config
  [PATH...]         # 多个加载路径
  -clear            # 清空上下文的配置信息
```

# 示例

```bash
# 加载两个 JSON 文件配置到上下文
react @config ~/a.json ~/b.json

# 动态决定 JSON 文件的加载路径
react @config ~/a-${update.type}.json
```

