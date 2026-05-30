// --- Google Play RSA Signature Verification ---
// This public key is from Play Console > Monetization > Licensing.
// It is SAFE to embed — it's a public key that can only verify, not forge, signatures.
const PLAY_RSA_PUBLIC_KEY = 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAukdH3ssZz1OGXrOYJ+VtZhRkU3eknS1St1wBA5vuvquymV3x48AiypIr/AiOblkJNnZnWsYTWasutbDNVpPvm8KpOESWHFPGNNhZcTHfHxkSQ9En1i6kVZSV0D3zM7Ki+FdRaUECyGFwsU4d0uK/C1McsASa8AXyrAXa3IC2QvAAjstPj3CEDAQ0ChlDX59Ve4xrc17KL2N+wQi94W9g0Cpd4fkCeUsHn/QT3UNgrMpq1qYVnTYWgtcgV2fbpYUCh7HliWXLQ5cBt3oIKChMzCEo3GPGh+d9SZtGO9wItaJvLcbPX7YJKjR3rPNEq4ZR54Y5vEcXBJmwmXoY/DkIWwIDAQAB';

async function verifyPlaySignature(originalJson, signature) {
	// Decode the base64 public key (SPKI format)
	const keyBuffer = Uint8Array.from(atob(PLAY_RSA_PUBLIC_KEY), c => c.charCodeAt(0));

	// Import RSA public key for verification
	const publicKey = await crypto.subtle.importKey(
		'spki', keyBuffer.buffer,
		{ name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-1' },
		false, ['verify']
	);

	// Decode the base64 signature from Google Play
	const sigBuffer = Uint8Array.from(atob(signature), c => c.charCodeAt(0));

	// Verify: does Google's signature match the purchase JSON?
	return await crypto.subtle.verify(
		'RSASSA-PKCS1-v1_5',
		publicKey,
		sigBuffer.buffer,
		new TextEncoder().encode(originalJson)
	);
}

// --- Product ID to Tier Mapping ---
const PRODUCT_TIER_MAP = {
	'ad_free_monthly': 'AD_FREE', 'ad_free_yearly': 'AD_FREE',
	'plus_monthly': 'PLUS', 'plus_yearly': 'PLUS',
	'pro_monthly': 'PRO', 'pro_yearly': 'PRO',
	'elite_monthly': 'ELITE', 'elite_yearly': 'ELITE'
};

export default {
	async fetch(request, env, ctx) {
		const url = new URL(request.url);
		const path = url.pathname;
		const method = request.method;

		// --- CORS Headers (restricted to known origins) ---
		const origin = request.headers.get("Origin");
		const ALLOWED_ORIGINS = ["null"]; // "null" = Android WebView (file://)

		const corsHeaders = {
			"Access-Control-Allow-Methods": "GET,HEAD,POST,OPTIONS,DELETE",
			"Access-Control-Allow-Headers": "Content-Type, Authorization",
			"Access-Control-Max-Age": "86400",
		};

		// Only allow CORS for known origins; native Android HTTP clients don't send Origin
		if (!origin || ALLOWED_ORIGINS.includes(origin)) {
			corsHeaders["Access-Control-Allow-Origin"] = origin || "null";
		}

		if (method === "OPTIONS") {
			return new Response(null, { headers: corsHeaders });
		}

		// --- ADMIN AUTH CHECK ---
		// Validates if the route starts with /api/admin and checks the Authorization header
		const isAdminRoute = path.startsWith('/api/admin');
		// Admin secret loaded from environment variable (set via: wrangler secret put ADMIN_SECRET)
		const ADMIN_SECRET = env.ADMIN_SECRET;

		if (isAdminRoute) {
			if (!ADMIN_SECRET) {
				return new Response(JSON.stringify({ error: "Server admin configuration missing" }), { status: 500, headers: corsHeaders });
			}
			const authHeader = request.headers.get('Authorization');
			if (!authHeader || authHeader !== `Bearer ${ADMIN_SECRET}`) {
				return new Response(JSON.stringify({ error: "Unauthorized" }), { status: 401, headers: corsHeaders });
			}
		}

		try {
			// ==========================================
			// ENDPOINT: GET /api/templates
			// Fetches a list of all ACCEPTED templates, joined with their tags
			// ==========================================
			if (method === "GET" && path === "/api/templates") {
				const { results } = await env.DB.prepare(
					`SELECT t.id, t.user_id, t.title, t.content, t.thumbnail, t.created_at, 
					 (SELECT GROUP_CONCAT(tag_name) FROM Tags WHERE template_id = t.id) as tags,
					 (SELECT AVG(score) FROM Reviews WHERE template_id = t.id) as avg_rating,
					 (SELECT COUNT(score) FROM Reviews WHERE template_id = t.id) as rating_count
					 FROM Templates t 
					 WHERE t.status = 'accepted'
					 ORDER BY t.created_at DESC`
				).all();

				return new Response(JSON.stringify({ success: true, templates: results }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/templates
			// Saves a new template and its tag
			// ==========================================
			if (method === "POST" && path === "/api/templates") {
				const body = await request.json();
				const { user_id, title, content, tag, thumbnail } = body;

				if (!title || !content) {
					return new Response(JSON.stringify({ error: "Missing title or content" }), { status: 400, headers: corsHeaders });
				}

				// Check Global Lock
				const globalLock = await env.DB.prepare(`SELECT value FROM Settings WHERE id = 'global_publish_allowed'`).first();
				if (globalLock && globalLock.value !== 'true') {
					return new Response(JSON.stringify({ error: "Publishing is currently disabled globally" }), { status: 403, headers: corsHeaders });
				}

				// Check if User is Banned
				if (user_id) {
					const isBanned = await env.DB.prepare(`SELECT user_id FROM BannedUsers WHERE user_id = ?`).bind(user_id).first();
					if (isBanned) {
						return new Response(JSON.stringify({ error: "You are banned from publishing" }), { status: 403, headers: corsHeaders });
					}
				}

				// Check Storage Safeguard (950MB Limit)
				const storageRes = await env.DB.prepare(`
					SELECT (
						SELECT SUM(LENGTH(content) + COALESCE(LENGTH(thumbnail), 0)) FROM Templates
					) + (
						SELECT COALESCE(SUM(LENGTH(review_text)), 0) FROM Reviews
					) + (
						SELECT COALESCE(SUM(LENGTH(tag_name)), 0) FROM Tags
					) as total_bytes
				`).first();

				const MAX_STORAGE_SAFEGUARD = 950 * 1024 * 1024; // 950MB
				if (storageRes && storageRes.total_bytes >= MAX_STORAGE_SAFEGUARD) {
					return new Response(JSON.stringify({ error: "Server storage limit reached (950MB). Publishing is temporarily disabled." }), { status: 507, headers: corsHeaders });
				}

				// Generate unique ID
				const templateId = crypto.randomUUID();

				// Insert into DB
				await env.DB.prepare(
					`INSERT INTO Templates (id, user_id, title, content, thumbnail) VALUES (?, ?, ?, ?, ?)`
				).bind(templateId, user_id || "anonymous", title, JSON.stringify(content), thumbnail || null).run();

				// Insert Tag if provided
				if (tag) {
					await env.DB.prepare(
						`INSERT INTO Tags (template_id, tag_name) VALUES (?, ?)`
					).bind(templateId, tag).run();
				}

				return new Response(JSON.stringify({ success: true, templateId }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/coupons/verify
			// Securely verify a coupon code and update user tier
			// ==========================================
			if (method === "POST" && (path === "/api/coupons/verify" || path === "/api/coupons/verify/")) {
				const body = await request.json();
				const { uid, code } = body;

				if (!uid || !code) {
					return new Response(JSON.stringify({ error: "Missing uid or code" }), { status: 400, headers: corsHeaders });
				}

				// Coupon codes are stored in env var as JSON (set via wrangler secret put VALID_COUPONS)
				// Format: {"CODE1":"TIER1","CODE2":"TIER2"}
				let validCoupons = {};
				try {
					if (env.VALID_COUPONS) validCoupons = JSON.parse(env.VALID_COUPONS);
				} catch (e) {
					return new Response(JSON.stringify({ error: "Server coupon configuration error" }), { status: 500, headers: corsHeaders });
				}

				const normalizedCode = code.trim().toUpperCase();
				const targetTier = validCoupons[normalizedCode];

				if (!targetTier) {
					return new Response(JSON.stringify({ success: false, error: "Invalid coupon code" }), { status: 400, headers: corsHeaders });
				}

				// Update User Tier in DB
				const result = await env.DB.prepare(`
					UPDATE Users SET tier = ?, last_seen = CURRENT_TIMESTAMP WHERE uid = ?
				`).bind(targetTier, uid).run();

				if (!result.meta.changes) {
					return new Response(JSON.stringify({ success: false, error: "User profile not found. Try restarting the app to sync." }), { status: 404, headers: corsHeaders });
				}

				return new Response(JSON.stringify({ success: true, tier: targetTier }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/users/sync
			// Centralized user registry sync
			// ==========================================
			if (method === "POST" && path === "/api/users/sync") {
				const body = await request.json();
				const { uid, email, name, ip, device_model, android_version } = body;

				if (!uid) return new Response("Missing uid", { status: 400, headers: corsHeaders });

				// Server-authoritative tier: sync does NOT accept tier from client.
				// Tier is only set via /api/subscriptions/verify or /api/coupons/verify.
				await env.DB.prepare(`
					INSERT INTO Users (uid, email, name, ip, device_model, android_version, tier, last_seen)
					VALUES (?, ?, ?, ?, ?, ?, 'FREE', CURRENT_TIMESTAMP)
					ON CONFLICT(uid) DO UPDATE SET
						email = excluded.email,
						name = excluded.name,
						ip = excluded.ip,
						device_model = excluded.device_model,
						android_version = excluded.android_version,
						last_seen = CURRENT_TIMESTAMP
				`).bind(uid, email || null, name || null, ip || null, device_model || null, android_version || null).run();

				// Return the server-authoritative tier so app can sync
				const user = await env.DB.prepare(`SELECT tier FROM Users WHERE uid = ?`).bind(uid).first();
				return new Response(JSON.stringify({ success: true, tier: user ? user.tier : 'FREE' }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/subscriptions/verify
			// Verifies Google Play purchase token server-side and grants tier
			// ==========================================
			if (method === "POST" && path === "/api/subscriptions/verify") {
				const body = await request.json();
				const { uid, originalJson, signature } = body;

				if (!uid || !originalJson || !signature) {
					return new Response(JSON.stringify({ error: "Missing uid, originalJson, or signature" }), { status: 400, headers: corsHeaders });
				}

				try {
					// Verify Google Play's RSA signature on the purchase data
					const isValid = await verifyPlaySignature(originalJson, signature);

					if (!isValid) {
						return new Response(JSON.stringify({ error: "Invalid purchase signature — purchase is not genuine" }), { status: 403, headers: corsHeaders });
					}

					// Signature verified — extract product ID from the purchase JSON
					const purchaseData = JSON.parse(originalJson);
					const productId = purchaseData.productId;

					// Map product to tier
					const targetTier = PRODUCT_TIER_MAP[productId];
					if (!targetTier) {
						return new Response(JSON.stringify({ error: "Unknown product ID: " + productId }), { status: 400, headers: corsHeaders });
					}

					// Purchase is genuine — grant tier
					await env.DB.prepare(`
						UPDATE Users SET tier = ?, last_seen = CURRENT_TIMESTAMP WHERE uid = ?
					`).bind(targetTier, uid).run();

					return new Response(JSON.stringify({ success: true, tier: targetTier }), {
						headers: { "Content-Type": "application/json", ...corsHeaders }
					});

				} catch (e) {
					console.error('Subscription verification error:', e.message);
					return new Response(JSON.stringify({ error: "Purchase verification failed" }), { status: 500, headers: corsHeaders });
				}
			}

			// ==========================================
			// ENDPOINT: GET /api/users/:uid/tier
			// Returns the server-authoritative tier for a user
			// ==========================================
			if (method === "GET" && path.match(/^\/api\/users\/[^\/]+\/tier$/)) {
				const uid = decodeURIComponent(path.split('/')[3]);
				const user = await env.DB.prepare(`SELECT tier FROM Users WHERE uid = ?`).bind(uid).first();

				return new Response(JSON.stringify({
					success: true,
					tier: user ? user.tier : 'FREE'
				}), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: GET /api/reviews
			// Gets reviews for a specific template
			// ==========================================
			if (method === "GET" && path.startsWith("/api/reviews")) {
				const templateId = url.searchParams.get("templateId");
				if (!templateId) return new Response("Missing templateId", { status: 400, headers: corsHeaders });

				const { results } = await env.DB.prepare(
					`SELECT id, template_id, user_id, score, review_text, images, timestamp, status, admin_reason 
					 FROM Reviews WHERE template_id = ? ORDER BY timestamp DESC`
				).bind(templateId).all();

				return new Response(JSON.stringify({ success: true, reviews: results }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/reviews
			// Submit a new review
			// ==========================================
			if (method === "POST" && path === "/api/reviews") {
				const body = await request.json();
				const { template_id, user_id, score, review_text, images } = body;

				if (!template_id || !score || !review_text) {
					return new Response(JSON.stringify({ error: "Missing required fields" }), { status: 400, headers: corsHeaders });
				}

				const reviewId = crypto.randomUUID();

				await env.DB.prepare(
					`INSERT INTO Reviews (id, template_id, user_id, score, review_text, images) VALUES (?, ?, ?, ?, ?, ?)`
				).bind(reviewId, template_id, user_id || "anonymous", score, review_text, images ? JSON.stringify(images) : null).run();

				return new Response(JSON.stringify({ success: true, id: reviewId }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: PUT /api/reviews/:id
			// Update an existing review
			// ==========================================
			if (method === "PUT" && path.startsWith("/api/reviews/")) {
				const id = path.split('/')[3];
				const body = await request.json();
				const { user_id, score, review_text, images } = body;

				if (!user_id || !score || !review_text) {
					return new Response(JSON.stringify({ error: "Missing required fields" }), { status: 400, headers: corsHeaders });
				}

				const { success } = await env.DB.prepare(
					`UPDATE Reviews SET score = ?, review_text = ?, images = ? WHERE id = ? AND user_id = ?`
				).bind(score, review_text, images ? JSON.stringify(images) : null, id, user_id).run();

				return new Response(JSON.stringify({ success: true }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: DELETE /api/reviews/:id
			// Delete an existing review
			// ==========================================
			if (method === "DELETE" && path.startsWith("/api/reviews/")) {
				const id = path.split('/')[3];
				const user_id = url.searchParams.get("userId");

				if (!user_id) return new Response("Missing userId", { status: 400, headers: corsHeaders });

				await env.DB.prepare(
					`DELETE FROM Reviews WHERE id = ? AND user_id = ?`
				).bind(id, user_id).run();

				return new Response(JSON.stringify({ success: true }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ADMIN ENDPOINT: GET /api/admin/templates
			// Fetches ALL templates regardless of status
			// ==========================================
			if (method === "GET" && path === "/api/admin/templates") {
				const { results } = await env.DB.prepare(
					`SELECT t.id, t.user_id, t.title, t.content, t.thumbnail, t.status, t.created_at,
					 (SELECT AVG(score) FROM Reviews WHERE template_id = t.id) as avg_rating,
					 (SELECT COUNT(score) FROM Reviews WHERE template_id = t.id) as rating_count,
					 (LENGTH(t.content) + COALESCE(LENGTH(t.thumbnail), 0) + 
					  COALESCE((SELECT SUM(LENGTH(review_text)) FROM Reviews WHERE template_id = t.id), 0) +
					  COALESCE((SELECT SUM(LENGTH(tag_name)) FROM Tags WHERE template_id = t.id), 0)) as size_bytes
					 FROM Templates t 
					 ORDER BY t.created_at DESC`
				).all();

				return new Response(JSON.stringify({ success: true, templates: results }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ADMIN ENDPOINT: GET /api/admin/stats
			// Fetches total storage, publish status, and banned users count
			// ==========================================
			if (method === "GET" && path === "/api/admin/stats") {
				const storageRes = await env.DB.prepare(`
					SELECT (
						SELECT SUM(LENGTH(content) + COALESCE(LENGTH(thumbnail), 0)) FROM Templates
					) + (
						SELECT COALESCE(SUM(LENGTH(review_text)), 0) FROM Reviews
					) + (
						SELECT COALESCE(SUM(LENGTH(tag_name)), 0) FROM Tags
					) as total_bytes
				`).first();
				const bannedRes = await env.DB.prepare(`SELECT COUNT(*) as count FROM BannedUsers`).first();
				const settingsRes = await env.DB.prepare(`SELECT value FROM Settings WHERE id = 'global_publish_allowed'`).first();

				return new Response(JSON.stringify({
					success: true,
					total_bytes: storageRes ? storageRes.total_bytes : 0,
					banned_count: bannedRes ? bannedRes.count : 0,
					global_publish_allowed: settingsRes ? settingsRes.value === 'true' : true
				}), { headers: { "Content-Type": "application/json", ...corsHeaders } });
			}

			// ==========================================
			// ADMIN ENDPOINT: POST /api/admin/settings/publish
			// Toggles the global publish lock
			// ==========================================
			if (method === "POST" && path === "/api/admin/settings/publish") {
				const body = await request.json();
				await env.DB.prepare(`UPDATE Settings SET value = ? WHERE id = 'global_publish_allowed'`).bind(body.allowed ? 'true' : 'false').run();
				return new Response(JSON.stringify({ success: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
			}

			// ==========================================
			// ADMIN ENDPOINT: POST /api/admin/users/:id/ban
			// Bans a specific user
			// ==========================================
			if (method === "POST" && path.match(/^\/api\/admin\/users\/[^\/]+\/ban$/)) {
				const banUserId = path.split('/')[4];
				await env.DB.prepare(`INSERT OR IGNORE INTO BannedUsers (user_id) VALUES (?)`).bind(banUserId).run();
				return new Response(JSON.stringify({ success: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
			}

			// ==========================================
			// ADMIN ENDPOINT: DELETE /api/admin/users/:id/ban
			// Unbans a specific user
			// ==========================================
			if (method === "DELETE" && path.match(/^\/api\/admin\/users\/[^\/]+\/ban$/)) {
				const banUserId = path.split('/')[4];
				await env.DB.prepare(`DELETE FROM BannedUsers WHERE user_id = ?`).bind(banUserId).run();
				return new Response(JSON.stringify({ success: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
			}

			// ==========================================
			// ADMIN ENDPOINT: POST /api/admin/templates/:id/status
			// Updates the status of a template
			// ==========================================
			if (method === "POST" && path.match(/^\/api\/admin\/templates\/[^\/]+\/status$/)) {
				const templateId = path.split('/')[4];
				const body = await request.json();
				const { status } = body;

				if (!['pending', 'accepted', 'rejected', 'hidden'].includes(status)) {
					return new Response(JSON.stringify({ error: "Invalid status" }), { status: 400, headers: corsHeaders });
				}

				await env.DB.prepare(`UPDATE Templates SET status = ? WHERE id = ?`)
					.bind(status, templateId)
					.run();

				return new Response(JSON.stringify({ success: true }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ADMIN ENDPOINT: POST /api/admin/reviews/:id/moderate
			// Moderates a review (deletes it with a reason)
			// ==========================================
			if (method === "POST" && path.match(/^\/api\/admin\/reviews\/[^\/]+\/moderate$/)) {
				const reviewId = path.split('/')[4];
				const body = await request.json();
				const { reason } = body;

				if (!reason) return new Response("Missing reason", { status: 400, headers: corsHeaders });

				await env.DB.prepare(`UPDATE Reviews SET status = 'deleted_by_admin', admin_reason = ? WHERE id = ?`)
					.bind(reason, reviewId)
					.run();

				return new Response(JSON.stringify({ success: true }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ADMIN ENDPOINT: DELETE /api/admin/templates/:id
			// Permanently deletes a template (optional)
			// ==========================================
			if (method === "DELETE" && path.match(/^\/api\/admin\/templates\/[^\/]+$/)) {
				const templateId = path.split('/')[4];

				// Delete from Tags and Reviews first to avoid orphans (if FK constraints aren't set to cascade)
				await env.DB.prepare(`DELETE FROM Tags WHERE template_id = ?`).bind(templateId).run();
				await env.DB.prepare(`DELETE FROM Reviews WHERE template_id = ?`).bind(templateId).run();
				await env.DB.prepare(`DELETE FROM Templates WHERE id = ?`).bind(templateId).run();

				return new Response(JSON.stringify({ success: true }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ADMIN ENDPOINT: GET /api/admin/users
			// Fetches the centralized User Registry
			// ==========================================
			if (method === "GET" && path === "/api/admin/users") {
				const { results } = await env.DB.prepare(
					`SELECT uid, email, name, ip, device_model, android_version, tier, last_seen 
					 FROM Users 
					 ORDER BY last_seen DESC`
				).all();

				return new Response(JSON.stringify({ success: true, users: results }), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			// ==========================================
			// ENDPOINT: POST /api/ai/chat
			// Proxies requests to Gemini 3 Flash with a 1000-request daily limit
			// ==========================================
			if (method === "POST" && path === "/api/ai/chat") {
				const body = await request.json();
				const { prompt, model } = body;
				
				if (!prompt) return new Response("Missing prompt", { status: 400, headers: corsHeaders });

				const today = new Date().toISOString().split('T')[0];

				// 1. Check/Increment Quota in D1
				// Ensure record exists for today
				await env.DB.prepare(`INSERT OR IGNORE INTO AiUsage (day, request_count) VALUES (?, 0)`).bind(today).run();
				
				// Get current count
				const usage = await env.DB.prepare(`SELECT request_count FROM AiUsage WHERE day = ?`).bind(today).first();
				
				if (usage && usage.request_count >= 1000) {
					return new Response(JSON.stringify({ 
						success: false, 
						error: "Daily Global Quota Reached (1,000 requests). Try again tomorrow." 
					}), { status: 403, headers: { "Content-Type": "application/json", ...corsHeaders } });
				}

				// 2. Proxy to Google Gemini API
				const GEMINI_API_KEY = env.GEMINI_API_KEY;
				if (!GEMINI_API_KEY) {
					return new Response("Server AI configuration error (Missing API Key)", { status: 500, headers: corsHeaders });
				}

				const targetModel = model || "gemini-2.5-flash";
				
				const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${targetModel}:generateContent?key=${GEMINI_API_KEY}`;
				
				const geminiResponse = await fetch(geminiUrl, {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({
						contents: [{ parts: [{ text: prompt }] }]
					})
				});

				const aiData = await geminiResponse.json();

				// 3. Increment usage count on success
				if (geminiResponse.ok) {
					await env.DB.prepare(`UPDATE AiUsage SET request_count = request_count + 1, last_updated = CURRENT_TIMESTAMP WHERE day = ?`).bind(today).run();
				}

				return new Response(JSON.stringify(aiData), {
					headers: { "Content-Type": "application/json", ...corsHeaders }
				});
			}

			return new Response("Vitae Backend API Check", { status: 200, headers: corsHeaders });

		} catch (e) {
			// Do not expose internal error details to clients
			console.error('Internal server error:', e.message);
			return new Response(JSON.stringify({ error: "Internal server error" }), { status: 500, headers: { "Content-Type": "application/json", ...corsHeaders } });
		}
	},
};
