CREATE DATABASE IF NOT EXISTS BloodBank;
USE BloodBank;

CREATE TABLE IF NOT EXISTS users (
    uid VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    mobile VARCHAR(20),
    gender VARCHAR(10),
    blood_group VARCHAR(10),
    city VARCHAR(100),
    district VARCHAR(100),
    role ENUM('user','admin') NOT NULL DEFAULT 'user'
);

CREATE TABLE IF NOT EXISTS donors (
    uid VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100),
    blood_group VARCHAR(10),
    city VARCHAR(100),
    mobile VARCHAR(20),
    last_donation_date DATE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (uid) REFERENCES users(uid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blood_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    uid VARCHAR(100),
    name VARCHAR(100),
    mobile VARCHAR(20),
    city VARCHAR(100),
    blood_group VARCHAR(10),
    quantity INT NOT NULL,
    status ENUM('pending','approved','rejected','completed') NOT NULL DEFAULT 'pending',
    is_emergency BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (uid) REFERENCES users(uid) ON DELETE SET NULL,
    CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS blood_inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    blood_group VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NOT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'manual',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS donations (
    donation_id INT AUTO_INCREMENT PRIMARY KEY,
    donor_uid VARCHAR(100),
    blood_group VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    donation_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status ENUM('scheduled','completed','cancelled') NOT NULL DEFAULT 'completed',
    FOREIGN KEY (donor_uid) REFERENCES donors(uid) ON DELETE CASCADE,
    CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS admin_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_uid VARCHAR(100),
    action VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Migration examples for old databases. Run only the missing ALTER statements manually.
-- ALTER TABLE users ADD COLUMN role ENUM('user','admin') NOT NULL DEFAULT 'user';
-- ALTER TABLE donors ADD COLUMN last_donation_date DATE;
-- ALTER TABLE donors ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE;
-- ALTER TABLE blood_requests ADD COLUMN city VARCHAR(100);
-- ALTER TABLE blood_requests ADD COLUMN status ENUM('pending','approved','rejected','completed') NOT NULL DEFAULT 'pending';
-- ALTER TABLE blood_requests ADD COLUMN is_emergency BOOLEAN NOT NULL DEFAULT FALSE;
-- ALTER TABLE blood_requests ADD COLUMN approved_at TIMESTAMP NULL;
-- ALTER TABLE blood_requests ADD COLUMN completed_at TIMESTAMP NULL;
