命令简介
======= 

`aliyunoss meta` 查询/更新OSS文件的元数据
    

用法
=======

```bash
aliyunoss [$name] meta 
	osspath     # OSS上的路径 
	-u $meta    # 需要更新的元数据,可选
```

基本用法
========

查询元数据

```bash
:> aliyunoss testabc meta test/abc.txt
{
   "Accept-Ranges": "bytes",
   Connection: "keep-alive",
   "Content-Length": 4,
   "Content-MD5": "C+6JsHokjifIP8PVlRITwQ==",
   "Content-Type": "text/plain",
   Date: "2020-05-03 10:16:04",
   ETag: "0BEE89B07A248E27C83FC3D5951213C1",
   "Last-Modified": "2020-05-02 23:14:45",
   Server: "AliyunOSS",
   "x-oss-hash-crc64ecma": "15846253086188071303",
   "x-oss-object-type": "Normal",
   "x-oss-request-id": "5EAE29645369E83635F19090",
   "x-oss-server-time": "69",
   "x-oss-storage-class": "Standard"
}
```

更新元数据

```bash
:> aliyunoss testabc meta test/abc.txt -u 'content-type:"application/json"'
{
   "Accept-Ranges": "bytes",
   Connection: "keep-alive",
   "Content-Length": 4,
   "Content-MD5": "C+6JsHokjifIP8PVlRITwQ==",
   "Content-Type": "application/json", # 这里变化了
   Date: "2020-05-03 10:16:04",
   ETag: "0BEE89B07A248E27C83FC3D5951213C1",
   "Last-Modified": "2020-05-02 23:14:45",
   Server: "AliyunOSS",
   "x-oss-hash-crc64ecma": "15846253086188071303",
   "x-oss-object-type": "Normal",
   "x-oss-request-id": "5EAE29645369E83635F19090",
   "x-oss-server-time": "69",
   "x-oss-storage-class": "Standard"
}
```