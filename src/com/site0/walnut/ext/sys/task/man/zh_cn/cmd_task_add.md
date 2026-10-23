# 过滤器简介

`@add` 向任务堆栈中添加一条任务。

命令执行后返回当前任务堆栈里剩余的任务对象列表：

```json
{
  "count" : 1,
  "tasks" : [{
    "id"   : "56y1..a8e2",
    "user" : "zozoh",
  }]
}
```

# 用法

```bash
task @add [命令模板] [-base64] [-input 内容] [-f 路径] [-u 用户] [-quiet] [-json] [-cqn]
  # 命令模板    # 任务执行的命令模板，不给则从标准输入读取
  -base64     # 输入的命令采用了 Base64 编码，入栈前预先解码
  -input      # 任务的标准输入内容；只声明不给值则试图从标准输入读取
  -f          # 通过文件指定标准输入内容，比 -input 优先级更高
  -u          # 指定任务的所属用户【需管理员权限】
  -quiet      # 静默模式不输出任何内容
  -json       # 结果按 JSON 方式打印，默认仅打印摘要
  -cqn        # JSON 模式的格式化开关
```

# 示例

```bash
# 添加一条任务
task @add 'echo `date` >> ~/task.log'

# 命令从管道读
echo 'obj ~/tmp @count' | task @add

# 任务的输入内容来自文件
task @add 'cat > ~/out.txt' -f ~/in.txt
```
