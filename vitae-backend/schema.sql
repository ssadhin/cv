-- Drop existing tables to start fresh
DROP TABLE IF EXISTS Tags;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Templates;
DROP TABLE IF EXISTS BannedUsers;
DROP TABLE IF EXISTS Settings;
DROP TABLE IF EXISTS Users;

-- Templates Table
CREATE TABLE Templates (
    id TEXT PRIMARY KEY,
    user_id TEXT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    thumbnail TEXT,
    status TEXT DEFAULT 'pending', -- Added for moderation
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tags Table
CREATE TABLE Tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    template_id TEXT NOT NULL,
    tag_name TEXT NOT NULL,
    FOREIGN KEY (template_id) REFERENCES Templates(id) ON DELETE CASCADE
);

-- Reviews Table
CREATE TABLE Reviews (
    id TEXT PRIMARY KEY,
    template_id TEXT NOT NULL,
    user_id TEXT,
    score INTEGER NOT NULL CHECK (score >= 0 AND score <= 10),
    review_text TEXT NOT NULL,
    images TEXT,
    status TEXT DEFAULT 'active',
    admin_reason TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES Templates(id) ON DELETE CASCADE
);

-- Settings Table
CREATE TABLE Settings (
    id TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

-- Banned Users Table
CREATE TABLE BannedUsers (
    user_id TEXT PRIMARY KEY,
    banned_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Users Registry Table
CREATE TABLE Users (
    uid TEXT PRIMARY KEY,
    email TEXT,
    name TEXT,
    ip TEXT,
    device_model TEXT,
    android_version TEXT,
    tier TEXT DEFAULT 'FREE',
    tier_source TEXT DEFAULT 'NONE',
    trial_start DATETIME DEFAULT CURRENT_TIMESTAMP,
    ai_count INTEGER DEFAULT 0,
    template_count INTEGER DEFAULT 0,
    last_reset_month TEXT,
    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Initialize Settings
INSERT INTO Settings (id, value) VALUES ('global_publish_allowed', 'true');

-- Create some dummy indexes for fast lookups
CREATE INDEX idx_templates_created ON Templates(created_at DESC);
CREATE INDEX idx_reviews_template ON Reviews(template_id);
CREATE INDEX idx_tags_template ON Tags(template_id);
CREATE INDEX idx_templates_status ON Templates(status);
 
 -- AI Usage Tracking Table
 CREATE TABLE IF NOT EXISTS AiUsage (
     day TEXT PRIMARY KEY, -- Format: YYYY-MM-DD
     request_count INTEGER DEFAULT 0,
     last_updated DATETIME DEFAULT CURRENT_TIMESTAMP
 );
 
 -- Initialize AiUsage for today (if not exists)
 INSERT OR IGNORE INTO AiUsage (day, request_count) VALUES (CURRENT_DATE, 0);
