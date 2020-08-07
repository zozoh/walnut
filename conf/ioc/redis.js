var ioc = {
    redis0 : {
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
    redis1 : {
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
    redis2 : {
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
    }
}