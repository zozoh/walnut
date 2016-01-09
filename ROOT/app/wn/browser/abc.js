({
	style : {
		width : 1920,
		height : 1080
	},
	duration : 1800,
	monitors : [ {
		width : 1920,
		height : 1080,
		mode : "landscape"
	}, {
		width : 1920,
		height : 1080,
		mode : "portrait"
	} ],
	layers : [ {
		name : '联图浮动层',
		zIndex : 2,
		events : {
			"init" : {
				action : "playItems",
				items : [ {
					duration : 600,
					libName : "pic",
					args : {
						monitor : 0,
					},
					style : {
						width : 600,
						height : 400,
						top : -10,
						left : 80
					},
					params : {
						main : 45
					},
					deps : [ {
						fid : "xxxxxx"
					} ]
				}, {
					duration : 600,
					libName : "pic",
					args : {
						monitor : 0,
					},
					style : {
						width : 600,
						height : 400,
						top : -10,
						left : 80
					},
					params : {
						main : 45
					},
					deps : [ {
						fid : "xxxxxx"
					} ]
				} ]
			}
		}
	}, {
		name : '视频播放层',
		zIndex : 2,
		events : {
			"init" : {
				action : "playItems",
				items : [ {
					duration : 600,
					libName : "pic",
					args : {
						monitor : 0,
					},
					style : {
						width : 600,
						height : 400,
						top : -10,
						left : 80
					},
					params : {
						main : 45
					},
					deps : [ {
						fid : "xxxxxx"
					} ]
				}, {
					duration : 600,
					libName : "pic",
					args : {
						monitor : 0,
					},
					style : {
						width : 600,
						height : 400,
						top : -10,
						left : 80
					},
					params : {
						main : 45
					},
					deps : [ {
						fid : "xxxxxx"
					} ]
				} ]
			}
		}
	} ]
})