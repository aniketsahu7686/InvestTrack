# 🚀 InvestTrack — Full Implementation Plan

> **Microservices-based Trade Idea Tracking Platform**
> Status tracking: ⬜ Not Started | 🔄 In Progress | ✅ Completed

---

## 📁 Project Structure Overview

```
InvestTrack/
├── pom.xml                          (Parent POM)
├── docker-compose.yml
├── .env
├── common-lib/                      (Shared library)
├── auth-service/                    (Port 8081)
├── trade-service/                   (Port 8082)
├── market-data-service/             (Port 8083)
├── analytics-service/               (Port 8084)
├── api-gateway/                     (Port 8080)
└── frontend/                        (Angular 17, Port 4200)
```

---

## Phase 1: Project Foundation ⬜

### Step 1.1 — Root Parent POM ⬜
> Ask: "Execute Step 1.1"

**File:** `pom.xml`
- Multi-module Maven parent POM
- Spring Boot 3.2.3 parent
- Module declarations: common-lib, auth-service, trade-service, market-data-service, analytics-service, api-gateway
- Dependency management: Spring Cloud 2023.0.0, MapStruct 1.5.5, Lombok 1.18.30, JJWT 0.12.5, SpringDoc 2.3.0
- Compiler plugin with Lombok + MapStruct annotation processors

### Step 1.2 — Common Library Module ⬜
> Ask: "Execute Step 1.2"

**Files:**
```
common-lib/
├── pom.xml
└── src/main/java/com/investtrack/common/
    ├── enums/
    │   ├── Timeframe.java           (INTRADAY, SWING, POSITIONAL)
    │   ├── TradeStatus.java         (OPEN, TARGET_HIT, SL_HIT, EXPIRED)
    │   ├── TradeReason.java         (TECHNICAL, FUNDAMENTAL)
    │   └── UserRole.java            (USER, VERIFIED_TRADER, ADMIN)
    ├── dto/
    │   ├── TradeIdeaRequest.java    (Validated request DTO)
    │   ├── TradeIdeaResponse.java   (Response DTO)
    │   ├── RegisterRequest.java     (Registration DTO)
    │   ├── LoginRequest.java        (Login DTO)
    │   ├── AuthResponse.java        (JWT response DTO)
    │   ├── TraderRankingResponse.java (Leaderboard DTO)
    │   └── PriceUpdateDTO.java      (Live price DTO)
    ├── event/
    │   ├── TradeStatusEvent.java    (RabbitMQ event: trade closed)
    │   └── PriceUpdateEvent.java    (RabbitMQ event: price tick)
    ├── config/
    │   └── RabbitMQConstants.java   (Exchange, queue, routing key names)
    ├── exception/
    │   ├── ApiError.java            (Standard error response)
    │   ├── ResourceNotFoundException.java
    │   ├── BusinessValidationException.java
    │   ├── UnauthorizedException.java
    │   └── GlobalExceptionHandler.java (Handles all exceptions)
    └── security/
        └── JwtUtils.java           (Token generation, validation, claim extraction)
```

---

## Phase 2: Auth Service ⬜

### Step 2.1 — Auth Service Setup ⬜
> Ask: "Execute Step 2.1"

**Files:**
```
auth-service/
├── pom.xml
├── src/main/resources/
│   └── application.yml
└── src/main/java/com/investtrack/auth/
    └── AuthServiceApplication.java
```
- Dependencies: web, security, data-jpa, postgresql, validation, amqp, actuator, common-lib
- Application config: port 8081, datasource, JPA, JWT secret, actuator

### Step 2.2 — Auth Entity & Repository ⬜
> Ask: "Execute Step 2.2"

**Files:**
```
auth-service/src/main/java/com/investtrack/auth/
├── entity/
│   └── User.java                   (UUID PK, username, email, password, role, timestamps)
└── repository/
    └── UserRepository.java         (findByUsername, findByEmail, existsByUsername, existsByEmail)
```

### Step 2.3 — Auth Security Configuration ⬜
> Ask: "Execute Step 2.3"

**Files:**
```
auth-service/src/main/java/com/investtrack/auth/
├── security/
│   ├── SecurityConfig.java         (SecurityFilterChain, BCrypt, CORS, stateless sessions)
│   ├── JwtAuthenticationFilter.java (OncePerRequestFilter, extract & validate JWT)
│   └── UserDetailsServiceImpl.java (Load user from DB for Spring Security)
```

### Step 2.4 — Auth Service & Controller ⬜
> Ask: "Execute Step 2.4"

**Files:**
```
auth-service/src/main/java/com/investtrack/auth/
├── service/
│   └── AuthService.java           (register, login, getCurrentUser)
├── controller/
│   └── AuthController.java        (POST /auth/register, POST /auth/login, GET /auth/me)
└── mapper/
    └── UserMapper.java            (MapStruct — Entity ↔ DTO)
```

### Step 2.5 — Auth Flyway Migration ⬜
> Ask: "Execute Step 2.5"

**Files:**
```
auth-service/src/main/resources/
└── db/migration/
    └── V1__create_users_table.sql  (users table + indexes on username, email)
```

### Step 2.6 — Auth Unit Tests ⬜
> Ask: "Execute Step 2.6"

**Files:**
```
auth-service/src/test/java/com/investtrack/auth/
├── service/
│   └── AuthServiceTest.java       (JUnit 5 + Mockito)
└── controller/
    └── AuthControllerTest.java    (MockMvc tests)
```

---

## Phase 3: Trade Service ⬜

### Step 3.1 — Trade Service Setup ⬜
> Ask: "Execute Step 3.1"

**Files:**
```
trade-service/
├── pom.xml
├── src/main/resources/
│   └── application.yml
└── src/main/java/com/investtrack/trade/
    └── TradeServiceApplication.java
```
- Dependencies: web, data-jpa, postgresql, validation, amqp, actuator, common-lib
- Application config: port 8082, datasource, RabbitMQ, JWT

### Step 3.2 — Trade Entity & Repository ⬜
> Ask: "Execute Step 3.2"

**Files:**
```
trade-service/src/main/java/com/investtrack/trade/
├── entity/
│   └── TradeIdea.java             (UUID PK, userId, stockSymbol, entryPrice, stopLoss, targetPrice,
│                                    riskRewardRatio, riskPercentage, timeframe, reason, status,
│                                    notes, createdAt, closedAt)
└── repository/
    └── TradeIdeaRepository.java   (JPA repository + custom queries)
```

### Step 3.3 — Trade JPA Specifications ⬜
> Ask: "Execute Step 3.3"

**Files:**
```
trade-service/src/main/java/com/investtrack/trade/
└── specification/
    └── TradeIdeaSpecification.java (Dynamic filters: timeframe, status, R:R, stockSymbol, userId)
```
- Filter by timeframe, winRate >= X, open trades, R:R >= X
- Supports pagination and sorting

### Step 3.4 — Trade Service Layer ⬜
> Ask: "Execute Step 3.4"

**Files:**
```
trade-service/src/main/java/com/investtrack/trade/
├── service/
│   └── TradeIdeaService.java      (createTrade, getTrades, autoCloseTrade)
├── mapper/
│   └── TradeIdeaMapper.java       (MapStruct — Entity ↔ DTO)
└── config/
    ├── RabbitMQConfig.java        (Exchange, queue, binding declarations)
    └── SecurityConfig.java        (JWT filter for trade-service)
```
- Validates R:R >= 1 (rejects otherwise)
- Auto-calculates riskRewardRatio = (target - entry) / (entry - stopLoss)
- No editing after submission
- Publishes TradeStatusEvent on status change

### Step 3.5 — Trade Controller ⬜
> Ask: "Execute Step 3.5"

**Files:**
```
trade-service/src/main/java/com/investtrack/trade/
├── controller/
│   └── TradeIdeaController.java   (POST /trades, GET /trades, GET /trades/{id}, GET /trades/user/{userId})
└── security/
    └── JwtAuthenticationFilter.java (JWT validation filter)
```
- `POST /trades` — create (authenticated)
- `GET /trades` — list with filters + pagination (public)
- `GET /trades/{id}` — get by ID (public)
- `GET /trades/user/{userId}` — get by user (public)

### Step 3.6 — Trade Event Listeners ⬜
> Ask: "Execute Step 3.6"

**Files:**
```
trade-service/src/main/java/com/investtrack/trade/
├── listener/
│   └── PriceUpdateListener.java   (Consumes PriceUpdateEvent from RabbitMQ)
├── publisher/
│   └── TradeEventPublisher.java   (Publishes TradeStatusEvent to RabbitMQ)
└── scheduler/
    └── TradeReconciliationJob.java (Scheduled job: reconcile open trades periodically)
```
- Listens for price updates → checks all open trades for that symbol
- If LTP >= target → `TARGET_HIT`
- If LTP <= stopLoss → `SL_HIT`
- Stores `closedAt` timestamp
- Publishes `TradeStatusEvent`

### Step 3.7 — Trade Flyway Migration ⬜
> Ask: "Execute Step 3.7"

**Files:**
```
trade-service/src/main/resources/
└── db/migration/
    └── V1__create_trades_table.sql (trades table + indexes on stockSymbol, userId, status, createdAt)
```

### Step 3.8 — Trade Unit Tests ⬜
> Ask: "Execute Step 3.8"

**Files:**
```
trade-service/src/test/java/com/investtrack/trade/
├── service/
│   └── TradeIdeaServiceTest.java
└── controller/
    └── TradeIdeaControllerTest.java
```

---

## Phase 4: Market Data Service ⬜

### Step 4.1 — Market Data Service Setup ⬜
> Ask: "Execute Step 4.1"

**Files:**
```
market-data-service/
├── pom.xml
├── src/main/resources/
│   └── application.yml
└── src/main/java/com/investtrack/marketdata/
    └── MarketDataServiceApplication.java
```
- Dependencies: web, data-jpa, postgresql, data-redis, amqp, websocket, actuator, common-lib
- Application config: port 8083, Redis, RabbitMQ, Kite API keys

### Step 4.2 — Kite Integration Layer ⬜
> Ask: "Execute Step 4.2"

**Files:**
```
market-data-service/src/main/java/com/investtrack/marketdata/
├── kite/
│   ├── KiteConfig.java            (API key, secret, redirect URL properties)
│   ├── KiteAuthService.java       (OAuth flow: generate login URL, exchange token)
│   ├── KiteTokenManager.java      (Store & refresh daily access token)
│   └── KiteWebSocketClient.java   (Connect to Kite WebSocket, receive ticks)
├── entity/
│   └── InstrumentToken.java       (stockSymbol → instrument token mapping)
└── repository/
    └── InstrumentTokenRepository.java
```

### Step 4.3 — Redis Price Cache & Publisher ⬜
> Ask: "Execute Step 4.3"

**Files:**
```
market-data-service/src/main/java/com/investtrack/marketdata/
├── config/
│   ├── RedisConfig.java           (RedisTemplate, serializers)
│   └── RabbitMQConfig.java        (Exchanges, queues for price events)
├── service/
│   ├── PriceCacheService.java     (Get/set prices in Redis with TTL)
│   └── PricePublisher.java        (Publish PriceUpdateEvent to RabbitMQ)
├── controller/
│   ├── KiteAuthController.java    (GET /kite/login, GET /kite/callback)
│   └── PriceController.java       (GET /prices/{symbol}, POST /prices/subscribe)
└── scheduler/
    └── PriceReconciliationJob.java (Periodic validation of cached prices)
```

### Step 4.4 — Market Data Flyway Migration ⬜
> Ask: "Execute Step 4.4"

**Files:**
```
market-data-service/src/main/resources/
└── db/migration/
    └── V1__create_instrument_tokens_table.sql
```

---

## Phase 5: Analytics Service ⬜

### Step 5.1 — Analytics Service Setup ⬜
> Ask: "Execute Step 5.1"

**Files:**
```
analytics-service/
├── pom.xml
├── src/main/resources/
│   └── application.yml
└── src/main/java/com/investtrack/analytics/
    └── AnalyticsServiceApplication.java
```
- Dependencies: web, data-jpa, postgresql, amqp, actuator, common-lib
- Application config: port 8084, datasource, RabbitMQ

### Step 5.2 — Analytics Entity & Repository ⬜
> Ask: "Execute Step 5.2"

**Files:**
```
analytics-service/src/main/java/com/investtrack/analytics/
├── entity/
│   └── TraderStats.java           (userId, totalTrades, wins, losses, winRate, avgRiskReward,
│                                    consistencyScore, riskControlScore, overallRankingScore, lastUpdated)
└── repository/
    └── TraderStatsRepository.java (findByUserId, findAllByOrderByOverallRankingScoreDesc)
```

### Step 5.3 — Ranking Algorithm & Service ⬜
> Ask: "Execute Step 5.3"

**Files:**
```
analytics-service/src/main/java/com/investtrack/analytics/
├── service/
│   ├── RankingService.java        (Calculate: Score = WinRate×0.4 + AvgRR×0.3 + Consistency×0.2 + RiskControl×0.1)
│   └── AnalyticsService.java      (Aggregate stats, maintain leaderboard)
├── listener/
│   └── TradeStatusListener.java   (Consume TradeStatusEvent → recalculate stats)
├── scheduler/
│   └── RankingRecalculationJob.java (Cron job: recalculate all rankings)
└── config/
    └── RabbitMQConfig.java
```

### Step 5.4 — Analytics Controller ⬜
> Ask: "Execute Step 5.4"

**Files:**
```
analytics-service/src/main/java/com/investtrack/analytics/
└── controller/
    └── AnalyticsController.java   (GET /analytics/leaderboard, GET /analytics/trader/{userId})
```
- Paginated leaderboard sorted by overallRankingScore DESC
- Individual trader stats endpoint

### Step 5.5 — Analytics Flyway Migration ⬜
> Ask: "Execute Step 5.5"

**Files:**
```
analytics-service/src/main/resources/
└── db/migration/
    └── V1__create_trader_stats_table.sql (trader_stats + index on overallRankingScore)
```

### Step 5.6 — Analytics Unit Tests ⬜
> Ask: "Execute Step 5.6"

**Files:**
```
analytics-service/src/test/java/com/investtrack/analytics/
├── service/
│   └── RankingServiceTest.java
└── controller/
    └── AnalyticsControllerTest.java
```

---

## Phase 6: API Gateway ⬜

### Step 6.1 — API Gateway Setup ⬜
> Ask: "Execute Step 6.1"

**Files:**
```
api-gateway/
├── pom.xml
├── src/main/resources/
│   └── application.yml
└── src/main/java/com/investtrack/gateway/
    └── ApiGatewayApplication.java
```
- Dependencies: spring-cloud-gateway, data-redis-reactive, actuator, common-lib
- Route definitions → auth(8081), trade(8082), market(8083), analytics(8084)

### Step 6.2 — Gateway Filters & Config ⬜
> Ask: "Execute Step 6.2"

**Files:**
```
api-gateway/src/main/java/com/investtrack/gateway/
├── filter/
│   ├── JwtAuthGatewayFilter.java  (Validate JWT at gateway, pass userId header downstream)
│   └── RateLimitingFilter.java    (Redis-based rate limiter for trade posting)
├── config/
│   ├── CorsConfig.java            (Allow Angular origin)
│   └── RouteConfig.java           (Programmatic route definitions, if needed)
└── exception/
    └── GatewayExceptionHandler.java
```

---

## Phase 7: Docker & DevOps ⬜

### Step 7.1 — Dockerfiles ⬜
> Ask: "Execute Step 7.1"

**Files:**
```
auth-service/Dockerfile
trade-service/Dockerfile
market-data-service/Dockerfile
analytics-service/Dockerfile
api-gateway/Dockerfile
```
- Multi-stage build: Maven build → JRE 17 runtime
- Health check instructions
- Non-root user

### Step 7.2 — Docker Compose & Environment ⬜
> Ask: "Execute Step 7.2"

**Files:**
```
docker-compose.yml                  (All services + infrastructure)
.env                                (Environment variables template)
```
- Services: api-gateway, auth-service, trade-service, market-data-service, analytics-service
- Infrastructure: PostgreSQL (5 schemas), Redis, RabbitMQ, pgAdmin
- Health checks, depends_on, restart policies
- Shared network

---

## Phase 8: Angular Frontend ⬜

### Step 8.1 — Angular Project Setup ⬜
> Ask: "Execute Step 8.1"

**Files:**
```
frontend/
├── angular.json
├── package.json
├── tsconfig.json
├── tsconfig.app.json
└── src/
    ├── index.html
    ├── main.ts
    ├── styles.scss
    └── app/
        ├── app.component.ts
        ├── app.component.html
        ├── app.config.ts
        └── app.routes.ts
```
- Angular 17+ with standalone components
- Angular Material setup
- Environment configuration (API URLs)

### Step 8.2 — Auth Module (Frontend) ⬜
> Ask: "Execute Step 8.2"

**Files:**
```
frontend/src/app/
├── auth/
│   ├── login/
│   │   ├── login.component.ts
│   │   └── login.component.html
│   ├── register/
│   │   ├── register.component.ts
│   │   └── register.component.html
│   └── guards/
│       └── auth.guard.ts
├── interceptors/
│   └── jwt.interceptor.ts
└── services/
    └── auth.service.ts
```

### Step 8.3 — Trade Ideas Module (Frontend) ⬜
> Ask: "Execute Step 8.3"

**Files:**
```
frontend/src/app/
├── trades/
│   ├── create-trade/
│   │   ├── create-trade.component.ts
│   │   └── create-trade.component.html
│   ├── trade-list/
│   │   ├── trade-list.component.ts
│   │   └── trade-list.component.html
│   └── trade-detail/
│       ├── trade-detail.component.ts
│       └── trade-detail.component.html
└── services/
    └── trade.service.ts
```
- Create trade form with full validation (R:R >= 1, SL & Target mandatory)
- Trade list with filters (timeframe, status, R:R), pagination, sorting
- Trade detail view

### Step 8.4 — Leaderboard Module (Frontend) ⬜
> Ask: "Execute Step 8.4"

**Files:**
```
frontend/src/app/
├── leaderboard/
│   ├── leaderboard.component.ts
│   └── leaderboard.component.html
├── trader-profile/
│   ├── trader-profile.component.ts
│   └── trader-profile.component.html
└── services/
    └── analytics.service.ts
```
- Ranked trader table with pagination
- Individual trader profile with stats breakdown

### Step 8.5 — Live Prices & WebSocket (Frontend) ⬜
> Ask: "Execute Step 8.5"

**Files:**
```
frontend/src/app/
├── live-prices/
│   ├── price-ticker/
│   │   ├── price-ticker.component.ts
│   │   └── price-ticker.component.html
│   └── price-dashboard/
│       ├── price-dashboard.component.ts
│       └── price-dashboard.component.html
└── services/
    └── websocket.service.ts
```
- WebSocket client connecting via API Gateway
- Real-time price ticker display
- Subscription management

### Step 8.6 — Shared Components (Frontend) ⬜
> Ask: "Execute Step 8.6"

**Files:**
```
frontend/src/app/
├── shared/
│   ├── navbar/
│   │   ├── navbar.component.ts
│   │   └── navbar.component.html
│   ├── disclaimer-banner/
│   │   ├── disclaimer-banner.component.ts
│   │   └── disclaimer-banner.component.html
│   ├── loading-spinner/
│   │   ├── loading-spinner.component.ts
│   │   └── loading-spinner.component.html
│   └── models/
│       ├── trade.model.ts
│       ├── user.model.ts
│       └── analytics.model.ts
└── environments/
    ├── environment.ts
    └── environment.prod.ts
```
- Navbar with auth-aware menu
- Disclaimer banner: *"Ideas shared are for educational purposes only. Do your own research."*
- No profit guarantee wording — uses "performance score" instead of monetary P&L

---

## 📊 Progress Tracker

| Phase | Description | Steps | Status |
|-------|-------------|-------|--------|
| 1 | Project Foundation | 1.1 – 1.2 | ⬜ |
| 2 | Auth Service | 2.1 – 2.6 | ⬜ |
| 3 | Trade Service | 3.1 – 3.8 | ⬜ |
| 4 | Market Data Service | 4.1 – 4.4 | ⬜ |
| 5 | Analytics Service | 5.1 – 5.6 | ⬜ |
| 6 | API Gateway | 6.1 – 6.2 | ⬜ |
| 7 | Docker & DevOps | 7.1 – 7.2 | ⬜ |
| 8 | Angular Frontend | 8.1 – 8.6 | ⬜ |
| | **Total** | **30 steps** | |

---

## 🏃 How To Use This Plan

1. **Say:** `"Execute Step X.Y"` — I will create all files for that step
2. **After completion**, I'll update this plan to mark it ✅
3. **You can skip steps** or do them out of order (dependencies noted above)
4. **You can combine:** `"Execute Steps 2.1 to 2.4"` for batch execution

### Dependencies
- Phase 1 must be done first (all services depend on common-lib)
- Phase 2 (Auth) should be before Phase 3 (Trade needs JWT)
- Phase 4 (Market Data) before Phase 3.6 (Trade listeners need price events)
- Phase 7 (Docker) after all backend services
- Phase 8 (Frontend) can be done after Phase 6 (Gateway)

---

*Last Updated: March 4, 2026*
