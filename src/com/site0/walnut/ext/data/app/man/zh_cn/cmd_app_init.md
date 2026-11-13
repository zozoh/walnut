# 命令简介

`app init` 命令用来根据一个布局模板，为一个域（Home）创建整套标准的
文件结构与对象，并初始化环境变量。

如果想了解背景与详细用法，请阅读文档 [域应用初始化](../../../../../../../../../doc/reference/101.dev/10-init-domain-app.md)。

# 使用方法

```bash
app init
    [/path/to/init-dir]     # 【选】模板所在目录（不是模板文件），与 -by 拼出模板路径
    [-conf /path/to/x.init] # 【选】直接指定模板文件，优先级最高（忽略首参与 -by）
    [-dir /path/to]         # 【选】目标目录路径，默认为 ~
    [-vars {...}]           # 【选】占位符填充的上下文 JSON
    [-by xxx.init]          # 【选】模板文件名，缺省为 _files
                            #      非 ".init" 结尾会自动补 ".init" 后缀
    [-script 名称]          # 【选】收尾脚本名，默认为 "_script"
                            #      设置为 "off" 表示不执行
```

> 为了找到模板文件，会依次尝试：

1. `-conf` 显式给出的模板**文件**路径
2. 命令第一个参数 `args[0]` 是模板**所在目录**，实际读取 `args[0]/<by>`
   （`<by>` 即 `-by`，缺省 `_files`；例如 `app init ~/.atest/init -by atest.init`
   读的是 `~/.atest/init/atest.init`）
3. `~/.domain/init/<by>`，再尝试 `~/.domain/init/${init_name}.init`——
   `init_name` 取用户元数据 `init_name`，缺省为**用户所在组名**，
   可用 `INIT_NAME` 环境变量覆盖
4. `/mnt/project/${init_name}/init/domain/<by>`，再依次尝试
   `/mnt/project/${init_name}/init/<by>`、`/mnt/project/${init_name}/init/${init_name}.init`
5. `/mnt/project/${init_name}/*/init/domain/<by>`

> 旧约定的 `_files` 命名仍被兼容（查找链与 `-by` 默认值均支持），
> 但新模板请统一使用 `xxx.init` 命名。

> 模板目录 `oHome` 就是模板文件所在目录（`-conf` 时是它所在的父目录），
> 模板里的 `%COPY > 相对路径` 与收尾脚本都在这个目录下找。

> 为了找到上下文变量，会依次尝试：

1. 命令参数 `-vars` 指向的 JSON（整个值是一个 JSON 对象，建议用单引号包住；
   JSON 里不要出现单引号）
2. 标准输入流 `[STDIN]`
3. `~/.domain/vars.json`（缺省为 `{}`）

# 实例

```bash
# 根据模板初始化自己的主目录（走查找链）
app init

# 模板在 ~/.atest/init 目录，文件名 atest.init；不跑收尾脚本
app init ~/.atest/init -by atest.init -script off

# 直接指定模板文件，并显式传入占位符变量
app init -conf ~/.atest/init/atest.init -vars '{"who": "walnut"}' -script off

# 根据指定的模板目录初始化某个目录
app init /etc/myapp/init -dir ~/abc

# 从管道传入变量
echo '{"host": "www.mysite.com"}' | app init

# 执行域初始化，但跳过收尾脚本
app init -script off
```
