-- Add role column to users table if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'ATTENDEE';

-- Update existing users to have ATTENDEE role if null
UPDATE users SET role = 'ATTENDEE' WHERE role IS NULL;
