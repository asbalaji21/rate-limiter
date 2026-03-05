---@diagnostic disable: undefined-global

local key = KEYS[1]
local now = tonumber(ARGV[1])
local windowStart = tonumber(ARGV[2])
local member = ARGV[3]

redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)
redis.call('ZADD', key, now, member)
local count = redis.call('ZCOUNT', key, windowStart, now)
redis.call('EXPIRE', key, math.ceil((now - windowStart) / 1000))

return count