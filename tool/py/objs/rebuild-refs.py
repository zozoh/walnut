#!/usr/bin/python3
# -*- coding: utf-8 -*-
#
"""
依赖组件
pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple pymongo
pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple redis
"""
import sys, os
import hashlib, json
from getopt  import getopt
from time    import time
from shutil  import copyfile
from redis   import Redis
from pymongo import MongoClient
#===================================================
usage = '''
Walnut 对象索引恢复程序

# 参数表

    --limit 10      # 最多10个桶，0 表示不限，默认 10
    --skip  0       # 跳过前面多少个桶，默认 0
    --conf  {..}    # 指定数据库连接方式，以及过滤条件等

# 调用示例

    python rebuild-refs --limit 5 --skip 10 --conf rebuild.json
'''
#===================================================
VERSION = "1.0"
def printVersion():
  print(VERSION)
def printUsage():
  print(usage)
#===================================================
# 获取命令行参数
limit = 10
skip  = 0
config = {}
flt = None
opts, args = getopt(sys.argv[1:], "hv", [
    "version","limit=","skip=", "conf="
  ])
for op, val in opts:
  if "-h" == op:
    printUsage()
    sys.exit()
  elif "--version" == op or "-v" == op:
    printVersion()
    sys.exit()
  elif "--limit" == op:
    limit = int(val)
  elif "--skip" == op:
    skip = int(val)
  elif "--conf" == op:
    fph = os.path.abspath(val)
    with open(fph, "r") as f:
      txt = f.read()
      config = json.loads(txt)
#===================================================
# Mongo 数据库连接
_mongo = config['mongo']
# Redis 数据库连接
_redis = config['redis']
# 数据过滤条件
flt = config['filter']
#===================================================
print("#" * 60)
print("#")
print("#   Walnut 索引恢复程序")
print("#   > 根据 MongoDB 恢复 Redis 索引")
print("#  ", VERSION)
print("#")
print("#" * 60)
if flt:
  print("过滤条件：")
  print(json.dumps(flt, indent=3))
else:
  print("~ 无过滤条件 ~")
print("#" * 60)
print("# MongoDB:")
print(json.dumps(_mongo, indent=3))
print("#" * 60)
print("# Redis:")
print(json.dumps(_redis, indent=3))
print("#" * 60)
#===================================================
#
# 连接 MongoDB
#
monUrl = 'mongodb://%s:%d/'%(_mongo['host'], _mongo['port'])
print("准备连接 MongoDB: ", monUrl)
monCli = MongoClient(monUrl)
monDB  = monCli[_mongo['db']]
print("...成功的连接了 MongoDB")
#===================================================
#
# 连接 Redis
#
print("准备连接 Redis: ", _redis['host'], _redis['port'])
red = Redis(host=_redis['host'], port=_redis['port'], db=_redis['db'])
print("...成功的连接了 Redis")
#===================================================
# 准备计数
now = time() * 1000
#===================================================
#
# 根据 MONGO 的索引寻找桶
#
print("\n开始从 MongoDB 查找索引 ...")
print("-" * 60)
count = 0
sha1_count = 0
objs = monDB['obj'].find(
  filter = flt,
  skip   = skip,
  limit  = limit
)
#-----------------------------------------------------
for obj in objs:
  oid  = obj.get('id')
  onm  = obj.get('nm')
  sha1 = obj.get('sha1')
  race = obj.get('race')
  #-------------------------------------
  # 看来有些对象是坏的
  if not oid or not onm:
    continue
  #-------------------------------------
  # 打印一下
  print("\n%d. %s %s : '%s'"%(count, oid, sha1, onm))
  count += 1
  #-------------------------------------
  # 无视
  if "DIR" == race or not sha1:
    print("  ~~ skip ~~")
    continue

  print("  Add to Redis")
  sha1_count += 1
  #-------------------------------------
  # 在 Redis 里记录索引
  rkey = "io:ref:%s"%(sha1)
  red.sadd(rkey, oid)
  #-------------------------------------
  # 计数
# end of for
#===================================================
# 结束
stop = time() * 1000
duInMs = stop - now
duInS  = int(round(duInMs/1000))
#
# 打印一下
#
print("-"*60)
print("Found %d objs (%d files)"%(count, sha1_count))
print("-"*60)
print("All done in %d seconds (%dms)\n"%(duInS, duInMs))