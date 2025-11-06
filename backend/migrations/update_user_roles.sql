-- Update specific users to have ORGANIZER or STAFF roles
-- Replace the email/username values with your actual user identifiers

-- Update users to ORGANIZER role
-- Example: UPDATE users SET role = 'ORGANIZER' WHERE email = 'organizer@example.com';
UPDATE users SET role = 'ORGANIZER' WHERE email LIKE '%organizer%' OR name LIKE '%Organizer%';

-- Update users to STAFF role
-- Example: UPDATE users SET role = 'STAFF' WHERE email = 'staff@example.com';
UPDATE users SET role = 'STAFF' WHERE email LIKE '%staff%' OR name LIKE '%Staff%';

-- Or update by specific usernames
-- UPDATE users SET role = 'ORGANIZER' WHERE name = 'Organizer3';
-- UPDATE users SET role = 'STAFF' WHERE name = 'Staff1';

-- Verify the changes
SELECT id, name, email, role FROM users ORDER BY role, name;
