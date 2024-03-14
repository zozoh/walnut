var ioc = {
	messageQueueApi : {
		type : 'com.site0.walnut.ext.mq.rocketmq.WnRocketMqApi',
		fields : {
			host : {
				java : '$conf.get("mq-host", "localhost")'
			},
			port : {
				java : '$conf.getInt("mq-port", 9876)'
			},
			timeout : {
				java : '$conf.get("mq-timeout", 5000)'
			},
			producerName : "wn-mq-producer",
			consumerName : "wn-mq-consumer"
		}
	}
}