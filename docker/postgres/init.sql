-- This file is executed when the PostgreSQL container starts for the first time
-- Additional database initialization can be added here

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Log successful initialization
SELECT 'Database initialized successfully' as status;
