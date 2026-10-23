# 命令简介

`ioc` 列出 IoC 容器中全部 Bean 名称

用于排查组件装配：确认某个服务是否已被加载、拼写是否正确。

# 用法

```bash
ioc
```

# 示例

```bash
# 列出全部 Bean
ioc

# 检查某服务是否装配
ioc | grep myService
```
