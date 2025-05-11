-- Create users table.
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create sleep_logs table.
CREATE TABLE IF NOT EXISTS sleep_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, 
    sleep_date DATE NOT NULL,
    time_in_bed_start TIMESTAMP WITH TIME ZONE NOT NULL,
    time_in_bed_end TIMESTAMP WITH TIME ZONE NOT NULL,
    total_time_in_bed_minutes INTEGER NOT NULL,
    morning_feeling TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_morning_feeling CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD')),

    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_time_order CHECK (time_in_bed_end > time_in_bed_start),

    CONSTRAINT uq_user_sleep_date UNIQUE (user_id, sleep_date)
);

-- Create index for user_id and sleep_date.
CREATE INDEX IF NOT EXISTS idx_sleep_logs_user_id_sleep_date ON sleep_logs (user_id, sleep_date DESC);