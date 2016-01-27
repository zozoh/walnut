({
	"style" : {
		"width" : 1920,
		"height" : 1080
	},
	"duration" : 1800,
	"monitors" : [ {
		"width" : 1920,
		"height" : 1080,
		"mode" : "landscape"
	}, {
		"width" : 1920,
		"height" : 1080,
		"mode" : "portrait"
	} ],
	"layers" : [ {
		"name" : "联图浮动层",
		"zIndex" : 2,
		"events" : {
			"init" : {
				"action" : "playItems",
				"items" : [ {
					"duration" : 60,
					"args" : {
						"monitor" : null
					},
					"style" : {
						"top" : 0,
						"left" : 0,
						"width" : null,
						"height" : null
					},
					"params" : {
						"main" : "q1mlut1864hifq62pmfhgv19g3"
					},
					"deps" : [ {
						"fid" : "q1mlut1864hifq62pmfhgv19g3"
					} ],
					"libName" : "pic"
				}, {
					"duration" : 60,
					"args" : {
						"monitor" : null
					},
					"style" : {
						"top" : 0,
						"left" : 0,
						"width" : null,
						"height" : null
					},
					"params" : {
						"main" : "619fijhh82gu0q6fq38e9itnob"
					},
					"deps" : [ {
						"fid" : "619fijhh82gu0q6fq38e9itnob"
					} ],
					"libName" : "pic"
				} ],
				"duration" : 120
			}
		}
	}, {
		"name" : "视频播放层",
		"zIndex" : 2,
		"events" : {
			"init" : {
				"action" : "playItems",
				"items" : []
			}
		}
	} ]
})