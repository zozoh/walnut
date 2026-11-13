"""CASE-105 命令通道建目录、写文件并按路径读回内容。

只造数据与取证，不做断言：actual 里的每个值都能在用例 `## 期望` 找到对应字段。
幂等：目录 / 文件对象用 -upsert，内容用 echo -n … > 覆盖写。
"""

import json

from atlib import emit, fixture, note, snap, wn

DIR = "~/atest-lab/repro"
FILE = "~/atest-lab/repro/hello.txt"
TEXT = "atest-repro-content"

note("铺 suite 夹具 atest/init/cmd_o.init（~/atest-lab 等静态布局）")
fixture()

note("建目录 %s（-race DIR -upsert，可重复执行）" % DIR)
wn("o @create -p ~/atest-lab -race DIR -upsert '{\"nm\":\"repro\"}'")

note("建文件对象 %s（-race FILE -upsert）" % FILE)
wn("o @create -p ~/atest-lab/repro -race FILE -upsert '{\"nm\":\"hello.txt\"}'")

note("命令通道写内容：echo -n %s > %s" % (TEXT, FILE))
wn("echo -n %s > %s" % (TEXT, FILE))

note("按路径读回内容：o %s @read @json -cqn" % FILE)
raw = wn("o %s @read @json -cqn" % FILE).strip()
line = raw.splitlines()[-1].strip() if raw else ""
try:
    readback = json.loads(line)
except ValueError:
    note("读回的内容不是 JSON：%r" % line[:200])
    raise SystemExit(2)

content = readback.get("content")
obj = snap(FILE)
note("读回 content=%r，对象 len=%r" % (content, obj.get("len")))

emit({
    "nm": obj.get("nm"),
    "race": obj.get("race"),
    "mime": obj.get("mime"),
    "len": obj.get("len"),
    "content": content,
})
