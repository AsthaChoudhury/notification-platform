# Notification Platform

A backend notification system that can send Email, SMS, and Push notifications.

The project is split into 4 steps each step adds one layer of complexity. So if you want to understand how it works, you can start from Step 1 and build up.

---

## What it does

You hit one API endpoint with a userId, notification type, and message. The system takes care of the rest queuing it, sending it, retrying if it fails, and storing the result.

```
POST /api/notifications/send
{
  "userId": 1,
  "type": "EMAIL",
  "message": "Your order is confirmed!"
}
```

That's basically the whole idea. Everything else (Kafka, Redis, Docker, PostgreSQL) is the infrastructure that makes this work reliably at scale.

---

## Tech stack

- **Java 25 + Spring Boot 3.4** — the backend
- **Apache Kafka** — message queue so notifications are processed async
- **Redis** — rate limiting (max 10 notifications per user per minute)
- **PostgreSQL** — stores every notification permanently
- **Docker** — runs Postgres, Redis, and Kafka with one command
- **H2** — in-memory database used in early development (Step 1-3)

---

## How I built it (the 4 steps)

### Step 1 — Basic API, no queue
Just a Spring Boot app with H2 in-memory database. Send a notification, it goes directly to the sender, you get a response. Simple synchronous flow. Good starting point to understand the structure before adding complexity.

### Step 2 — Added Kafka
Instead of sending notifications directly, the API now drops the message into a Kafka topic and returns immediately. A consumer picks it up in the background and does the actual sending. This is how you'd handle high traffic without the API getting slow. Also added retry logic, if sending fails, it retries up to 3 times before marking it as dead letter.

### Step 3 — Added Redis rate limiting
Prevents a single user from spamming the API. Each user gets 5 notifications per minute per channel (EMAIL/SMS/PUSH tracked separately). Redis stores the counter with a 60-second TTL so it resets automatically. Response headers show how many requests are remaining.

### Step 4 — Docker
Moved PostgreSQL, Redis, and Kafka into Docker containers. Before this, data was lost every time the app restarted (H2 in-memory). Now data persists permanently. Everything starts with one command.

---

## Running it locally

### What you need
- Java 21+
- Maven
- Docker Desktop (make sure it's running)

### Start the infrastructure
```bash
docker-compose up -d
```

This starts PostgreSQL, Redis, Kafka and Zookeeper. First time takes a couple of minutes to download images.

Check everything is up:
```bash
docker-compose ps
```

All 4 should show `Up` or `healthy`.

### Run the app
Open the project in IntelliJ, navigate to `NotificationApplication.java` and hit run. Or from terminal:

```bash
mvn spring-boot:run
```

Wait for `Started NotificationApplication` in the logs.

---

## Testing it

### Send an email notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "type": "EMAIL", "message": "Your order is confirmed!"}'
```

You should get back:
```json
{
  "notificationId": 1,
  "status": "QUEUED",
  "message": "Notification queued — will be delivered shortly",
  "userId": 1,
  "type": "EMAIL"
}
```

### Send an SMS
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "type": "SMS", "message": "OTP: 847291"}'
```

### Send a push notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{"userId": 3, "type": "PUSH", "message": "Your food is out for delivery!"}'
```

### Check status of a notification
```bash
curl http://localhost:8080/api/notifications/1
```

### See all notifications for a user
```bash
curl http://localhost:8080/api/notifications/user/1
```

### Analytics
```bash
curl http://localhost:8080/api/notifications/analytics
```

Returns total sent, failed, queued etc.

### Test rate limiting
Hit the same endpoint 11 times for the same user. First 10 go through, the 11th returns 429.

```bash
for i in {1..6}; do
  curl -s -o /dev/null -w "Request $i: %{http_code}\n" \
    -X POST http://localhost:8080/api/notifications/send \
    -H "Content-Type: application/json" \
    -d '{"userId": 99, "type": "EMAIL", "message": "rate limit test"}'
done
```

Output:
```
Request 1: 201
Request 2: 201
...
Request 5: 201
Request 6: 429
```

### Health check
```bash
curl http://localhost:8080/api/notifications/health
```

---

## Check the database directly

```bash
docker exec -it notif_postgres psql -U postgres -d notifications_db -c "SELECT id, user_id, type, status FROM notifications;"
```

---

## Project structure

```
src/main/java/com/notifplatform/
  ├── controller/         — HTTP endpoints
  ├── service/            — business logic + email/sms/push senders
  ├── kafka/              — producer, consumer, event model
  ├── ratelimit/          — Redis token bucket
  ├── model/              — database entities
  ├── dto/                — request/response objects
  └── repository/         — database access
```

---

## Things I ran into while building this

**Java 25 + Lombok don't get along.** Lombok uses annotation processing which breaks on newer Java versions. Ended up removing Lombok entirely and writing getters/setters manually. Not ideal for a real project but it works fine here.

**Spring Boot 3.4 compiled with Java 25 bytecode crashes.** The ASM library inside Spring can't read Java 25 class files. Fixed by setting `maven.compiler.source` and `maven.compiler.target` to 21 even though the JDK itself is 25.

**Redis INCR vs checking and incrementing separately.** At first I wrote code that reads the counter, checks if it's below the limit, then increments. This has a race condition two requests can read the same value simultaneously and both get through. Switched to `INCR` which is atomic in Redis.

---

## Stopping everything

```bash
# Stop the app hit the stop button in IntelliJ

# Stop Docker containers (data is preserved)
docker-compose down

# Stop and delete all data (fresh start)
docker-compose down -v
```