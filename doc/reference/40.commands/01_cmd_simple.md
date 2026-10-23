---
title: 简单命令
module: impl.box
keywords: [命令, 简单命令, JvmExecutor, cmd_cat, ZParams, WnSystem]
source:
  - impl/box/JvmExecutor.java
  - impl/box/cmd/cmd_cat.java
  - util/ZParams.java
related: [../10.concepts/04-command-model.md, custom-command, o, sqlx]
---

# 简单命令

**简单命令**直接继承 `JvmExecutor`，一个命令一个源文件，一次调用完成一件事情。
它是 Walnut 命令的基础形态：参数进来、结果出去，没有子命令，也没有跨调用的上下文。

系统内置的 73 个命令全部是简单命令；扩展命令中有 23 个是简单命令。

## 代码形态

一个最简单的命令长这样（以 `cmd_echo` 为例）：

```java
package com.site0.walnut.impl.box.cmd;

import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_echo extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        // 用 ZParams 解析参数
        ZParams params = ZParams.parse(args, "^(n|l|e)$");
        // 向标准输出打印
        sys.out.println(params.val(0));
    }

}
```

### 接口与参数详解

**`exec(WnSystem sys, String[] args)`**

命令的入口方法，两个参数：

- **`sys`**：系统运行时句柄，是命令与 Walnut 交互的唯一通道。常用成员：
  - `sys.in` / `sys.out` / `sys.err`：标准输入输出流（详见 [命令输入输出](05_cmd_std_in_out.md)）
  - `sys.io`：IO 服务，读写对象（`sys.io.readText(o)`、`sys.io.getInputStream(o, 0)` 等）
  - `sys.session`：当前会话（环境变量、用户身份）
  - `sys.auth`：登录鉴权接口
  - `sys.jef`：命令工厂，可执行子命令（`sys.exec(...)` 系列方法）
  - `sys.cmdOriginal`：原始命令行文本
  - `sys.getMe()` / `sys.getHome()` / `sys.getCurrentObj()`：当前用户 / 主目录 / 当前目录对象
- **`args`**：参数数组。已经过 `prepareArgs` 预处理——默认实现会剥掉参数首尾成对的引号。
  如果希望保留引号（例如值本身可能是 `@xxx`），子类可以重载 `prepareArgs`。

**`ZParams.parse(args, "^(n|l|e)$")`**

参数解析统一用 `ZParams`：

- **`params.vals`**：位置参数数组（非 `-` 开头的部分）
- **`params.is("quiet")`**：开关参数是否存在（`-quiet` → true）
- **`params.getString("f")`**：带值参数（`-f xxx` → xxx）
- **第二个参数是开关正则**：匹配该正则的参数按布尔开关解析，不进 `vals`。
  注意 `params.is(...)` 的大小写敏感——按源码里 `params.is("noTop")` 的写法原样传

**其他可重载点**

- `prepareArgs(args)`：参数预处理（默认剥引号）
- `getManual()`：读取手册（基类已实现，按约定路径找 markdown，见下）

## 完整示例：`cmd_cat`

`cat` 输出一个或多个文件的全部内容，是典型简单命令：

```java
package com.site0.walnut.impl.box.cmd;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Streams;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_cat extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {

        // 不能为空
        if (args.length == 0) {
            throw Er.create("e.cmd.noargs");
        }
        // 输出每个路径
        ZParams params = ZParams.parse(args, "^(quiet)$");
        boolean isQuiet = params.is("quiet");

        // 计算要列出的要处理的对象
        List<WnObj> list = null;

        // 尝试获取
        try {
            list = Cmds.evalCandidateObjsNoEmpty(sys, params.vals, 0);
        }
        catch (Throwable e) {
            // 静默模式
            if (isQuiet) {
                list = new LinkedList<>();
            }
            // 普通模式，抛错
            else {
                throw Er.wrap(e);
            }
        }

        // 没内容
        if (list.isEmpty()) {
            if (!isQuiet) {
                throw Er.create("e.io.obj.noexists", sys.cmdOriginal);
            }
        }
        // 输出内容
        for (WnObj o : list) {
            // 解开链接
            WnObj o2 = Wn.real(o, sys.io, new HashMap<>());
            // 目录不能输出
            if (o2.isDIR()) {
                sys.err.printlnf("e.io.readdir : %s", o2.path());
                continue;
            }
            // 输出内容
            InputStream ins = sys.io.getInputStream(o2, 0);
            try {
                sys.out.write(ins);
            }
            finally {
                Streams.safeClose(ins);
            }
        }
    }

}
```

要点解读：

- `Cmds.evalCandidateObjsNoEmpty(sys, params.vals, 0)`：把位置参数批量解析成 `WnObj`（支持路径与 `id:` 两种写法），这是内置命令处理对象参数的惯例
- `Wn.real(o, ...)`：解开链接，拿到真实对象
- `sys.err.printlnf(...)`：错误信息走标准错误流，不污染标准输出
- 抛错统一用 `Er.create("e.xxx.yyy", ...)`，错误码会被 `ajaxre` 等命令识别

## 使用方法

```bash
cat ~/abc.txt                                # 按路径输出
cat id:fqmnqpKVIvu91vYZgePoQ0                # 按 ID 输出
cat a.txt b.txt                              # 一次输出多个
cat -quiet ~/maybe-not-exists.txt            # 不存在时静默
```

运行时查看手册：`man cat`。

## 文档放在哪里

每个命令一篇中文手册，与源码同包，运行时 `man` 命令直接读取：

- 内置：`src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_<命令名>.md`
- 扩展：`src/com/site0/walnut/ext/**/man/zh_cn/cmd_<命令名>.md`

查找顺序为 `man/zh_cn/`（新）→ 包根 `.md`（旧）→ 包根 `.man`（更旧）。
文档格式约定见 [write-doc.skill](../../skill/write-doc.skill.md)（简介 / 用法 / 示例三部分）。

## 现有的简单命令

- 内置 73 个（其中 `obj` 已弃用，用 `o` 替代）
- 扩展 23 个
- 命令名链接指向其源码包内的中文手册

### 内置·文件与对象

- **[cat](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_cat.md)**：输出文件内容
- **[cd](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_cd.md)**：切换当前目录
- **[chgrp](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_chgrp.md)**：修改对象属组
- **[chimg](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_chimg.md)**：图片转换（缩放/旋转/iOS 方向修正）
- **[chmod](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_chmod.md)**：修改对象权限位
- **[chown](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_chown.md)**：修改对象属主
- **[count](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_count.md)**：统计目录子对象数量
- **[cp](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_cp.md)**：拷贝对象
- **[cpobj](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_cpobj.md)**：拷贝对象（元数据复制策略完整版）
- **[defthumbnail](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_defthumbnail.md)**：生成系统默认缩略图
- **[dircount](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_dircount.md)**：按深度统计目录子对象数量
- **[disk](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_disk.md)**：列出目录子对象
- **[du](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_du.md)**：统计目录空间占用
- **[find](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_find.md)**：遍历目录树，输出相对路径
- **[fnm](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_fnm.md)**：生成不冲突的文件名
- **[hex](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_hex.md)**：十六进制查看
- **[ln](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_ln.md)**：创建链接（硬/软）
- **[ls](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_ls.md)**：列出目录内容
- **[ll](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_ll.md)**：`ls -l` 简写
- **[mkdir](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_mkdir.md)**：创建目录
- **[mount](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_mount.md)**：设置目录 mnt 映射
- **[mv](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_mv.md)**：移动/重命名
- **[obj](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_obj.md)**：旧对象命令，已弃用，用 `o` 替代
- **[pwd](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_pwd.md)**：显示当前目录
- **[rename](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_rename.md)**：重命名对象
- **[rm](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_rm.md)**：删除对象
- **[touch](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_touch.md)**：创建文件/刷新时间戳
- **[umount](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_umount.md)**：卸载目录 mnt 映射
- **[unzip](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_unzip.md)**：解压 zip 包
- **[zip](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_zip.md)**：打包压缩
- **[hook](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_hook.md)**：查询/重跑对象钩子

### 内置·文本与数据

- **[ajaxre](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_ajaxre.md)**：包装成 AjaxReturn 格式输出
- **[code](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_code.md)**：管道内容按指定字符集编码输出
- **[date](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_date.md)**：显示/格式化日期时间
- **[echo](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_echo.md)**：输出文本
- **[env](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_env.md)**：显示会话环境变量
- **[export](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_export.md)**：设置会话环境变量
- **[grep](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_grep.md)**：筛选匹配行（正则/子串）
- **[httpout](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_httpout.md)**：输出标准 HTTP 响应
- **[httpparam](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_httpparam.md)**：解析 HTTP 请求参数
- **[jsc](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_jsc.md)**：执行 JS 脚本
- **[json](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_json.md)**：JSON 加工（取值/过滤/映射/合并）
- **[me](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_me.md)**：当前用户信息与变量
- **[output](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_output.md)**：延迟/批量输出（管道测试用）
- **[random](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_random.md)**：生成随机数
- **[run](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_run.md)**：执行命令模板（逐行作为命令执行）
- **[str](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_str.md)**：字符串转换（URI 解码/去空白/替换）
- **[textTable](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_textTable.md)**：JSON 渲染文本表格
- **[time](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_time.md)**：计量命令耗时
- **[trim](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_trim.md)**：去除文本首尾空白
- **[uuid](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_uuid.md)**：生成 UUID
- **[wordt](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_wordt.md)**：Word 文档占位符替换

### 内置·账户与系统

- **[adduser](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_adduser.md)**：添加用户
- **[alias](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_alias.md)**：命令别名
- **[cmds](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_cmds.md)**：列出全部可用命令
- **[exit](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_exit.md)**：登出当前会话
- **[grp](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_grp.md)**：用户组管理（查组员/增删成员）
- **[ioc](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_ioc.md)**：列出 IoC 容器 Bean
- **[jvmMemory](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_jvmMemory.md)**：查看 JVM 内存
- **[login](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_login.md)**：创建子会话（su）
- **[man](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_man.md)**：查看命令手册
- **[mode](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_mode.md)**：权限码分析/换算
- **[passwd](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_passwd.md)**：修改密码
- **[resetrootpasswd](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_resetrootpasswd.md)**：重置 root 密码
- **[session](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_session.md)**：查看/列出会话
- **[sys](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_sys.md)**：查看系统级信息
- **[whoami](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_whoami.md)**：当前用户名

### 内置·摘要与下载

- **[curl](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_curl.md)**：发起 HTTP 请求
- **[md5sum](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_md5sum.md)**：MD5 摘要
- **[sha1sum](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_sha1sum.md)**：SHA1 摘要
- **[wget](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_wget.md)**：下载文件

### 内置·图片

- **[image](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_image.md)**：读取图片信息（尺寸/EXIF）
- **[iimg](../../../src/com/site0/walnut/impl/box/cmd/man/zh_cn/cmd_iimg.md)**：图片信息/缩略图/压缩

### 扩展·数据与实体

- **[buy](../../../src/com/site0/walnut/ext/data/entity/buy/man/zh_cn/cmd_buy.md)**：购物车管理
- **[captcha](../../../src/com/site0/walnut/ext/data/captcha/man/zh_cn/cmd_captcha.md)**：生成图形验证码
- **[favor](../../../src/com/site0/walnut/ext/data/entity/favor/man/zh_cn/cmd_favor.md)**：收藏管理
- **[like](../../../src/com/site0/walnut/ext/data/entity/like/man/zh_cn/cmd_like.md)**：点赞管理
- **[score](../../../src/com/site0/walnut/ext/data/entity/score/man/zh_cn/cmd_score.md)**：打分管理

### 扩展·媒体

- **[avatar](../../../src/com/site0/walnut/ext/media/avatar/man/zh_cn/cmd_avatar.md)**：文字头像生成
- **[imagic](../../../src/com/site0/walnut/ext/media/imagic/man/zh_cn/cmd_imagic.md)**：图像处理（缩放/裁剪/镜像等过滤器）
- **[qrcode](../../../src/com/site0/walnut/ext/media/qrcode/man/zh_cn/cmd_qrcode.md)**：二维码生成与解析
- **[sheet](../../../src/com/site0/walnut/ext/media/sheet/man/zh_cn/cmd_sheet.md)**：JSON 数组 ⇄ 表格（csv/xls）转换，字段映射
- **[videoc](../../../src/com/site0/walnut/ext/media/ffmpeg/man/zh_cn/cmd_videoc.md)**：视频转码（ffmpeg），产出预览视频/预览图
- **[videoi](../../../src/com/site0/walnut/ext/media/ffmpeg/man/zh_cn/cmd_videoi.md)**：读取视频元信息

### 扩展·网络

- **[aliyunoss](../../../src/com/site0/walnut/ext/net/aliyun/oss/man/zh_cn/cmd_aliyunoss.md)**：阿里云 OSS 文件操作
- **[aliyunvod](../../../src/com/site0/walnut/ext/net/aliyun/vod/man/zh_cn/cmd_aliyunvod.md)**：阿里云 VOD（播放凭证/搜索）
- **[ping](../../../src/com/site0/walnut/ext/net/ping/man/zh_cn/cmd_ping.md)**：网络连通探测
- **[shorturl](../../../src/com/site0/walnut/ext/util/shorturl/man/zh_cn/cmd_shorturl.md)**：短链接生成
- **[socket](../../../src/com/site0/walnut/ext/net/socket/man/zh_cn/cmd_socket.md)**：socket 通信
- **[whoisx](../../../src/com/site0/walnut/ext/net/whoisx/man/zh_cn/cmd_whoisx.md)**：域名 WHOIS 友好查询

### 扩展·系统与工具

- **[base64](../../../src/com/site0/walnut/ext/util/base64/man/zh_cn/cmd_base64.md)**：base64 编码/解码（管道）
- **[btea](../../../src/com/site0/walnut/ext/util/btea/man/zh_cn/cmd_btea.md)**：btea 对称加解密（管道）
- **[dirm](../../../src/com/site0/walnut/ext/sys/dirm/man/zh_cn/cmd_dirm.md)**：目录映射管理
- **[hexbin](../../../src/com/site0/walnut/ext/util/hexbin/man/zh_cn/cmd_hexbin.md)**：二进制 ⇄ HEX（管道）
- **[i18n](../../../src/com/site0/walnut/ext/sys/i18n/man/zh_cn/cmd_i18n.md)**：本地化字符串支持
- **[os](../../../src/com/site0/walnut/ext/sys/os/man/zh_cn/cmd_os.md)**：系统信息（字体等）
