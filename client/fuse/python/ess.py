#!/usr/bin/python
# -*- coding: utf-8 -*-

import requests
from fuse import FUSE, FuseOSError, Operations, LoggingMixIn
from errno import *
from urllib import urlencode
from time import time
from stat import *
from contextlib import closing

MOUNT_POINT = ""

class ESS(LoggingMixIn, Operations):
    '''
    
    '''

    def __init__(self, seid, host="127.0.0.1", port=8080):
    	self.seid = seid
        self.host = host
        self.port = port
        self.session = requests.Session()
        self.session.headers.update({"SEID":seid})
        
    def _url(self, uri):
        return "http://%s:%d/fuse/%s" % (self.host, self.port, uri)
        
    def _get(self, uri, params, do_print=True):
        print self._url(uri), params
        resp = self.session.get(self._url(uri), params=params, headers={"Cookie":"SEID="+self.seid})
        if do_print :
            print "resp", resp.status_code, len(resp.content)
        if resp.status_code == 404 :
            raise FuseOSError(ENOENT)
        if resp.status_code == 403:
            raise FuseOSError(EEXIST)
        if resp.status_code != 200 :
            raise FuseOSError(EBUSY)
        return resp

    def init(self, root, conn):
        conn = conn.contents
        print(dir(conn))
        print(conn.proto_major)
        print(conn.proto_minor)
        print(conn.capable)
        print(conn.want)
        conn.want |= 8

    def chmod(self, path, mode):
        with closing(self._get("chmod", dict(path=path,mode=mode))) as resp :
            if resp.status_code != 200 :
                pass
        return 0

    def chown(self, path, uid, gid):
        return 0

    def create(self, path, mode):
        with closing(self._get("create", dict(path=path))) as resp :
            if resp.status_code == 200 :
                return int(resp.content)
        return 0

    def destroy(self, path):
        return 0

    def getattr(self, path, fh=None):
        if path == "/" :
            return dict(st_mode=(S_IFDIR | 0755), st_nlink=1,
                                st_size=0, st_ctime=time(), st_mtime=time(),
                                st_atime=time())
        with closing(self._get("getattr", dict(path=path))) as resp :
            return resp.json()

    def mkdir(self, path, mode):
        with closing(self._get("mkdir", dict(path=path))) as resp :
            pass
        return 0

    def read(self, path, size, offset, fh):
        with closing(self._get("read", dict(path=path, size=size, offset=offset,fh=fh), False)) as resp :
            if resp.status_code != 200 :
                raise FuseOSError(EBUSY)
        return resp.content

    def readdir(self, path, fh):
        #if path == "/" :
        #    return ["root", "home"]
        with closing(self._get("readdir", dict(path=path))) as resp :
            return resp.json()

    def readlink(self, path):
        with closing(self._get("readlink", dict(path=path))) as resp :
            re = resp.content
            if re :
                return MOUNT_POINT + re

    def rename(self, target, source):
        with closing(self._get("rename", dict(source=target, target=source))) as resp :
            return 0

    def rmdir(self, path):
        with closing(self._get("rmdir", dict(path=path))) as resp :
            return 0

    def open(self, path, flags):        
        with closing(self._get("open", dict(path=path, flags=flags))) as resp :
            if resp.status_code == 200:
                return int(resp.content)
        return 0

    def release(self, path, fh):
        with closing(self._get("release", dict(path=path, fh=fh))) as resp :
            return 0

    def symlink(self, target, source):
        with closing(self._get("symlink", dict(target=target, source=source))) as resp :
            return 0

    def truncate(self, path, length, fh=None):
        with closing(self._get("truncate", dict(path=path, length=length, fh=fh))) as resp :
            pass
        return 0

    def unlink(self, path):
        with closing(self._get("unlink", dict(path=path))) as resp :
            return 0

    #def utimens(self, path, times=None):
    #    return self._get("utimens", dict(path=path)).json()

    def write(self, path, data, offset, fh):
        URI = "write?offset=%d&size=%d&path=%s&fh=%s" % (offset, len(data), path, str(fh))
        headers = {"content-type":"application/octet-stream", "Cookie":"SEID=" + self.seid}
        print URI, headers
        with closing(self.session.post(self._url(URI), data=data, headers=headers)) as resp :
            if resp.status_code == 200:
                return resp.json()
            else:
                print "resp", resp.status_code, len(resp.content)
        raise FuseOSError(EBUSY)

if __name__ == '__main__':
    import sys
    argv = sys.argv
    if len(argv) != 5:
        print('usage: %s <seid> <host> <port> <mountpoint>' % argv[0])
        exit(1)
    MOUNT_POINT = argv[4]
    import  logging
    logging.basicConfig(filename='ess.log',level=logging.INFO)
    fuse = FUSE(ESS(argv[1], argv[2], int(argv[3])), argv[4], foreground=1, debug=0
					#,entry_timeout=5, attr_timeout=5
					, big_writes=True
					, kernel_cache=True
					, auto_cache=True
					#,large_read=True
					#, flags=8
					)
    
    
