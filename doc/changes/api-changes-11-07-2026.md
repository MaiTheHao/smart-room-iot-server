# API Changes Log - 11/07/2026

## Added
- **GET** `/api/v1/public/time`: Lấy thời gian hiện tại của server (UTC).
  - Controller: `PublicApiController`
  - DTO trả về: `ServerTimeResponseDto`
  - Cấu trúc dữ liệu:
    ```json
    {
      "status": 200,
      "message": "Success",
      "data": {
        "time": "2026-07-11T09:07:26Z"
      },
      "timestamp": "2026-07-11T09:07:26Z"
    }
    ```
  - Trạng thái bảo mật: Public (Permit All)
