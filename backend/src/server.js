const express = require('express')
const app = express()
const port = 3000

// Middleware để parse JSON body
app.use(express.json())

app.post('/api/create', (req, res) => {
    //https://developers.momo.vn/#/docs/en/aiov2/?id=payment-method
    //parameters
    var accessKey = 'F8BBA842ECF85';
    var secretKey = 'K951B6PE1waDMi640xX08PD3vg6EkVlz';

    // Lấy thông tin từ request body
    const { paymentName, amount, description } = req.body;

    var orderInfo = paymentName || '1234567';
    var partnerCode = 'MOMO';
    // Redirect về backend để hiển thị HTML
    var redirectUrl = 'http://192.168.1.176:3000/api/result';
    var ipnUrl = 'http://192.168.1.176:3000/api/result';
    var requestType = "payWithMethod";
    var orderId = partnerCode + new Date().getTime();
    var requestId = orderId;
    var extraData = description ? Buffer.from(description).toString('base64') : '';
    var paymentCode = 'T8Qii53fAXyUftPV3m9ysyRhEanUs9KlOPfHgpMR0ON50U10Bh+vZdpJU7VY4z+Z2y77fJHkoDc69scwwzLuW5MzeUKTwPo3ZMaB29imm6YulqnWfTkgzqRaion+EuD7FN9wZ4aXE1+mRt0gHsU193y+yxtRgpmY7SDMU9hCKoQtYyHsfFR5FUAOAKMdw2fzQqpToei3rnaYvZuYaxolprm9+/+WIETnPUDlxCYOiw7vPeaaYQQH0BF0TxyU3zu36ODx980rJvPAgtJzH1gUrlxcSS1HQeQ9ZaVM1eOK/jl8KJm6ijOwErHGbgf/hVymUQG65rHU2MWz9U8QUjvDWA==';
    var orderGroupId = '';
    var autoCapture = true;
    var lang = 'vi';

    //before sign HMAC SHA256 with format
    //accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType
    var rawSignature = "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData + "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo + "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl + "&requestId=" + requestId + "&requestType=" + requestType;
    //puts raw signature
    console.log("--------------------RAW SIGNATURE----------------")
    console.log(rawSignature)
    //signature
    const crypto = require('crypto');
    var signature = crypto.createHmac('sha256', secretKey)
        .update(rawSignature)
        .digest('hex');
    console.log("--------------------SIGNATURE----------------")
    console.log(signature)

    //json object send to MoMo endpoint
    const requestBody = JSON.stringify({
        partnerCode: partnerCode,
        partnerName: "Test",
        storeId: "MomoTestStore",
        requestId: requestId,
        amount: amount,
        orderId: orderId,
        orderInfo: orderInfo,
        redirectUrl: redirectUrl,
        ipnUrl: ipnUrl,
        lang: lang,
        requestType: requestType,
        autoCapture: autoCapture,
        extraData: extraData,
        orderGroupId: orderGroupId,
        signature: signature
    });
    //Create the HTTPS objects
    const https = require('https');
    const options = {
        hostname: 'test-payment.momo.vn',
        port: 443,
        path: '/v2/gateway/api/create',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(requestBody)
        }
    }
    //Send the request and get the response
    const req2 = https.request(options, res2 => {
        console.log(`Status: ${res2.statusCode}`);
        console.log(`Headers: ${JSON.stringify(res2.headers)}`);
        res2.setEncoding('utf8');

        let body = '';
        res2.on('data', (chunk) => {
            body += chunk;
        });

        res2.on('end', () => {
            console.log('Response Body: ', body);
            try {
                const response = JSON.parse(body);
                res.status(201).json({
                    message: 'Create MoMo payment successfully',
                    data: response,
                });
            } catch (error) {
                console.error('Error parsing response:', error);
                res.status(500).json({
                    message: 'Error parsing MoMo response',
                    error: error.message
                });
            }
        });
    })

    req2.on('error', (e) => {
        console.log(`problem with request: ${e.message}`);
    });
    // write data to request body
    console.log("Sending....")
    req2.write(requestBody);
    req2.end();
})

app.get('/api/result', (req, res) => {
    console.log('=== MoMo Payment Result ===');
    console.log('Query params:', req.query);

    const {
        partnerCode,
        orderId,
        requestId,
        amount,
        orderInfo,
        orderType,
        transId,
        resultCode,
        message,
        payType,
        responseTime,
        extraData,
        signature
    } = req.query;

    // Kiểm tra resultCode để xác định giao dịch thành công hay thất bại
    if (resultCode === '0') {
        // Giao dịch thành công
        console.log('✅ Payment successful!');
        console.log(`Order ID: ${orderId}`);
        console.log(`Transaction ID: ${transId}`);
        console.log(`Amount: ${amount} VND`);

        // Trả về trang HTML thành công
        res.send(`
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Thanh toán thành công</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                    }
                    .success-icon {
                        font-size: 64px;
                        color: #4CAF50;
                        margin-bottom: 20px;
                    }
                    h1 {
                        color: #333;
                        margin: 0 0 10px 0;
                    }
                    .message {
                        color: #666;
                        margin-bottom: 30px;
                    }
                    .detail {
                        text-align: left;
                        background: #f5f5f5;
                        padding: 20px;
                        border-radius: 5px;
                        margin-bottom: 20px;
                    }
                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 10px;
                    }
                    .detail-label {
                        font-weight: bold;
                        color: #555;
                    }
                    .detail-value {
                        color: #333;
                    }
                    .btn {
                        background: #667eea;
                        color: white;
                        padding: 12px 30px;
                        border: none;
                        border-radius: 5px;
                        font-size: 16px;
                        cursor: pointer;
                        text-decoration: none;
                        display: inline-block;
                    }
                    .btn:hover {
                        background: #5568d3;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✓</div>
                    <h1>Thanh toán thành công!</h1>
                    <p class="message">${message}</p>
                    
                    <div class="detail">
                        <div class="detail-row">
                            <span class="detail-label">Mã đơn hàng:</span>
                            <span class="detail-value">${orderId}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Mã giao dịch:</span>
                            <span class="detail-value">${transId}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Số tiền:</span>
                            <span class="detail-value">${parseInt(amount).toLocaleString('vi-VN')} VND</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Phương thức:</span>
                            <span class="detail-value">${payType}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Thời gian:</span>
                            <span class="detail-value">${new Date(parseInt(responseTime)).toLocaleString('vi-VN')}</span>
                        </div>
                    </div>
                    
                    <a href="irequest://payment/success?orderId=${orderId}&transId=${transId}&amount=${amount}&payType=${payType}" class="btn">Quay về ứng dụng</a>
                </div>
                <script>
                    // Tự động redirect sau 3 giây
                    setTimeout(function() {
                        window.location.href = 'irequest://payment/success?orderId=${orderId}&transId=${transId}&amount=${amount}&payType=${payType}';
                    }, 3000);
                </script>
            </body>
            </html>
        `);
    } else {
        // Giao dịch thất bại
        console.log('❌ Payment failed!');
        console.log(`Result Code: ${resultCode}`);
        console.log(`Message: ${message}`);

        res.send(`
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Thanh toán thất bại</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.1);
                        text-align: center;
                        max-width: 500px;
                    }
                    .error-icon {
                        font-size: 64px;
                        color: #f44336;
                        margin-bottom: 20px;
                    }
                    h1 {
                        color: #333;
                        margin: 0 0 10px 0;
                    }
                    .message {
                        color: #666;
                        margin-bottom: 30px;
                    }
                    .detail {
                        text-align: left;
                        background: #f5f5f5;
                        padding: 20px;
                        border-radius: 5px;
                        margin-bottom: 20px;
                    }
                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 10px;
                    }
                    .detail-label {
                        font-weight: bold;
                        color: #555;
                    }
                    .detail-value {
                        color: #333;
                    }
                    .btn {
                        background: #667eea;
                        color: white;
                        padding: 12px 30px;
                        border: none;
                        border-radius: 5px;
                        font-size: 16px;
                        cursor: pointer;
                        text-decoration: none;
                        display: inline-block;
                        margin: 5px;
                    }
                    .btn:hover {
                        background: #5568d3;
                    }
                    .btn-retry {
                        background: #4CAF50;
                    }
                    .btn-retry:hover {
                        background: #45a049;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">✗</div>
                    <h1>Thanh toán thất bại!</h1>
                    <p class="message">${message}</p>
                    
                    <div class="detail">
                        <div class="detail-row">
                            <span class="detail-label">Mã đơn hàng:</span>
                            <span class="detail-value">${orderId}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Mã lỗi:</span>
                            <span class="detail-value">${resultCode}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Số tiền:</span>
                            <span class="detail-value">${parseInt(amount).toLocaleString('vi-VN')} VND</span>
                        </div>
                    </div>
                    
                    <a href="irequest://payment/failed?orderId=${orderId}&resultCode=${resultCode}" class="btn">Quay về ứng dụng</a>
                </div>
                <script>
                    // Tự động redirect sau 3 giây
                    setTimeout(function() {
                        window.location.href = 'irequest://payment/failed?orderId=${orderId}&resultCode=${resultCode}';
                    }, 3000);
                </script>
            </body>
            </html>
        `);
    }
});

// POST endpoint cho IPN (Server-to-Server notification từ MoMo)
app.post('/api/result', (req, res) => {
    console.log('=== MoMo IPN Notification ===');
    console.log('Body:', req.body);

    // Xác thực signature từ MoMo
    const crypto = require('crypto');
    const secretKey = 'K951B6PE1waDMi640xX08PD3vg6EkVlz';
    const accessKey = 'F8BBA842ECF85';

    const {
        partnerCode,
        orderId,
        requestId,
        amount,
        orderInfo,
        orderType,
        transId,
        resultCode,
        message,
        payType,
        responseTime,
        extraData,
        signature
    } = req.body;

    // Tạo signature để xác thực
    const rawSignature = `accessKey=${accessKey}&amount=${amount}&extraData=${extraData}&message=${message}&orderId=${orderId}&orderInfo=${orderInfo}&orderType=${orderType}&partnerCode=${partnerCode}&payType=${payType}&requestId=${requestId}&responseTime=${responseTime}&resultCode=${resultCode}&transId=${transId}`;

    const calculatedSignature = crypto.createHmac('sha256', secretKey)
        .update(rawSignature)
        .digest('hex');

    // So sánh signature
    if (signature === calculatedSignature) {
        console.log('✅ Signature verified!');

        // Xử lý cập nhật database, gửi email, etc.
        if (resultCode === 0) {
            console.log('✅ Payment confirmed via IPN');
            // TODO: Cập nhật trạng thái đơn hàng trong database
        } else {
            console.log('❌ Payment failed via IPN');
            // TODO: Xử lý đơn hàng thất bại
        }

        // Trả về 204 No Content để MoMo biết đã nhận được thông báo
        res.status(204).send();
    } else {
        console.log('❌ Invalid signature!');
        res.status(400).json({ message: 'Invalid signature' });
    }
});

app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})
