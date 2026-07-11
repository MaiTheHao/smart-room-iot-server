# Public Module

## Publicly accessible API endpoints (Permit All)

---

<details>
<summary><b>GET</b> <code>/api/v1/public/time</code> - Lấy thời gian hiện tại của server</summary>

> Trả về thời gian hiện tại của server dưới dạng Instant (UTC ISO-8601 String).

### Response Example (200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "time": "2026-07-11T09:10:00Z"
    },
    "timestamp": "2026-07-11T09:10:00Z"
}
```

</details>
