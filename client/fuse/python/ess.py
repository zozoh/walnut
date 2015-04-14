#!/usr/bin/python
# -*- coding: utf-8 -*-

import requests
from fuse import FUSE, FuseOSError, Operations, LoggingMixIn
from errno import *
from urllib import urlencode
from time import time
from stat import *
from contextlib import closing

class ESS(LoggingMixIn, Operations):
    '''
    
    '''

    def __init__(self, host="127.0.0.1", port=8080, user="root", password="123456"):
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.session = requests.Session()
        self.session.auth = (user, password)
        resp = self._get("getattr", dict(path="/root"))
        if resp.status_code != 200 :
            raise FuseOSError(EACCES)
        resp.close()
        print resp.cookies
        
    def _url(self, uri):
        return "http://%s:%d/fuse/%s" % (self.host, self.port, uri)
        
    def _get(self, uri, params, do_print=True):
        print self._url(uri), params
        resp = self.session.get(self._url(uri), params=params)
        if do_print :
            print "resp", resp.status_code, len(resp.content)
        if resp.status_code == 404 :
            raise FuseOSError(ENOENT)
        if resp.status_code == 403:
            raise FuseOSError(EEXIST)
        if resp.status_code != 200 :
            raise FuseOSError(EBUSY)
        return resp

    def chmod(self, path, mode):
        return 0

    def chown(self, path, uid, gid):
        return 0

    def create(self, path, mode):
        with closing(self._get("create", dict(path=path))) as resp :
            if resp.status_code != 200 :
                raise FuseOSError(EEXIST)
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
        with closing(self._get("read", dict(path=path, size=size, offset=offset), False)) as resp :
            if resp.status_code != 200 :
                raise FuseOSError(EBUSY)
        return resp.content

    def readdir(self, path, fh):
        if path == "/" :
            return ["root", "home"]
        with closing(self._get("readdir", dict(path=path))) as resp :
            return resp.json()

    #def readlink(self, path):
    #    return self.sftp.readlink(path)

    def rename(self, source, target):
        with closing(self._get("rename", dict(source=source, target=target))) as resp :
            return 0

    def rmdir(self, path):
        with closing(self._get("rmdir", dict(path=path))) as resp :
            return 0

    def symlink(self, source, target):
        with closing(self._get("symlink", dict(target=target, source=source))) as resp :
            return 0

    def truncate(self, path, length, fh=None):
        with closing(self._get("truncate", dict(path=path, length=length))) as resp :
            pass
        #return 0

    def unlink(self, path):
        with closing(self._get("unlink", dict(path=path))) as resp :
            return 0

    def utimens(self, path, times=None):
        pass
        #return self._get("utimens", dict(path=path)).json()

    def write(self, path, data, offset, fh):
        URI = "write?offset=%d&size=%d&path=%s" % (offset, len(data), path)
        headers = {"content-type":"application/octet-stream"}
        with closing(self.session.post(self._url(URI), data=data, headers=headers)) as resp :
            if resp.status_code == 200:
                return resp.json()
        raise FuseOSError(EBUSY)

if __name__ == '__main__':
    import sys
    argv = sys.argv
    if len(argv) != 6:
        print('usage: %s <host> <port> <user> <password> <mountpoint>' % argv[0])
        exit(1)
    import  logging
    logging.basicConfig(filename='ess.log',level=logging.INFO)
    fuse = FUSE(ESS(argv[1], int(argv[2]), argv[3], argv[4]), argv[5], foreground=True)
    
    