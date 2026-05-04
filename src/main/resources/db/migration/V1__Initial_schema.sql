-- Initial database schema for URLShortener Pro

-- Shortened URLs table
CREATE TABLE urls (
                      id BIGSERIAL PRIMARY KEY,
                      short_code VARCHAR(10) UNIQUE NOT NULL,
                      original_url VARCHAR(2048) NOT NULL,
                      user_id VARCHAR(255),
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      expires_at TIMESTAMP,
                      click_count BIGINT NOT NULL DEFAULT 0,
                      is_active BOOLEAN NOT NULL DEFAULT true,
                      description TEXT,
                      tags VARCHAR(255)
);

-- Create indexes for common queries
CREATE INDEX idx_short_code ON urls(short_code);
CREATE INDEX idx_user_id ON urls(user_id);
CREATE INDEX idx_created_at ON urls(created_at);
CREATE INDEX idx_is_active ON urls(is_active);
CREATE INDEX idx_expires_at ON urls(expires_at);

-- Click analytics table (column name must match the @Column annotation)
CREATE TABLE analytics (
                           id BIGSERIAL PRIMARY KEY,
                           url_id BIGINT NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
                           ip_address VARCHAR(45) NOT NULL,
                           user_agent VARCHAR(500),
                           referrer VARCHAR(500),
                           country VARCHAR(100),
                           city VARCHAR(100),
                           clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for analytics queries
CREATE INDEX idx_analytics_url_id ON analytics(url_id);
CREATE INDEX idx_analytics_clicked_at ON analytics(clicked_at);
CREATE INDEX idx_analytics_country ON analytics(country);