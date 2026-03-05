# Rate-limiter

## Algorithms

### Fixed Window Rate Limiter

#### Core Concept

    Fixed Window Rate Limiter Algorithm is very simple and easy to implement. 
    The main idea is to keep a counter and increment the counter for every incoming requests. 
    When the counter reaches the limit within the specified window, drop subsequent requests.
    Once the time window has reached, reset the counter and restart the tracking again.

---

#### Pros

- Very Simple to implement
- Though the use case is limited, there are certain use cases where the counter resets at absolute time - fixed window Rate limiter shines there. 
- E.g: Claude Token limit reached (resets at absolute time say 2PM every day, Mobile Data Usage Limit - Resets at 12 AM every day)

#### Cons

- Boundary Problem - When bursts of requests happened at the edges of neighbouring time window, the total requests breached the threshold.
- E.g: Say 5 Requests per 1 minute is the contract. 5 requests happened at 59th second and next set of 5 requests happened at 02nd second of next window. 
- Now technically in the last 1 minute, total requests has reached 10 instead of 5.

---

#### High Level Design

We need a counter to track the requests hit per API per user. Also, the counter has to reset once the TTL is expired. Redis would be the right choice here. We can use the Redis's INCR and EX Commands to achieve this.  

#### Java Implementation

```
        long newValue = redisTemplate.opsForValue().increment(redisKey);
        if(newValue == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SIZE_IN_SECONDS));
        }
        return newValue > LIMIT_THRESHOLD;
```
We use RedisTemplate to connect to Redis server.
Here the increment operation increments the counter if the key is present and automatically creates and increments the counter if the key is not present.
This entire operation is one atomic operation. So, there is no Race condition involved.
If the counter is 1 (meaning it is newly created), we set time to expire on it.
Once the key is expired, it is removed from the redis and the same key is created again if the API is called by same user.
> Key Format: prototype:rate-limit:{userId}:{API endpoint}

---

#### API Contract

> **_Sample Request Curl_**
> 
> postman request POST 'http://localhost:8080/v1/rate-limiter' \
--header 'Content-Type: application/json' \
--body '{
"userId": "sample-user",
"targetEndpoint": "www.google.com"
}'

**_Response Headers_**

| Header | Description |
|--------|-------------|
| X-Ratelimit-Limit | Maximum requests allowed per window |
| X-Ratelimit-Remaining | Requests remaining in current window |
| Retry-After | Seconds to wait before retrying (only on 429) |

---
