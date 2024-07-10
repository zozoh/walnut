import pymongo
import redis

# 连接到本地的MongoDB服务器
_mog = pymongo.MongoClient("mongodb://127.0.0.1:27017/")
_red = redis.StrictRedis(host="127.0.0.1", port=6379, db=0)

# 选择数据库和集合
db = _mog["walnut"]
co = db["obj"]

# 遍历集合中的前100条记录并打印
query = {"race": "FILE", "len": {"$gt": 0}, "sha1": {"$ne": ""}}

cu = co.find(query).limit(1000000)
for i, obj in enumerate(cu):
    id = obj.get("id")
    race = obj.get("race")
    tp = obj.get("tp")
    nm = obj.get("nm")
    len = obj.get("len")
    sha1 = obj.get("sha1")

    print(
        "%5d) id: %s <%s:%-4s> %8s bytes {%s} : %s" % (i, id, race, tp, len, sha1, nm)
    )

    # 将对象的id加入到以sha1为键的集合中
    ref_key = f"io:ref:{sha1}"
    _red.sadd(ref_key, id)
