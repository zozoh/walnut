# wn-cli — Walnut 智能体客户端

登录（含建域决策树）、命令通道、对象操作、本地挂载。契约见
`{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

```bash
# 登录：给 --level 就不用写 --url（端点从 .wn-instances.json 解析）
# 域不存在时自动转 root 建域；密码错 3 次终止
python wn-cli.py --level L1 --dist D:/work/zozoh/walnut/dist/wn4j --who big-dad \
    login --usr demo --passwd 123456

# root 登录不用传密码：按 web_local.properties → web.properties →
# 缺省 123456 的顺序现找，失败终止并请主人提供
python wn-cli.py --level L2 --dist D:/work/zozoh/walnut/dist/wn4j --who big-dad \
    login --usr root

# 之后带同一个 --level 即可，会话与端点都自动挑（也可显式 --se L1-demo-00）
python wn-cli.py --level L1 cmd 'o ~/data -l'
python wn-cli.py --level L1 cmd 'mkdir ~/tmp/x && echo hi > ~/tmp/x/a.txt'
echo 'app init' | python wn-cli.py --level L1 cmd -

# 对象操作（文件字节走 REST）
python wn-cli.py --level L1 obj children ~/data
python wn-cli.py --level L1 obj save-text ~/.domain/vars.json --file ./vars.json
python wn-cli.py --level L1 obj content ~/data/report.pdf --out ./report.pdf
python wn-cli.py --level L1 obj upload ./logo.png ~/www/logo.png
python wn-cli.py --level L1 obj set ~/thedir 'mnt="file://D:/work/proj/www"'

# 挂载本地目录（写完自动 ls 自检）
python wn-cli.py --level L1 mnt ~/site D:/work/proj/www
```

**实例登记** `.wn-instances.json`（开发者维护，智能体只读）：

```json
{"L1": {"host": "127.0.0.1", "port": 8080},
 "L2": {"host": "127.0.0.1", "port": 8090}}
```

查找顺序：`--instances <路径>` → `$WN_INSTANCES` → 从当前目录逐级向上 → `~/.wn-instances.json`。

会话缓存：`<工作区>/.at/client/session/<级别>-<登录名>-00.toml`（如 `L1-demo-00.toml`；TOML）。
**会话池是工作区级的**——同一工作区里所有项目共用一份登录，换个仓库不用重登。
目录解析顺序：`--session-dir` → `$WN_SESSION_DIR` → 工作区级（`.wn-instances.json` 所在目录）
→ 项目级 `./.at/client/session`。**级别进名字**，1 级/2 级互不串用；第二个会话用 `--se L1-demo-01`。
**缓存不存密码**：root 密码每次按上面顺序从实例配置现找，域密码不落盘。
401：root 会话自动重登一次；确认失效的会话**直接删掉本地缓存**并报错（码 3），
下次 login 从干净状态开始（网络错误不删）。

也可作库使用：

```python
from wn_cli import WnClient
c = WnClient("http://localhost:8080")
c.login("demo", "123456")
print(c.cmd("o ~ -l"))
```

退出码：`0` 成功；`1` 用法；`2` 对象不存在；`3` 认证失败；`4` 服务器异常。
