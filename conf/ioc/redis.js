var ioc = {
    redisConfForIoRefers : {
        type : 'org.nutz.walnut.ext.redis.WedisConfig',
        fields : {
            host : {
                java : '$conf.get("redis-host")'
            },
            port : {
                java : '$conf.getInt("redis-port")'
            },
            ssl : {
                java : '$conf.getBoolean("redis-ssl", false)'
            },
            password : {
                java : '$conf.get("redis-password")'
            },
            database : 0
        }
    },
    redisConfForIoBM : {
        type : 'org.nutz.walnut.ext.redis.WedisConfig',
        fields : {
            host : {
                java : '$conf.get("redis-host")'
            },
            port : {
                java : '$conf.getInt("redis-port")'
            },
            ssl : {
                java : '$conf.getBoolean("redis-ssl", false)'
            },
            password : {
                java : '$conf.get("redis-password")'
            },
            database : 1
        }
    },
    redisConfForIoHandle : {
        type : 'org.nutz.walnut.ext.redis.WedisConfig',
        fields : {
            host : {
                java : '$conf.get("redis-host")'
            },
            port : {
                java : '$conf.getInt("redis-port")'
            },
            ssl : {
                java : '$conf.getBoolean("redis-ssl", false)'
            },
            password : {
                java : '$conf.get("redis-password")'
            },
            database : 2
        }
    },
    redisConfForLockApi : {
        type : 'org.nutz.walnut.ext.redis.WedisConfig',
        fields : {
            host : {
                java : '$conf.get("redis-host")'
            },
            port : {
                java : '$conf.getInt("redis-port")'
            },
            ssl : {
                java : '$conf.getBoolean("redis-ssl", false)'
            },
            password : {
                java : '$conf.get("redis-password")'
            },
            database : 2,
            setup : {
                "prefix" : {
                    java: '$conf.get("lock-redis-prefix", "lock:")'
                },
                "ask-du" : {
                    java: '$conf.getInt("lock-ask-du", 3)'
                },
                "ask-retry-interval" : {
                    java: '$conf.getLong("lock-ask-retry-interval", 100)'
                },
                "ask-retry-times" : {
                    java: '$conf.getInt("lock-ask-retry-times", 5)'
                }
            }
        }
    }
}