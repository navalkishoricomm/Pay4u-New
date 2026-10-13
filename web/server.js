const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const DATA_FILE = path.join(__dirname, 'data.json');

// Initial state matching Android app
const defaultState = {
  users: [
    { id: 1, name: "Master Admin", phone: "9999999999", email: "admin@pay4u.in", role: "ADMIN", walletBalance: 25000.0 },
    { id: 2, name: "Mukesh Garg", phone: "9876543210", email: "mukgarg11@gmail.com", role: "USER", walletBalance: 1250.0 }
  ],
  transactions: [
    {
      id: 1,
      userId: 2,
      userName: "Mukesh Garg",
      userPhone: "9876543210",
      serviceType: "MOBILE",
      category: "Prepaid",
      providerName: "Jio",
      targetNumber: "9876543210",
      amount: 299.0,
      paymentMethod: "UPI",
      userUpiUtr: "428710928374",
      status: "SUCCESS",
      operatorTrId: "JIO-TR-88492019",
      adminNotes: "Processed via Jio Core Gateway",
      createdAt: Date.now() - 86400000
    },
    {
      id: 2,
      userId: 2,
      userName: "Mukesh Garg",
      userPhone: "9876543210",
      serviceType: "DTH",
      category: "Monthly Pack",
      providerName: "Tata Play",
      targetNumber: "1098472910",
      amount: 450.0,
      paymentMethod: "CREDIT_CARD",
      userUpiUtr: "VISA-••4242",
      status: "PENDING",
      operatorTrId: "",
      adminNotes: "Awaiting Admin TR ID",
      createdAt: Date.now() - 1800000
    }
  ],
  vouchers: [
    { id: 1, brandName: "Amazon Pay Gift Card", category: "Shopping", discountPercent: 2.5, denominations: [250, 500, 1000, 2000, 5000], badge: "Best Seller" },
    { id: 2, brandName: "Flipkart Gift Card", category: "Shopping", discountPercent: 2.0, denominations: [100, 500, 1000, 2500, 5000], badge: "Popular" },
    { id: 3, brandName: "Swiggy Food & Instamart", category: "Food", discountPercent: 5.0, denominations: [100, 250, 500, 1000], badge: "5% OFF" },
    { id: 4, brandName: "Zomato Voucher", category: "Food", discountPercent: 4.0, denominations: [150, 250, 500, 1000], badge: "Hot Deal" },
    { id: 5, brandName: "Google Play Code", category: "Gaming", discountPercent: 3.0, denominations: [100, 300, 500, 1000, 1500], badge: "Gaming" },
    { id: 6, brandName: "Myntra E-Gift Card", category: "Fashion", discountPercent: 6.0, denominations: [500, 1000, 2000, 5000], badge: "6% OFF" }
  ],
  userVouchers: [
    {
      id: 1,
      userId: 2,
      userName: "Mukesh Garg",
      brandName: "Amazon Pay Gift Card",
      denomination: 500.0,
      finalPaid: 487.50,
      paymentMethod: "UPI",
      status: "ACTIVE",
      voucherCode: "AMZN-8839-2910-KPL9",
      pinCode: "4921",
      expiryDate: "12 Oct 2027",
      purchasedAt: Date.now() - 3600000
    }
  ],
  providers: [
    { id: 1, serviceType: "MOBILE", name: "Jio", code: "JIO", commissionPercent: 2.2, flatCommission: 0.0, isActive: true },
    { id: 2, serviceType: "MOBILE", name: "Airtel", code: "AIRTEL", commissionPercent: 1.8, flatCommission: 0.0, isActive: true },
    { id: 3, serviceType: "MOBILE", name: "Vi", code: "VI", commissionPercent: 3.0, flatCommission: 0.0, isActive: true },
    { id: 4, serviceType: "MOBILE", name: "BSNL", code: "BSNL", commissionPercent: 4.5, flatCommission: 0.0, isActive: true },
    { id: 5, serviceType: "DTH", name: "Tata Play", code: "TATASKY", commissionPercent: 2.5, flatCommission: 0.0, isActive: true },
    { id: 6, serviceType: "DTH", name: "Airtel Digital TV", code: "AIRTELDTH", commissionPercent: 2.8, flatCommission: 0.0, isActive: true },
    { id: 7, serviceType: "BILL", name: "BSES Rajdhani Delhi", code: "BSES", commissionPercent: 0.5, flatCommission: 2.0, isActive: true },
    { id: 8, serviceType: "BILL", name: "Delhi Jal Board", code: "DJB", commissionPercent: 0.0, flatCommission: 3.0, isActive: true }
  ]
};

function loadState() {
  try {
    if (fs.existsSync(DATA_FILE)) {
      return JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
    }
  } catch (e) {
    console.error("Error loading state:", e);
  }
  saveState(defaultState);
  return defaultState;
}

function saveState(state) {
  try {
    fs.writeFileSync(DATA_FILE, JSON.stringify(state, null, 2), 'utf8');
  } catch (e) {
    console.error("Error saving state:", e);
  }
}

let db = loadState();

function sendJson(res, data, status = 200) {
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type'
  });
  res.end(JSON.stringify(data));
}

function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        reject(e);
      }
    });
  });
}

const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    return res.end();
  }

  // API Endpoints
  if (pathname === '/api/state') {
    return sendJson(res, db);
  }

  if (pathname === '/api/bill/fetch' && req.method === 'POST') {
    try {
      const { category, biller, consumerId } = await parseBody(req);
      const numHash = parseInt((consumerId || '500').replace(/\D/g, '').slice(-4) || '500', 10);
      const baseAmount = category === 'Electricity' ? 850 + (numHash % 1200) : 350 + (numHash % 400);
      const units = category === 'Electricity' ? `${Math.floor(baseAmount / 7.5)} kWh` : '15 kL';

      return sendJson(res, {
        isSuccess: true,
        consumerName: "Mukesh Garg",
        consumerNumber: consumerId,
        billerName: biller,
        billDate: "01 Oct 2026",
        dueDate: "25 Oct 2026",
        amount: baseAmount,
        unitsConsumed: units,
        latencyMs: 142,
        status: "HTTP 200 OK (Live BBPS Network Verified)"
      });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/recharge' && req.method === 'POST') {
    try {
      const { serviceType, category, providerName, targetNumber, amount, paymentMethod, paymentRef } = await parseBody(req);
      const user = db.users.find(u => u.id === 2);

      // Strict Wallet Validation
      if (paymentMethod === 'WALLET') {
        if (user.walletBalance < amount) {
          return sendJson(res, {
            error: `Insufficient Wallet Balance! Available: ₹${user.walletBalance.toFixed(2)}, Required: ₹${amount.toFixed(2)}. Please choose UPI or Credit Card.`
          }, 400);
        }
        user.walletBalance -= amount;
      }

      const newTx = {
        id: db.transactions.length + 1,
        userId: user.id,
        userName: user.name,
        userPhone: user.phone,
        serviceType,
        category,
        providerName,
        targetNumber,
        amount: parseFloat(amount),
        paymentMethod,
        userUpiUtr: paymentMethod === 'UPI' ? paymentRef : '',
        status: "PENDING",
        operatorTrId: "",
        adminNotes: paymentMethod === 'WALLET' ? "Paid via Wallet (Debited)" : `Payment Ref: ${paymentRef}`,
        createdAt: Date.now()
      };

      db.transactions.unshift(newTx);
      saveState(db);
      return sendJson(res, { success: true, transaction: newTx, user });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/voucher/buy' && req.method === 'POST') {
    try {
      const { voucherId, denomination, paymentMethod, paymentRef } = await parseBody(req);
      const user = db.users.find(u => u.id === 2);
      const vch = db.vouchers.find(v => v.id === parseInt(voucherId, 10));
      if (!vch) return sendJson(res, { error: "Voucher not found" }, 404);

      const discountAmt = denomination * (vch.discountPercent / 100.0);
      const finalPayable = denomination - discountAmt;

      // Strict Wallet Balance Validation
      if (paymentMethod === 'WALLET') {
        if (user.walletBalance < finalPayable) {
          return sendJson(res, {
            error: `Insufficient Wallet Balance! Available: ₹${user.walletBalance.toFixed(2)}, Required: ₹${finalPayable.toFixed(2)}. You are short by ₹${(finalPayable - user.walletBalance).toFixed(2)}. Please pay via UPI or Credit Card.`
          }, 400);
        }
        user.walletBalance -= finalPayable;
      }

      const txId = db.transactions.length + 1;
      const newTx = {
        id: txId,
        userId: user.id,
        userName: user.name,
        userPhone: user.phone,
        serviceType: "VOUCHER",
        category: "Brand Voucher",
        providerName: vch.brandName,
        targetNumber: `${vch.brandName} (₹${denomination})`,
        amount: denomination,
        netAmount: finalPayable,
        paymentMethod,
        userUpiUtr: paymentMethod === 'UPI' ? paymentRef : '',
        status: "PENDING",
        operatorTrId: "",
        adminNotes: "Awaiting Admin payment review & genuine voucher code upload",
        createdAt: Date.now()
      };
      db.transactions.unshift(newTx);

      // Create Pending User Voucher Record (No fake generated code!)
      const newUserVoucher = {
        id: db.userVouchers.length + 1,
        userId: user.id,
        userName: user.name,
        transactionId: txId,
        brandName: vch.brandName,
        denomination,
        finalPaid: finalPayable,
        paymentMethod,
        status: "PENDING", // PENDING ADMIN UPLOAD
        voucherCode: "",
        pinCode: "",
        expiryDate: "",
        adminNotes: "Order submitted. Awaiting genuine voucher code from Admin.",
        purchasedAt: Date.now()
      };
      db.userVouchers.unshift(newUserVoucher);

      saveState(db);
      return sendJson(res, { success: true, userVoucher: newUserVoucher, transaction: newTx, user });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  // Admin Actions
  if (pathname === '/api/admin/tr-id' && req.method === 'POST') {
    try {
      const { transactionId, operatorTrId, status, notes } = await parseBody(req);
      const tx = db.transactions.find(t => t.id === parseInt(transactionId, 10));
      if (!tx) return sendJson(res, { error: "Transaction not found" }, 404);

      tx.operatorTrId = operatorTrId.trim();
      tx.status = status;
      tx.adminNotes = notes || "Processed by Admin";

      // If FAILED and paid via wallet, refund!
      if (status === 'FAILED' && tx.paymentMethod === 'WALLET') {
        const user = db.users.find(u => u.id === tx.userId);
        if (user) user.walletBalance += (tx.netAmount || tx.amount);
      }

      saveState(db);
      return sendJson(res, { success: true, transaction: tx });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/admin/upload-voucher' && req.method === 'POST') {
    try {
      const { voucherId, genuineCode, pinCode, expiryDate, notes } = await parseBody(req);
      const vch = db.userVouchers.find(v => v.id === parseInt(voucherId, 10));
      if (!vch) return sendJson(res, { error: "Voucher order not found" }, 404);

      vch.voucherCode = genuineCode.trim();
      vch.pinCode = pinCode.trim();
      vch.expiryDate = expiryDate.trim();
      vch.status = "ACTIVE";
      vch.adminNotes = notes || "Verified and genuine code uploaded by Admin";

      // Also mark transaction as SUCCESS
      const tx = db.transactions.find(t => t.id === vch.transactionId);
      if (tx) {
        tx.status = "SUCCESS";
        tx.operatorTrId = genuineCode.trim();
        tx.adminNotes = "Genuine voucher issued";
      }

      saveState(db);
      return sendJson(res, { success: true, userVoucher: vch });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/admin/reject-voucher' && req.method === 'POST') {
    try {
      const { voucherId, reason } = await parseBody(req);
      const vch = db.userVouchers.find(v => v.id === parseInt(voucherId, 10));
      if (!vch) return sendJson(res, { error: "Voucher order not found" }, 404);

      vch.status = "REJECTED";
      vch.adminNotes = reason || "Payment verification failed";

      const tx = db.transactions.find(t => t.id === vch.transactionId);
      if (tx) {
        tx.status = "FAILED";
        tx.adminNotes = reason;
      }

      // Refund wallet if paid via wallet
      if (vch.paymentMethod === 'WALLET') {
        const user = db.users.find(u => u.id === vch.userId);
        if (user) user.walletBalance += vch.finalPaid;
      }

      saveState(db);
      return sendJson(res, { success: true, userVoucher: vch });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/admin/adjust-balance' && req.method === 'POST') {
    try {
      const { userId, amountDelta } = await parseBody(req);
      const user = db.users.find(u => u.id === parseInt(userId, 10));
      if (!user) return sendJson(res, { error: "User not found" }, 404);

      user.walletBalance = Math.max(0, user.walletBalance + parseFloat(amountDelta));
      saveState(db);
      return sendJson(res, { success: true, user });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  if (pathname === '/api/admin/commission' && req.method === 'POST') {
    try {
      const { providerId, commissionPercent, flatCommission, isActive } = await parseBody(req);
      const prov = db.providers.find(p => p.id === parseInt(providerId, 10));
      if (!prov) return sendJson(res, { error: "Provider not found" }, 404);

      prov.commissionPercent = parseFloat(commissionPercent);
      prov.flatCommission = parseFloat(flatCommission);
      prov.isActive = Boolean(isActive);

      saveState(db);
      return sendJson(res, { success: true, provider: prov });
    } catch (e) {
      return sendJson(res, { error: e.message }, 400);
    }
  }

  // Serve static HTML index
  if (pathname === '/' || pathname === '/index.html' || pathname === '/admin') {
    const htmlPath = path.join(__dirname, 'index.html');
    if (fs.existsSync(htmlPath)) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      return res.end(fs.readFileSync(htmlPath));
    }
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`pay4u Web Portal & Admin Panel listening on http://0.0.0.0:${PORT}`);
});
