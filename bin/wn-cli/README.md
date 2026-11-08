# wn-cli — Walnut 智能体客户端

登录（含建域决策树）、命令通道、对象操作、本地挂载。契约见
`{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

```bash
# 登录（域不存在时自动转 root 建域；密码错 3 次终止）
python wn-cli.py --url http://localhost:8080 --se se-demo-00 \
    --dist D:/work/zozoh/walnut/dist/wn4j --who big-dad \
    login --usr demo --passwd 123456

# root 登录不用传密码：按 web_local.properties → web.properties →
# 缺省 123456 的顺序现找，失败终止并请主人提供
python wn-cli.py --url http://localhost:8080 --se se-root-00 \
    --dist D:/work/zozoh/walnut/dist/wn4j --who big-dad \
    login --usr root

# 之后省略 --url，用会话缓存里的 base 与票据（记得带同一个 --se）
python wn-cli.py --se se-demo-00 cmd 'o ~/data -l'
python wn-cli.py --se se-demo-00 cmd 'mkdir ~/tmp/x && echo hi > ~/tmp/x/a.txt'
echo 'app init' | python wn-cli.py --se se-demo-00 cmd -

# 对象操作（文件字节走 REST）
python wn-cli.py --se se-demo-00 obj children ~/data
python wn-cli.py --se se-demo-00 obj save-text ~/.domain/vars.json --file ./vars.json
python wn-cli.py --se se-demo-00 obj content ~/data/report.pdf --out ./report.pdf
python wn-cli.py --se se-demo-00 obj upload ./logo.png ~/www/logo.png
python wn-cli.py --se se-demo-00 obj set ~/thedir 'mnt="file://D:/work/proj/www"'

# 挂载本地目录（写完自动 ls 自检）
python wn-cli.py --se se-demo-00 mnt ~/site D:/work/proj/www
```

会话缓存：`.at/client/session/se-<登录名>-00.toml`（TOML 格式，约定命名；
兼容读旧 `.json`；`--se se-root-01` 开同名第二个会话，多个智能体各建各的文件）。
**缓存不存密码**：root 密码每次按上面顺序从实例配置现找，域密码不落盘。
401 时 root 会话自动重登一次，再失败报错退出（码 3）；域会话重新 login。

也可作库使用：

```python
from wn_cli import WnClient
c = WnClient("http://localhost:8080")
c.login("demo", "123456")
print(c.cmd("o ~ -l"))
```

退出码：`0` 成功；`1` 用法；`2` 对象不存在；`3` 认证失败；`4` 服务器异常。
