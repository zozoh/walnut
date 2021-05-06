#!/usr/bin/python3

import os, subprocess, shutil, getopt, sys

proj_name = (os.environ.get("PROJ_NAME") or "walnut")

def opt_clean():
    if os.path.exists("build"):
        shutil.rmtree("build")
    if os.path.exists("target"):
        shutil.rmtree("target")

def opt_build():
    if sys.platform == "win32":
        subprocess.check_call(["mvn", "package", "-Dmaven.test.skip=true", "dependency:copy-dependencies"], shell=True)
    else:
        subprocess.check_call(["/opt/maven/bin/mvn package -Dmaven.test.skip=true dependency:copy-dependencies"], shell=True)

def t_opt_lessc(rootdir):
    for root, dirs, files in os.walk(rootdir) :
        if root.find("font-awesome") >= 0 :
            continue
        for name in files :
            if name.startswith("_"):
                continue
            if name.endswith(".less"):
                path = os.path.join(root, name)
                print("lessc>> ", path)
                cmds = []
                if sys.platform == "win32" :
                    cmds += ["cmd.exe", "/c"]
                cmds += ["lessc", "--include-path=" + os.path.abspath("ROOT/rs/theme/less"), path, path[:-4] + "css"]
                subprocess.check_call(cmds, shell=True)

def t_opt_sassc(rootdir):
    print(">> " + rootdir)
    if not os.path.exists(rootdir) :
        return
    for root, dirs, files in os.walk(rootdir) :
        if root.find("fontawesome") >= 0 :
            continue
        for name in files :
            #print(">> " + name)
            if name.startswith("_"):
                continue
            if name.endswith(".scss"):
                path = os.path.join(root, name)
                print("sassc>> ", path)
                cmds = []
                if sys.platform == "win32" :
                    cmds += ["cmd.exe", "/c"]
                cmds += ["lessc", "-I", os.path.abspath("../titanium/src/theme"), path, path[:-4] + "css"]
                subprocess.check_call(cmds, shell=True)

def opt_lessc():
    t_opt_lessc("ROOT")

def opt_sassc():
    t_opt_sassc("src")
    t_opt_sassc("ROOT")

def opt_wbuild():

    opt_build()
    if os.path.exists("build/wzip"):
        shutil.rmtree("build/wzip")
    os.makedirs("build/wzip/")

    # 开始拷贝数据
    shutil.copytree("target/classes/", "build/wzip/classes/")
    shutil.copytree("WebContent/", "build/wzip/WebContent/")
    shutil.copytree("ROOT/", "build/wzip/ROOT/")
    shutil.copytree("target/dependency/", "build/wzip/libs/")

    # 添加web.allow
    web_allows = ""
    for root, dirs, files in os.walk("build/wzip/WebContent/"):
        if root.find("WEB-INF") >= 0 :
            continue
        for name in files:
            path = os.path.join(root, name)[len("build/wzip/WebContent/"):]
            web_allows += path + "\r\n"
    with open("build/wzip/web.allows", "w") as f :
        f.write(web_allows)

    # 写入启动脚本
    tmpWin32 = '''java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp "classes"'''
    tmpUnix = '''touch classes/web_local.properties;
                 java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp "classes"'''
    tmpWin32 += " org.nutz.web.WebLauncher"
    with open('build/wzip/start.bat', "w") as f:
        f.write(tmpWin32)

    tmpUnix += " org.nutz.web.WebLauncher"
    with open('build/wzip/start.sh', "w") as f :
        f.write(tmpUnix)
    os.chmod('build/wzip/start.sh', 0o755)

    with open('build/wzip/stop.sh', "w") as f:
        f.write('''#!/bin/bash
        if [ -e /var/log/''' + proj_name + '''.pid]; then
            kill `cat /var/log/''' + proj_name + '''.pid`
        else
            killall java
        fi
    ''')
    os.chmod('build/wzip/stop.sh', 0o755)


    with open('build/wzip/run.sh', 'w') as f:
        f.write('''
cd "$(dirname "$0")"
cp $WUPROOT/web_local.properties classes/web_local.properties
cp $WUPROOT/web_local.properties web_local.properties
export JAVA_HOME=$WUPROOT/jdk
export PATH=$JAVA_HOME/bin:$PATH
touch web_local.properties
export WL_PID_PATH=/var/run/''' + proj_name + '''.pid
mkdir /var/log/walnut/
./start.sh''')
    os.chmod('build/wzip/run.sh', 0o755)

    with open('build/update', "w") as f:
        f.write('''#!/bin/bash
   		if [ -e /var/log/''' + proj_name + '''.pid]; then
   			kill `cat /var/log/''' + proj_name + '''.pid`
   		else
   			killall java
   		fi
   		rm -r $WUPROOT/$APPNAME.old
   		mv $WUPROOT/$APPNAME $WUPROOT/$APPNAME.old
   		mkdir $WUPROOT/$APPNAME/
   		tar -C $WUPROOT/$APPNAME/ -x -f $APPNAME.tar
   		rm -rf $WUPROOT/$APPNAME.old &
   	    ''')
    os.chmod('build/update', 0o755)

    # 改写web.properties
    with open("build/wzip/classes/web.properties", "rb") as f :
        webp = f.read()
    with open("build/wzip/classes/web.properties", "wb") as f :
        f.write(webp.replace(b"~/workspace/git/github/walnut", b"."))

    # 改写log4j.properties
    with open("build/wzip/classes/log4j_new.properties", "rb") as f :
        log4j = f.read()
    with open("build/wzip/classes/log4j.properties", "wb") as f :
        f.write(log4j.replace(b'D:/workspace/tmp/log/', b'/var/log/walnut/'))

    if not os.path.exists("build/wzip/classes/META-INF/services/") :
        os.makedirs("build/wzip/classes/META-INF/services/")
    with open("build/wzip/classes/META-INF/services/javax.servlet.ServletContainerInitializer", "w") as f :
        f.write('''org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer
org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer
org.eclipse.jetty.apache.jsp.JettyJasperInitializer
''')

    opt_fatJar()

def add_dir_to_zip(dst, rootdir, all_files) :
    for root, dirs, files in os.walk(rootdir) :
        for name in files:
            fullpath = os.path.join(root, name)
            _path = fullpath[len(rootdir):]
            dst.write(fullpath, _path)
            all_files.append(_path)

def opt_fatJar():

    #if os.path.exists('build/fatJar') :
    #    shutil.rmtree('build/fatJar')
    #os.makedirs('build/fatJar')

    # unzip all deps jar
    all_files = []
    import zipfile
    with zipfile.ZipFile("build/wzip/walnut.jar", "w",compression=zipfile.ZIP_DEFLATED, allowZip64=True) as dst :

        
        dst.writestr("META-INF/MANIFEST.MF", '''Manifest-Version: 1.0
Implementation-Title: Walnut
Implementation-Version: 1.r.69
Main-Class: org.nutz.walnut.web.WnLauncher
''')

        # 添加class目录全部内容
        add_dir_to_zip(dst, "build/wzip/classes/", all_files)
        add_dir_to_zip(dst, "build/wzip/WebContent/", all_files)
        dst.write("build/wzip/web.allows", "web.allows")

        for name in os.listdir("build/wzip/libs") :
            print("unzip >> " + name)
            with zipfile.ZipFile(os.path.join("build/wzip/libs", name), 'r') as zip_ref:
                for name in zip_ref.namelist():
                    if name.find("META-INF") >= 0 and (name.endswith("RSA") or name.endswith("SF") or name.endswith("DSA")) :
                        continue
                    if name.lower().startswith("license"):
                        continue
                    if name == "web_local.properties":
                        continue
                    if name == "META-INF/MANIFEST.MF":
                        continue
                    if name == "META-INF/services/javax.servlet.ServletContainerInitializer" :
                        continue
                    if name in all_files :
                        continue
                    all_files.append(name)
                    dst.writestr(name, zip_ref.read(name))

def opt_wtar():
    import time
    tag = time.strftime("%Y%m%d%H%M%S", time.localtime()) 

    with open('build/wzip/libs/start.sh', "w") as f: 
        f.write("java $JAVA_OPTS -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar %s.jar" % (proj_name, ))
    os.chmod('build/wzip/libs/start.sh', 0o755)

    import tarfile
    with tarfile.open("build/%s.tar" % (proj_name,), "w") as tar :
        tar.add("build/wzip/walnut.jar", "walnut.jar")
        tar.add("build/wzip/run.sh", "run.sh")
        tar.add("build/wzip/stop.sh", "stop.sh")
        tar.add("build/wzip/libs/start.sh", "start.sh")
        tar.add("build/wzip/ROOT", "ROOT")

    with tarfile.open("build/%s-%s.tgz" % (proj_name, tag), "w:gz") as tar :
        tar.add("build/walnut.tar", "walnut.tar")
        tar.add("build/update", "update")

def main():
    opts, args = getopt.getopt(sys.argv[1:], 'hp:v')
    for arg in args :
        if arg == "clean":
            opt_clean()
        elif  arg == "build":
            opt_build()
        elif arg == "lessc":
            print("no more lessc")
            pass#opt_lessc()
        elif arg == "sassc":
            opt_sassc()
        elif arg == "wbuild":
            opt_wbuild()
        elif arg == "wtar":
            opt_wtar()
        elif arg == "fatJar" or arg == "fatjar":
            opt_fatJar()
        else:
            print('???', arg)

if __name__ == '__main__':
    main()

