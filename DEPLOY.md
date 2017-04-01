Walnut - 部署指南
===


## 安装脚本(ubuntu 14.04)

```
apt-get update
#apt-get upgrade -y
apt-get install -y supervisor curl

cd /opt
rm wup.py
wget http://walnut.nutz.cn/dw/wup.py
chmod a+x wup.py
```

## 创建wup自启动

文件路径 /etc/supervisor/conf.d/wup.conf

```
[program:wup]
command=python /opt/wup.py
directory=/opt
priority=10
numprocs=1
autostart=true
autorestart=true
```

## 初始化wup启动

```
python /opt/wup.py --type strato
```

## 安装监控

```
CI_LICENSE_KEY=UwdWU1gEAVRe5ceWGFcUXAocXxa35cBZD0lTAwRVSe49aAQPTAFUHANU0f01BFVIUQYYUlw= bash -c "$(curl -L https://download.oneapm.com/oneapm_ci_agent/install_agent.sh)"
```



