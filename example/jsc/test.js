var o = sys.fetch("~/xyz");


var ins = sys.io().getInputStream(o, 0);

var o2 = sys.check("~/abc.txt")
var re = sys.io().writeAndClose(o2, ins);
sys.out.println("re is " + re);