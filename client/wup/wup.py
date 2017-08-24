#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, os, time, glob
import traceback, subprocess
import argparse, json, httplib, urllib,hashlib, thread

# begin 全局变量
# APPS = {}
WATCHDOG_CHECK = True
ROOT = "/opt"
INIT_TYPE = ""
CONFS = dict(
    host = "walnut.nutz.cn",
    port = 8181,
    zsyncroot = "/gu/root/wup/pkgs/",
    apiroot = "/api/root/wup/v1",
    approot = "/opt",
    godkey = "123456"
)
# End 全局变量


# begin 主函数群
def main():
    # begin 处理命令行参数
    global ROOT
    global WATCHDOG_CHECK
    global INIT_TYPE
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', dest="wuproot")
    parser.add_argument('--type', dest='inittype')
    args = parser.parse_args()
    if args.wuproot :
        ROOT = args.wuproot
    if args.inittype :
        INIT_TYPE = args.inittype
    log.debug("ROOT=" + ROOT)
    reloadConfig()
    log.debug("CONFIG=" + json.dumps(CONFS, indent=2))
    # end 处理命令行参数

    thread.start_new_thread(watchdog, ())

    # begin 事件循环
    while 1 :
        try:
            loop()
        except Exception as e:
            traceback.print_exc()
        WATCHDOG_CHECK = True
        time.sleep(15)
    # end 事件循环

def loop():

    if not CONFS.get("key") :
        re = getJson("/node/init", {"macid":CONFS["macid"], "godkey" : CONFS["godkey"], "type" : INIT_TYPE})
        if re and re.get("key") :
            CONFS.update(re)
            writeConfig()
        return
    remotec = getJson("/node/get", {"macid":CONFS["macid"], "key":CONFS["key"]})
    writeJsonFile(ROOT + "/wup_remote.json", remotec)
    localc = readJsonFile(ROOT + "/wup_local.json")
    if not localc.get("pkgs") :
        localc["pkgs"] = {}

    log.debug("remotec="+json.dumps(remotec))
    if not remotec :
        return
    # 检查各pkg的版本
    for pkg in remotec["pkgs"] :
        pkg_name = pkg["name"]
        pkg_version = pkg.get("version") or "lastest"
        check_resp = getJson("/pkg/info", {"macid":CONFS["macid"], "key":CONFS["key"], "name":pkg_name, "version":pkg_version})
        if not check_resp :
            log.warn("no such pkg=%s version=%s ?" % (pkg_name, pkg_version))
            continue
        if localc["pkgs"].get(pkg_name) and check_resp.get("sha1") == localc["pkgs"][pkg_name].get("sha1") :
            continue
        WATCHDOG_CHECK = False
        dst = ROOT + "/wup/pkgs/"+pkg_name + "/" + pkg_version +".tgz"
        downloadFile("/pkg/get", {"macid":CONFS["macid"], "key":CONFS["key"], "name":pkg_name, "version":pkg_version}, dst, check_resp.get("sha1"))
        _install(dst, pkg_name)
        localc["pkgs"][pkg_name] = check_resp
        writeJsonFile(ROOT + "/wup_local.json", localc)

def watchdog() :
    time.sleep(5)
    while 1 :
        _watchdog()
        time.sleep(3)

def reloadConfig():
    tmp = readJsonFile(ROOT + "/wup_config.json")
    CONFS.update(tmp)
    CONFS["macid"] = _macid()

def writeConfig() :
    writeJsonFile(ROOT + "/wup_config.json", CONFS)

def _install(dst, app) :
    log.debug("install ... " + dst )
    subprocess.check_call("tar -C /tmp -x -f " + dst, shell=1)
    _env = os.environ.copy()
    _env["WUPROOT"] = ROOT
    _env["APPNAME"] = app
    subprocess.check_call("/tmp/update", cwd="/tmp", shell=1, env=_env)
    log.debug("install complete " + dst)

# end 主函数群

# begin 帮助函数
def getJson(uri, params):
    hc = None
    try:
        hc = _http()
        hc.request('GET', CONFS["apiroot"] + uri + "?" + urllib.urlencode(params))
        resp = hc.getresponse()
        if resp.status == 200 :
            data = resp.read()
            if data and data[0] == "{":
                return json.loads(data)
    except Exception, e:
        traceback.print_exc()
    finally :
        if hc :
            hc.close()

def downloadFile(uri, params, dst, sha1) :
    if _sha1(dst) == sha1 :
        log.debug("same sha1 >> " + dst)
        return
    else :
        log.debug("expect %s but %s" % (sha1, _sha1(dst)))
    hc = None
    try:
        log.debug("download file >> " + dst)
        # check zsync first, if prevent pkg exists
        if os.path.exists("/usr/bin/zsync") and os.path.exists(os.path.dirname(dst)) and len(os.listdir(os.path.dirname(dst))) > 0 :
            _pkg_dir = os.path.dirname(dst)
            list_of_files = glob.glob(_pkg_dir + '/*.tgz')
            latest_file = max(list_of_files, key=os.path.getctime)
            hc = _http()
            zsync_url = "http://%s:%d%s/%s/%s.tgz.zsync" % (CONFS["host"], CONFS["port"], CONFS["zsyncroot"], params["name"], params["version"])
            log.debug("zsync URL=" + zsync_url)
            hc.request('GET', zsync_url)
            resp = hc.getresponse()
            if resp.status == 200 :
                log.debug("zsync seem can work >> " + dst)
                hc.close()
                try :
                    _tmp = ROOT + "/wup/pkgs/zsync.tmp"
                    subprocess.check_call(["nohup", "/usr/bin/zsync", "-i", latest_file, "-o", _tmp, zsync_url])
                    if _sha1(_tmp) == sha1 :
                        log.debug("zsync GOOD!")
                        subprocess.check_call(["mv", _tmp, dst])
                        return
                    else :
                        log.debug("zsync done, but sha1 not good?! fallback to simple download")
                except:
                    log.debug("zsync FAIL!!!")
            else :
                log.debug("zsync contrl file not exists?, fallback to simple download")
            
        hc = _http()
        hc.request('GET', CONFS["apiroot"] + uri + "?" + urllib.urlencode(params))
        resp = hc.getresponse()
        if resp.status == 200 :
            if os.path.exists(dst) :
                os.remove(dst)
            elif not os.path.exists(os.path.dirname(dst)):
                os.makedirs(os.path.dirname(dst))
            with open(dst, "w") as f :
                while 1:
                    buf = resp.read(8192*1024)
                    if not buf :
                        break
                    f.write(buf)
            log.debug("Download Complete >> " + dst)
            return
        log.info("WHAT?!!! code=" + resp.status)
    except Exception, e:
        traceback.print_exc()
    finally :
        if hc :
            hc.close()


def _http() :
    hc = httplib.HTTPConnection(CONFS["host"], CONFS["port"], timeout=30)
    hc.__exit__ = hc.close
    return hc

def getHwAddr(ifname):
    if sys.platform == 'win32' :
        return "AABBCCDDEEFF"
    import fcntl, socket, struct
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    info = fcntl.ioctl(s.fileno(), 0x8927,  struct.pack('256s', ifname[:15]))
    return ''.join(['%02X' % ord(char) for char in info[18:24]])

def _macid() :
    try:
        return getHwAddr("eth0")
    except Exception as e:
        return getHwAddr("em1")

def readJsonFile(path) :
    if os.path.exists(path) :
        with open(path) as f :
            return json.loads(f.read())
    return {}

def writeJsonFile(path, vals) :
    with open(path, "w") as f :
        json.dump(vals, f)

def _sha1(path) :
    if not os.path.exists(path) :
        log.debug("not exists " + path)
        return

    m = hashlib.sha1()
    with open(path, "rb") as f:
        while True:
            data = f.read(8192)
            if not data:
                break
            m.update(data)
    return m.hexdigest()

# end 帮助函数

# begin 日志

import time
import os,os.path
import sys
import logging, logging.handlers
import subprocess
import threading
from compiler.ast import Exec

log = logging.getLogger()
log.setLevel(logging.DEBUG)
fh = logging.handlers.RotatingFileHandler("/dev/shm/watchdog.log", maxBytes=16*1024*1024,
                                          backupCount=1, encoding="UTF-8")
fh.setLevel(logging.DEBUG)
formatter = logging.Formatter('$: %(asctime)s > %(levelname)-5s > %(filename)s:%(lineno)s > %(message)s')
fh.setFormatter(formatter)
log.addHandler(fh)

td = {}

base = ROOT

def _watchdog():
    global base
    base = ROOT
    if not WATCHDOG_CHECK :
        return
    try:
        for app in os.listdir(base) :
            if app in td.keys() and td[app].isAlive() :
                continue
            if "-" in app or "_" in app or "." in app:
                continue
            start_cmd = "%s/%s/run.sh" % (base, app)
            stop_cmd  = "%s/%s/stop.sh"  % (base, app)
            if os.path.exists(start_cmd) and os.path.exists(stop_cmd) :
                log.info("add new app " + app)
                t = ExecThread(os.path.dirname(start_cmd), app,  start_cmd, stop_cmd)
                td[app] = t
                t.start()
        #subprocess.call(["uptime"])
        time.sleep(1)
    except:
        log.info("bad bad", exc_info=True)

class ExecThread(threading.Thread):

    def __init__(self, app_root, app, start_cmd, stop_cmd):
        threading.Thread.__init__(self)
        self.app_root  = app_root
        self.start_cmd = start_cmd
        self.stop_cmd  = stop_cmd
        self.app = app
        self.daemon    = True

    def run(self):
        log.info("restart app --> " + self.app)
        subprocess.call(["chmod", "777", self.stop_cmd])
        subprocess.call(["chmod", "777", self.start_cmd])
        _env = os.environ.copy()
        _env["WUPROOT"] = ROOT
        _env["APPNAME"] = self.app
        subprocess.call(self.stop_cmd, cwd=self.app_root, close_fds=True, shell=1, env=_env)
        time.sleep(5)
        with open("/var/log/"+self.app+".log", "w") as f:
            subprocess.call(self.start_cmd, cwd=self.app_root, close_fds=True, shell=1, env=_env, stdout=f, stderr=f)

# end 日志

if __name__ == '__main__':
    main()
