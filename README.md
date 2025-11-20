**API TO SWAP TOKENS VIA DEDUST**

# Beeton Swap API — Примеры запросов

## Эндпоинт
```
POST http://localhost:8080/beeton-swap-api/swap/desust
Content-Type: application/json
```

---

## Общие параметры запроса

| Поле | Тип | Обязательное | Описание |
|------|------|---------------|-----------|
| **direction** | `string` | ✅ | Направление сделки: `buy` или `sell`. <br>— `buy`: вы **покупаете** `jettonB`, отдавая `jettonA` или TON. <br>— `sell`: вы **продаёте** `jettonA` за `jettonB` или TON. |
| **route** | `string` | ✅ | Тип маршрута обмена: <br>— `native`: обмен через пул с **TON** (одношаговый). <br>— `multi`: многошаговый маршрут через TON (например, `A → TON → B`). |
| **jettonA** | `string` | ✅ | Адрес токена, который **отдаётся** при `sell`, или токена, который **покупается** при `buy`. <br>Формат: `workchain:hash` (64 hex-символа), например:<br>`0:111122223333444455556666777788889999aaaabbbbccccddddeeeeffff0000` |
| **jettonB** | `string` | ✅ | Адрес токена, который **получается** после обмена. <br>Для обменов с **native (TON)** можно передавать любое значение. |
| **jettonAmount** | `string` | ✅ | Количество токенов в **атомарных единицах**. <br>Например `"1000000000"` соответствует 1 токену, если у него 9 знаков после запятой. |
| **mnemonic** | `string` | ✅ | BIP-39 мнемоническая фраза (24 слова, пробел-разделённые). Используется серверным кошельком для подписания транзакции. **Осторожно:** мнемоника — секрет, не храните и не пересылайте её в публичных каналах. |


---

## Примеры запросов

---

### Покупка (BUY) — маршрут **multi** (через TON)

Вы хотите **купить `jettonA`**, используя TON и промежуточный обмен через `jettonB`.

```bash
curl -X POST 'http://localhost:8080/beeton-swap-api/swap/desust'   -H 'Content-Type: application/json'   -H 'Idempotency-Key: buy-multi-001'   -H 'X-Request-Id: req-buy-multi-001'   -d '{
    "direction": "buy",
    "route": "multi",
    "jettonA": "0:111122223333444455556666777788889999aaaabbbbccccddddeeeeffff0000",
    "jettonB": "0:9999888877776666555544443333222211110000ffffeeeeddddccccbbbbaaaa",
    "jettonAmount": "1000000000",
    "mnemonic": "flush wolf spring claim shy mouse vibrant unfold trend call sea kick mechanic syrup winner crumble fun celery group high uncover miss deputy social"
  }'
```

**Пояснение:**
- `direction: "buy"` — вы покупаете токен **A** (jettonA).
- `route: "multi"` — обмен идёт через TON (два шага): **TON → jettonA**.
- `jettonA` — токен, который вы хотите получить.
- `jettonB` — вспомогательный токен (может быть TON).
- `jettonAmount` — сколько токенов **jettonA** вы хотите купить (в атомарных единицах).

---

### Продажа (SELL) — маршрут **multi**

Вы хотите **продать `jettonA`** за `jettonB` через TON.

```bash
curl -X POST 'http://localhost:8080/beeton-swap-api/swap/desust'   -H 'Content-Type: application/json'   -H 'Idempotency-Key: sell-multi-001'   -H 'X-Request-Id: req-sell-multi-001'   -d '{
    "direction": "sell",
    "route": "multi",
    "jettonA": "0:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "jettonB": "0:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
    "jettonAmount": "2500000000",
    "mnemonic": "flush wolf spring claim shy mouse vibrant unfold trend call sea kick mechanic syrup winner crumble fun celery group high uncover miss deputy social"
  }'
```

**Пояснение:**
- `direction: "sell"` — вы продаёте токен **A**.
- `route: "multi"` — используется двухшаговый обмен: **jettonA → TON → jettonB**.
- `jettonA` — токен, который вы отдаёте.
- `jettonB` — токен, который вы хотите получить.
- `jettonAmount` — количество jettonA, которое продаётся (в атомарных единицах).

---

### Продажа (SELL) — маршрут **native**

Вы хотите **продать jettonA** за **TON** напрямую, без второго шага.

```bash
curl -X POST 'http://localhost:8080/beeton-swap-api/swap/desust'   -H 'Content-Type: application/json'   -H 'Idempotency-Key: sell-native-001'   -H 'X-Request-Id: req-sell-native-001'   -d '{
    "direction": "sell",
    "route": "native",
    "jettonA": "0:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "jettonB": "0:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
    "jettonAmount": "500000000",
    "mnemonic": "flush wolf spring claim shy mouse vibrant unfold trend call sea kick mechanic syrup winner crumble fun celery group high uncover miss deputy social"
  }'
```

**Пояснение:**
- `direction: "sell"` — вы продаёте токен **A**.
- `route: "native"` — обмен осуществляется **jettonA → TON**.
- `jettonA` — токен, который вы отдаёте.
- `jettonB` — произвольный (TON не имеет адреса jetton).
- `jettonAmount` — количество продаваемого токена **A**.

---

## Пример успешного ответа

```json
{
  "status": "success",
  "direction": "sell",
  "route": "multi",
  "jettonA": "0:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "jettonB": "0:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
  "jettonAmount": "2500000000",
  "amountIn": "285000000",
  "amountOut": "26355686",
  "message": "Transaction sent successfully"
}
```

---

## Возможные ошибки

| Код | Тип | Описание |
|-----|------|----------|
| `400` | `BAD_REQUEST` | Пропущены обязательные поля или ошибка формата JSON. |
| `422` | `UNPROCESSABLE_ENTITY` | Невозможный маршрут обмена (`native` недоступен для пары). |
| `500` | `INTERNAL_ERROR` | Внутренняя ошибка при формировании или отправке транзакции. |

Пример:
```json
{
  "status": "error",
  "code": "UNPROCESSABLE_ENTITY",
  "message": "Route 'native' недоступен для указанной пары активов"
}
```

---

## Кратко

| Сценарий | direction | route | Пример |
|-----------|------------|--------|---------|
| Покупка за TON | `buy` | `native` | `A ← TON` |
| Покупка за Jetton | `buy` | `multy` | `B ← TON ←  A` |
| Продажа за Jetton | `sell` | `multi` | `A → TON → B` |
| Продажа за TON | `sell` | `native` | `A → TON` |