/**
 * 确保 /etc 目录是普通目录，原来的目录通常是一个映射目录
 * <br>之后将 /etc 目录下加上固定两个链接目录 /etc/ui 和 /etc/thumbnail
 */

var re = sys.exec2('obj /etc -V -e mnt');

// 是链接目录，那么开始搞吧
if(re) {
	sys.out.println("start tidy /etc");
	
	// 得到下面的关键目录 mount 信息
	var mnt_ui = sys.exec2('obj /etc/ui -V -e mnt');
	var mnt_thumbnail = sys.exec2('obj /etc/thumbnail -V -e mnt');
	
	sys.out.printlnf(" - /etc/ui => %s", mnt_ui);
	sys.out.printlnf(" - /etc/thumbnail => %s", mnt_thumbnail);
	
	// 移除原来的 /etc
	var cmdText = 'rm /etc';
	sys.out.printlnf(" do:> %s", cmdText);
	sys.exec(cmdText);
	
	// 创建新的目录
	cmdText = 'mkdir /etc; chmod +rx /etc';
	sys.out.printlnf(" do:> %s", cmdText);
	sys.exec(cmdText);
	
	// 创建子目录 /etc/ui
	cmdText = 'mkdir /etc/ui; mount "' + mnt_ui + '" /etc/ui';
	sys.out.printlnf(" do:> %s", cmdText);
	sys.exec(cmdText);
	
	// 创建子目录 /etc/thumbnail
	cmdText = 'mkdir /etc/thumbnail; mount "' + mnt_thumbnail + '" /etc/thumbnail';
	sys.out.printlnf(" do:> %s", cmdText);
	sys.exec(cmdText);
	
	// 创建默认的 hosts.d
	var pos = mnt_ui.lastIndexOf("/");
	var mnt_dft = mnt_ui.substring(0, pos) + "/hosts.d/default";
	cmdText = 'mkdir -p /etc/hosts.d/default; mount "' + mnt_dft + '" /etc/hosts.d/default';
	sys.out.printlnf(" do:> %s", cmdText);
	sys.exec(cmdText);
	
	// 搞定
	sys.out.println("All done");
}
//
else {
	sys.out.println("not need tidy /etc");
}