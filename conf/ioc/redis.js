var ioc = {
    redis : {
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
            database : {
                java : '$conf.getInt("redis-database", 0)'
            }
        }
    }
}