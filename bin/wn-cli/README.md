# wn-cli — Walnut 智能体客户端

登录（含建域决策树）、命令通道、对象操作、本地挂载。契约见
`doc/reference/102.auto-test/30-at-tools.md`。

```bash
# 登录（域不存在时自动转 root 建域；密码错 3 次终止）
python wn-cli.py --url http://localhost:8080 login \
    --usr demo --passwd 123456 --root-passwd 123456

# 之后省略 --url，用会话缓存里的 base 与票据
python wn-cli.py cmd 'o ~/data -l'
python wn-cli.py cmd 'mkdir ~/tmp/x && echo hi > ~/tmp/x/a.txt'
echo 'app init' | python wn-cli.py cmd -

# 对象操作（文件字节走 REST）
python wn-cli.py obj children ~/data
python wn-cli.py obj save-text ~/.domain/vars.json --file ./vars.json
python wn-cli.py obj content ~/data/report.pdf --out ./report.pdf
python wn-cli.py obj upload ./logo.png ~/www/logo.png
python wn-cli.py obj set ~/thedir 'mnt="file://D:/work/proj/www"'

# 挂载本地目录（写完自动 ls 自检）
python wn-cli.py mnt ~/site D:/work/proj/www
```

会话缓存：`.at/client/session/se-00.json`（`--se se-01` 开第二个）。
401 时自动按缓存凭据重登一次，再失败报错退出（码 3）。

也可作库使用：

```python
from wn_cli import WnClient
c = WnClient("http://localhost:8080")
c.login("demo", "123456")
print(c.cmd("o ~ -l"))
```

退出码：`0` 成功；`1` 用法；`2` 对象不存在；`3` 认证失败；`4` 服务器异常。
