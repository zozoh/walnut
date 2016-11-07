#!/usr/bin/python
# -*- coding: utf-8 -*-
import sys, os, time
import traceback, subprocess
import argparse, json, httplib, urllib

# begin 全局变量
APPS = {}
ROOT = "/opt"
CONFS = dict(
    host = "192.168.88.133",
    apiroot = "/api/root/wup/v1",
    approot = "/opt",
    godkey = "123456"
)
# End 全局变量


# begin 主函数群
def main():
    # begin 处理命令行参数
    global ROOT
    if len(sys.argv) > 1 :
        ROOT = sys.argv[1]
    reloadConfig()
    print "CONFIG=", json.dumps(CONFS, indent=2)
    # end 处理命令行参数

    # begin 事件循环
    while 1 :
        try:
            loop()
        except Exception as e:
            traceback.print_exc()
        time.sleep(15)
    # end 事件循环

def loop():
    if not CONFS.get("key") :
        re = getJson("/node/init", {"macid":CONFS["macid"], "godkey" : CONFS["godkey"]})
        if re and re.get("key") :
            CONFS.update(re)
            writeConfig()
    remotec = getJson("/node/get", {"macid":CONFS["macid"], "key":CONFS["key"]})
    writeJsonFile(ROOT + "/wup_remote.json", remotec)
    localc = readJsonFile(ROOT + "/wup_local.json")
    if not localc.get("pkgs") :
        localc["pkgs"] = {}

    print remotec
    # 检查各pkg的版本
    for pkg in remotec["pkgs"] :
        pkg_name = pkg["name"]
        pkg_version = pkg.get("version") or "lastest"
        check_resp = getJson("/pkg/info", {"macid":CONFS["macid"], "key":CONFS["key"], "name":pkg_name, "version":pkg_version})
        if not check_resp :
            warn("no such pkg=%s version=%s ?" % (pkg_name, pkg_version))
            continue
        if localc["pkgs"].get(pkg_name) and check_resp.get("sha1") == localc["pkgs"][pkg_name].get("sha1") :
            continue
        dst = ROOT + "/wup/pkgs/"+pkg_name + "/" + pkg_version +".tgz"
        downloadFile("/pkg/get", {"macid":CONFS["macid"], "key":CONFS["key"], "name":pkg_name, "version":pkg_version}, dst)
        _install(dst)
        localc["pkgs"][pkg_name] = check_resp
        writeJsonFile(ROOT + "/wup_local.json", localc)

def reloadConfig():
    tmp = readJsonFile(ROOT + "/wup_config.json")
    CONFS.update(tmp)
    CONFS["macid"] = _macid()

def writeConfig() :
    writeJsonFile(ROOT + "/wup_config.json", CONFS)

def _install(dst) :
    subprocess.check_call("tar -C /tmp -x -f " + dst, shell=1)
    subprocess.check_call("WUPROOT=%s /tmp/update" % (ROOT), cwd="/tmp", shell=1)

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

def downloadFile(uri, params, dst) :
    hc = None
    try:
        debug("download file >> " + dst)
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
            return
        print "WHAT?!!!"
    except Exception, e:
        traceback.print_exc()
    finally :
        if hc :
            hc.close()


def _http() :
    hc = httplib.HTTPConnection(CONFS["host"], 8080, timeout=30)
    hc.__exit__ = hc.close
    return hc

def getHwAddr(ifname):
    if sys.platform == 'win32' :
        return "AABBCCDDEEFF"
    import fcntl, socket, struct
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    info = fcntl.ioctl(s.fileno(), 0x8927,  struct.pack('256s', ifname[:15]))
    return ''.join(['%02x' % ord(char) for char in info[18:24]])

def _macid() :
    try:
        return getHwAddr("eth0")
    except Exception as e:
        return getHwAddr("enp0s3")

def readJsonFile(path) :
    if os.path.exists(path) :
        with open(path) as f :
            return json.loads(f.read())
    return {}

def writeJsonFile(path, vals) :
    with open(path, "w") as f :
        json.dump(vals, f)

def debug(msg):
    print "DEBUG", msg

def info(msg):
    print "INFO ", msg

def warn(msg):
    print "WARN ", msg

def error(msg):
    print "ERROR", msg



# end 帮助函数

if __name__ == '__main__':
    main()
