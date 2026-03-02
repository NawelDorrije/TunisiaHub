# Carpooling Trips API (Phase 1 - Task 1)

Base path: `/api/carpooling/trips`

Headers used for identity on driver-only endpoints:

- `X-USER-ID: <long>`
- `X-ROLE: DRIVER | PASSENGER | ADMIN`

## Create trip (DRIVER only)

```bash
curl -X POST "http://localhost:8080/api/carpooling/trips" \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 101" \
  -H "X-ROLE: DRIVER" \
  -d '{
    "departurePoint": "Tunis",
    "destination": "Sousse",
    "departureDateTime": "2026-03-20T09:30:00",
    "price": 20.50,
    "seatsTotal": 3
  }'
```

## Search/list trips (public)

```bash
curl "http://localhost:8080/api/carpooling/trips?departurePoint=tunis&destination=sousse&date=2026-03-20"
```

## Trip details (public)

```bash
curl "http://localhost:8080/api/carpooling/trips/1"
```

## My trips (DRIVER only)

```bash
curl "http://localhost:8080/api/carpooling/trips/me" \
  -H "X-USER-ID: 101" \
  -H "X-ROLE: DRIVER"
```

## Cancel trip (DRIVER owner only)

```bash
curl -X PATCH "http://localhost:8080/api/carpooling/trips/1/cancel" \
  -H "X-USER-ID: 101" \
  -H "X-ROLE: DRIVER"
```
