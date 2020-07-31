import os
import time
import redis
from pymongo import MongoClient
#===================================================
# 桶的主目录
phBucketHome = "D:/DB/walnut/rt/bucket"
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
#
# 连接 MongoDB
#
monUrl = 'mongodb://%s:%d/'%(_mongo['host'], _mongo['port'])
print("准备连接 MongoDB: ", monUrl)
monCli = MongoClient(monUrl)
monDB  = monCli[_mongo['db']]
print("成功连接了 MongoDB")
#===================================================
#
# 连接 Redis
#
print("准备连接 Redis: ")
red = redis.Redis(host=_redis['host'], port=_redis['port'], db=_redis['db'])
print("成功连接了 Redis")
#===================================================
# 收集的桶对象列表
buckets = list()
lostBucks = list()
now = time.time() * 1000
#===================================================
#
# 根据 MONGO 的索引寻找桶
#
print("开始从 MongoDB 查找索引 ...")
count = 0
colls = monDB['bucket'].find()
objs  = monDB['obj']
#-----------------------------------------------------
for bu in colls:
  oid  = "!!!LOST!!!"
  data = bu['id']     # 桶 ID
  buNb = bu['b_nb']   # 桶块数量
  buSz = bu['sz']     # 桶总大小
  sha1 = bu['sha1']   # 桶指纹
  refs = 0
  if 'refer' in bu:
    refs = bu['refer']  # 引用数量
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
  print("\n%d. %s (B)x%d : '%s' <-%d"%(count, data, buNb, sha1, refs))
  print(" = OID: %s"%(oid))
  # 计算桶文件
  path = "%s/%s/%s"%(phBucketHome, data[0:2], data[2:])
  files = list()
  #-------------------------------------
  for i in range(buNb):
    aph = "%s/%d"%(path, i)
    print("      %d) %s : %s"%(i, os.path.exists(aph), aph))
    files.append(aph)
  #-------------------------------------
  # 在 Redis 里记录索引
  rkey = "io:ref:%s"%(sha1)
  red.sadd(rkey, oid)
  #-------------------------------------
  # 记录到桶里
  buckets.append({
    "index": count,
    "oid"  : oid,
    "data" : data,
    "nb"   : buNb,
    "sz"   : buSz,
    "sha1" : sha1,
    "refs" : refs,
    "files": files
  })
  # 计数
  count += 1
# end of for
#===================================================
#
# 收集全部的桶文件列表
#
def getAllBucketFiles(path, bucks):
  filelist = os.listdir(path)
  for filename in filelist:
    ph = os.path.join(path, filename)
    # 目录: 进入
    if os.path.isdir(ph):  
      getAllBucketFiles(ph, bucks)
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
stop = time.time() * 1000
duInMs = stop - now
duInS  = int(round(duInMs/1000))
#
# 打印一下
#
print("-"*60)
print("Found %d buckets"%(len(buckets)))
print("Lost Buckets x(%d)"%(len(lostBucks)))
i = 0
for lbu in lostBucks:
  print(" %d. %s"%(i, lbu))
  i += 1
print("-"*60)
print("All done in %d seconds (%dms)"%(duInS, duInMs))