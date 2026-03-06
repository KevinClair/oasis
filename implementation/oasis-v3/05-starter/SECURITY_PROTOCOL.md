# SECURITY_PROTOCOL

## 鉴权方式
`AppKey + HMAC`。

## 签名建议串
`timestamp + nonce + method + path + bodyHash`

## 安全校验
- 时间窗校验
- nonce 防重放
- bodyHash 防篡改
