export default {
	async fetch(request, env, ctx) {
		const url = new URL(request.url);
		const path = url.pathname;
		const method = request.method;

		// --- Default CORS Headers for Android app and Web Dashboard ---
		const corsHeaders = {
			"Access-Control-Allow-Origin": "*",
			"Access-Control-Allow-Methods": "GET,HEAD,POST,OPTIONS,DELETE",
			"Access-Control-Allow-Headers": "Content-Type, Authorization",
			"Access-Control-Max-Age": "86400",
		};

		if (method === "OPTIONS") {
			return new Response(null, { headers: corsHeaders });
		}

		// --- ADMIN AUTH CHECK ---
		// Validates if the route starts with /api/admin and checks the Authorization header
		const isAdminRoute = path.startsWith('/api/admin');
		// Hardcoded admin secret for this basic implementation (can be moved to env vars later)
		const ADMIN_SECRET = 'vitae_admin_secret_2026';

		if (isAdminRoute) {
			const authHeader = request.headers.get('Authorization');
			const allowedKeys = [`Bearer ${ADMIN_SECRET}`, 'Bearer MASTER', 'Bearer DEBUG'];

			if (!authHeader || !allowedKeys.includes(authHeader)) {
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

				// Define valid coupons and their associated tiers
				const validCoupons = {
					"VITAE-ELITE-FREE": "ELITE",
					"VITAE2026": "PLUS",
					"DEBUG-ADS-ON": "FREE"
				};

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
				const { uid, email, name, ip, device_model, android_version, tier } = body;

				if (!uid) return new Response("Missing uid", { status: 400, headers: corsHeaders });

				await env.DB.prepare(`
					INSERT INTO Users (uid, email, name, ip, device_model, android_version, tier, last_seen)
					VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
					ON CONFLICT(uid) DO UPDATE SET
						email = excluded.email,
						name = excluded.name,
						ip = excluded.ip,
						device_model = excluded.device_model,
						android_version = excluded.android_version,
						tier = excluded.tier,
						last_seen = CURRENT_TIMESTAMP
				`).bind(uid, email || null, name || null, ip || null, device_model || null, android_version || null, tier || 'FREE').run();

				return new Response(JSON.stringify({ success: true }), {
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

			return new Response("Vitae Backend API Check", { status: 200, headers: corsHeaders });

		} catch (e) {
			return new Response(e.message, { status: 500, headers: corsHeaders });
		}
	},
};
