var unm = args.length > 0 ? args[0] : "";

if(unm) {
	sys.out.println('--------------------------------------\n# Remove user profile:');
	sys.exec('rm -v /sys/usr/"' + unm + '"');
	
	sys.out.println('--------------------------------------\n# Remove user group setting:');
	sys.exec('rm -rfv /sys/grp/"' + unm + '"');
	
	sys.out.println('--------------------------------------\n# Remove user home:');
	sys.exec('rm -rfv /home/"' + unm + '"');
}else {
	sys.err.println("no user name");
}