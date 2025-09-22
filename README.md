# PocketMusala API

Kotlin/Ktor REST API for finding nearby mosques.

## Status
- ✅ Basic API structure working
- ✅ `/mosques/nearby` endpoint with validation
- ✅ Health check endpoint
- 🔄 Firebase/Firestore integration pending
- 🔄 Docker deployment pending

## Running Locally
```bash
./gradlew run

## Endpoints
-GET /health - Health check
-GET /mosques/nearby?lat={lat}&lng={lng}&radiusKm={radius} - Find nearby mosques
