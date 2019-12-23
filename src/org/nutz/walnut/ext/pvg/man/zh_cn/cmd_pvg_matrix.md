命令简介
=======

`pvg matrix` 输出权限矩阵

用法
=======

```bash
pvg [PvgPath] matrix
    [-json]        # 【选】是否为 JSON 模式输出，默认为表格模式
    [-cqn]         # 【选】JSON 模式下的格式化参数
    [-t Yes:N/A]   # 【选】是和否的输出字符样式
    [-ibase 1]     # 【选】默认模式，行序号开始数字，默认 1
```

- 如果不指定 `-t`那么在默认模式下，相当于 `Yes:N/A`, JSON 模式下就是 `true/false`
- 
`-t` 模式的具体含义

  Params      |表格（是）| 表格（是）|JSON（是）|JSON（是） 
--------------|--------|--------|-------------|-----------
`undefined`   | `Yes`  | `--`   | `true`      | `false`
`"Yes:N/A"`   | `Yes`  | `N/A`  | `"Yes"`     | `"N/A"`
`"true:false"`| `true` | `false`| `true`      | `false`
`"On:Off"`    | `On`   | `Off`  | `"On"`      | `"Off"`
`"Yes"`       | `Yes`  | Empty  | `"Yes"`     | `undefined`
`"true"`      | `Yes`  | Empty  | `true`      | `undefined`
`":No"`       | Empty  | `No`   | `undefined` | `"No"`
`":false"`    | Empty  | `false`| `undefined` | false
  
示例
=======

```bash
# 打印权限矩阵表格
demo:> pvg ~/my-pvg.json matrix
 # | Roles | Action-A | Action-B | Action-C | Action-D | Action-E 
---|-------|----------|----------|----------|----------|----------
 1 | Role0 | Yes      | --       | --       | --       | --    
 2 | Role1 | Yes      | --       | Yes      | Yes      | --    
 3 | Role2 | Yes      | Yes      | --       | --       | --    
 4 | Role3 | Yes      | Yes      | Yes      | Yes      | Yes  
 5 | Role4 | Yes      | --       | Yes      | Yes      | Yes

# 输出权限矩阵表格的 JSON 形式
demo:> pvg ~/my-pvg.json matrix -json
{
  "Role1" : {
    "Action-A" : true,
    "Action-B" : true,
    "Action-C" : false,
    "Action-D" : false,
    "Action-E" : false,
  }
}
```
    
    
