#!/usr/bin/python3
# -*- coding: utf-8 -*-
import sys, os
import hashlib, json
from getopt  import getopt
from time    import time
from shutil  import copyfile
from redis   import Redis
from pymongo import MongoClient
#===================================================
usage = '''
Walnut 数据迁移程序

# 参数表

    --limit 10      # 最多10个桶，0 表示不限，默认 10
    --skip  0       # 跳过前面多少个桶，默认 0
    --flt   {..}    # 指定一个过滤条件

# 调用示例

    python transdata.py --limit 5 --skip 10 --bunb
'''
#===================================================
VERSION = "1.0"
def printVersion():
  print(VERSION)
def printUsage():
  print(usage)
#===================================================
# 桶的主目录: 这个是 Walnut 老数据的桶目录
# 下面就是 om/t4djk9umhq0r0fklhveq1sge/ 这样的桶文件夹了
phBucketHome = "D:/DB/walnut/rt/bucket"
#
# 目标目录: 要将上面的桶目录迁移的目标目录
phTargetHome = "D:/DB/walnut/io/buck"
#
# Mongo 数据库连接
_mongo = {
  "host": "127.0.0.1",
  "port": 27017,
  "user": None,
  "passwd": None,
  "db": "walnut"
}
# Redis 数据库连接
_redis = {
  "host": "127.0.0.1",
  "port": 6379,
  "user": None,
  "passwd": None,
  "db": 0
}
#===================================================
# 获取命令行参数
limit = 10
skip  = 0
flt   = None
opts, args = getopt(sys.argv[1:], "hv", [
    "version","limit=","skip=", "flt="
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
  elif "--flt" == op:
    # 直接就是 JSON
    if val.startswith("{") and val.endswith("}"):
      flt = json.loads(val)
    # 否则当作文件
    else:
      fph = os.path.abspath(val)
      with open(fph, "r") as f:
        txt = f.read()
        flt = json.loads(txt)
#===================================================
print("#" * 60)
print("#")
print("#   Walnut 数据迁移程序")
print("#  ", VERSION)
print("#")
print("#" * 60)
if flt:
  print("过滤条件：")
  print(json.dumps(flt, indent=3))
else:
  print("~ 无过滤条件 ~")
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
# 收集的丢失的桶列表
lostBucks = list()
emptyBucks = list()
sha1Cache = {}

now = time() * 1000
#===================================================
#
# 快速得到某个文件的 SHA1 （带缓冲）
def GetFileSha1(file):
  # 命中
  if file in sha1Cache:
    return sha1Cache[file]
  # 计算吧
  with open(file,'rb') as f:
    SHA1 = hashlib.sha1()
    while True:
      bs = f.read(8192)
      if bs:
        SHA1.update(bs)
      else:
        break
    sha1 = SHA1.hexdigest()
    sha1Cache[file] = sha1
    return sha1
#===================================================
# 
# 追加文件，将 src 文件追加到 target 文件末尾
def AppendFile(target, src):
  # 打开目标文件的追加写
  fTa = open(target, "ab")
  fSr = open(src, "rb")
  # 因为是桶块，也没必要循环了，一次读完
  try:
    bb = fSr.read()
    fTa.write(bb)
  finally:
    fTa.close()
    fSr.close()
#===================================================
#
# 转换桶文件的帮助方法
#
def TransBucket(buck):
  sha1  = buck['sha1']
  data  = buck['data']
  size  = buck['sz']
  files = buck['files']
  print(" = Trans %d Bucket Blocks ..."%(len(files)))
  # 原始目录为空，诡异啊，记录一下
  if 0 == len(files):
    print(" = 空桶目录，跳过！")
    emptyBucks.append(data)
    return
  # 目标桶文件路径
  phTa = "%s/%s/%s"%(phTargetHome, sha1[0:4], sha1[4:])
  phDr = os.path.dirname(phTa)
  # 如果目标桶文件存在，那么校验一下 sha1
  if os.path.exists(phTa):
    print(" = 目标桶已经存，开始校验 ... (%s) : %dbytes "%(sha1, size))
    taSha1 = GetFileSha1(phTa)
    if sha1 == taSha1:
      print(" = OK = 校验通过")
      return
    else:
      print(" = KO ! 校验失败，移除这个文件")
      del sha1Cache[phTa]
      os.remove(phTa)
  
  # 确保目标桶的散列目录存在
  if not os.path.exists(phDr):
    print(" = 为目标桶建立父路径:", phDr)
    os.makedirs(phDr)

  # 如果目标桶不存在，或者校验失败，那么copy第一个桶过去
  print(" = 先 Copy 第一个桶块")
  f0 = files[0]
  copyfile(f0, phTa)
  print(" =  ... OK")

  # 如果还有剩余的桶块，附加
  if len(files) > 1:
    print(" = 依次附加其余的桶块")
    for fn in files[1:]:
      name = os.path.basename(fn)
      print(" = + %3s >> %s"%(name, phTa))
      AppendFile(phTa, fn)
  print(" =  ~~~ 转换完毕 ~~~ =")
#===================================================
#
# 根据 MONGO 的索引寻找桶
#
print("\n开始从 MongoDB 查找索引 ...")
print("-" * 60)
count = 0
colls = monDB['bucket'].find(
  filter = flt,
  sort   = [("_id", 1)],
  skip   = skip,
  limit  = limit
)
objs  = monDB['obj']
#-----------------------------------------------------
for bu in colls:
  oid  = None
  data = bu['id']     # 桶 ID
  buNb = bu['b_nb']   # 桶块数量
  buSz = bu['sz']     # 桶总大小
  sha1 = bu['sha1']   # 桶指纹
  refs = 0
  if 'refer' in bu:
    refs = bu['refer']  # 引用数量
  #-------------------------------------
  index = skip + count
  #-------------------------------------
  # 查询一下这个桶对应的对象 ID
  obj = objs.find_one({"data": data})
  if obj is None:
    lostBucks.append(data)
    continue
  else:
    oid = obj['id']
  #-------------------------------------
  # 打印一下
  print("\n%d. %s (B)x%d : '%s' <-%d"%(index, data, buNb, sha1, refs))
  print(" = OID: %s"%(oid))
  # 计算桶文件
  path = "%s/%s/%s"%(phBucketHome, data[0:2], data[2:])
  files = list()
  #-------------------------------------
  for i in range(buNb):
    aph = "%s/%d"%(path, i)
    print("      %3d) %s : %s"%(i, os.path.exists(aph), aph))
    files.append(aph)
  #-------------------------------------
  # 在 Redis 里记录索引
  rkey = "io:ref:%s"%(sha1)
  red.sadd(rkey, oid)
  #-------------------------------------
  # 准备桶对象，进行转换
  buck = {
    "index": index,
    "oid"  : oid,
    "data" : data,
    "nb"   : buNb,
    "sz"   : buSz,
    "sha1" : sha1,
    "refs" : refs,
    "files": files
  }
  TransBucket(buck)
  #-------------------------------------
  # 计数
  count += 1
# end of for
#===================================================
#
# 收集全部的桶文件列表
#
def GetAllBucketFiles(path, bucks):
  filelist = os.listdir(path)
  for filename in filelist:
    ph = os.path.join(path, filename)
    # 目录: 进入
    if os.path.isdir(ph):  
      GetAllBucketFiles(ph, bucks)
    # 文件：记录
    elif os.path.isfile(ph):
      aph = ph.replace('\\', '/')
      data = aph[len(phBucketHome):].replace('/', '')
      index = len(bucks)
      bo = {
        'index': index,
        'data' : data,
        'path' : aph
      }
      # 打印搜索到的桶
      print("%d. %s : %s"%(index, data, aph))
      # 计入
      bucks.append(bo)
# end getAllBucketFiles
 
#
# 收集桶文件
# 
#getAllBucketFiles(phBucketHome, buckets) 
#
# 结束
stop = time() * 1000
duInMs = stop - now
duInS  = int(round(duInMs/1000))
#
# 打印一下
#
print("-"*60)
print("Found %d buckets"%(count))
print("Lost Buckets x(%d)"%(len(lostBucks)))
i = 0
for lbu in lostBucks:
  print(" %d. %s"%(i, lbu))
  i += 1
print("-"*60)
print("SHA1 bucket x(%d)"%(len(sha1Cache)))
print("-"*60)
print("All done in %d seconds (%dms)\n"%(duInS, duInMs))