-- Learning & Development Portal - Database Initialization Script
-- This script creates all necessary tables for the microservices

-- ============================================
-- USER SERVICE TABLES
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(100),
    role VARCHAR(50) DEFAULT 'EMPLOYEE',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    permissions JSONB
);

-- User roles mapping
CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- SSO providers
CREATE TABLE sso_providers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_name VARCHAR(50) NOT NULL,
    client_id VARCHAR(255),
    client_secret VARCHAR(255),
    config JSONB
);

-- User sessions
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500),
    refresh_token VARCHAR(500),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TRAINING SERVICE TABLES
-- ============================================

-- Trainings table
CREATE TABLE trainings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    difficulty_level VARCHAR(20),
    duration_hours DECIMAL(5,2),
    thumbnail_url VARCHAR(500),
    created_by UUID REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Training modules
CREATE TABLE training_modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sequence_order INT,
    content_type VARCHAR(50),
    estimated_duration_minutes INT
);

-- Training content
CREATE TABLE training_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID REFERENCES training_modules(id) ON DELETE CASCADE,
    content_url VARCHAR(500),
    content_type VARCHAR(50),
    file_size BIGINT,
    metadata JSONB
);

-- Training prerequisites
CREATE TABLE training_prerequisites (
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    prerequisite_training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    PRIMARY KEY (training_id, prerequisite_training_id)
);

-- Training tags
CREATE TABLE training_tags (
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    tag VARCHAR(50),
    PRIMARY KEY (training_id, tag)
);

-- Training enrollments (user-training relationships)
CREATE TABLE training_enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    enrollment_status VARCHAR(50) DEFAULT 'ENROLLED',
    progress_percentage INT DEFAULT 0,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, training_id)
);

-- Index for faster queries
CREATE INDEX idx_enrollment_user ON training_enrollments(user_id);
CREATE INDEX idx_enrollment_training ON training_enrollments(training_id);
CREATE INDEX idx_enrollment_status ON training_enrollments(enrollment_status);

-- Quizzes
CREATE TABLE quizzes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID REFERENCES training_modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    passing_score INT,
    time_limit_minutes INT
);

-- Quiz questions
CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20),
    options JSONB,
    correct_answer JSONB,
    points INT
);

-- ============================================
-- ASSIGNMENT SERVICE TABLES
-- ============================================

-- Assignments
CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    training_id UUID REFERENCES trainings(id),
    assigned_by UUID REFERENCES users(id),
    assignment_type VARCHAR(20) DEFAULT 'OPTIONAL',
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User assignments
CREATE TABLE user_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assignment_id UUID REFERENCES assignments(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    completion_percentage INT DEFAULT 0
);

-- Mandatory trainings
CREATE TABLE mandatory_trainings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    training_id UUID REFERENCES trainings(id),
    role VARCHAR(50),
    department VARCHAR(100),
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

-- Assignment reminders
CREATE TABLE assignment_reminders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_assignment_id UUID REFERENCES user_assignments(id) ON DELETE CASCADE,
    reminder_type VARCHAR(20),
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP
);

-- ============================================
-- PROGRESS SERVICE TABLES
-- ============================================

-- User progress
CREATE TABLE user_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    module_id UUID REFERENCES training_modules(id) ON DELETE CASCADE,
    progress_percentage INT DEFAULT 0,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_spent_minutes INT DEFAULT 0,
    UNIQUE(user_id, training_id, module_id)
);

-- Quiz attempts
CREATE TABLE quiz_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
    score INT,
    max_score INT,
    attempt_number INT,
    answers JSONB,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Video progress
CREATE TABLE video_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    video_id UUID,
    watched_duration INT DEFAULT 0,
    total_duration INT,
    last_position INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, video_id)
);

-- Completion certificates
CREATE TABLE completion_certificates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    certificate_url VARCHAR(500),
    verification_code VARCHAR(50) UNIQUE
);

-- Learning path progress
CREATE TABLE learning_path_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    path_id UUID,
    completed_trainings INT DEFAULT 0,
    total_trainings INT,
    progress_percentage INT DEFAULT 0
);

-- ============================================
-- NOTIFICATION SERVICE TABLES
-- ============================================

-- Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50),
    title VARCHAR(255),
    message TEXT,
    read_status BOOLEAN DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification preferences
CREATE TABLE notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    email_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    digest_frequency VARCHAR(20) DEFAULT 'DAILY'
);

-- Notification templates
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50) UNIQUE NOT NULL,
    channel VARCHAR(20),
    subject VARCHAR(255),
    body_template TEXT,
    variables JSONB
);

-- Notification delivery log
CREATE TABLE notification_delivery_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_id UUID REFERENCES notifications(id) ON DELETE CASCADE,
    channel VARCHAR(20),
    status VARCHAR(20),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT
);

-- ============================================
-- CONTENT SERVICE TABLES
-- ============================================

-- Content metadata
CREATE TABLE content_metadata (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_type VARCHAR(50),
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    storage_path VARCHAR(500),
    cdn_url VARCHAR(500),
    uploaded_by UUID REFERENCES users(id),
    upload_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Video metadata
CREATE TABLE video_metadata (
    content_id UUID PRIMARY KEY REFERENCES content_metadata(id) ON DELETE CASCADE,
    duration_seconds INT,
    resolution VARCHAR(20),
    codec VARCHAR(50),
    thumbnail_url VARCHAR(500),
    hls_playlist_url VARCHAR(500)
);

-- Content access log
CREATE TABLE content_access_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID REFERENCES content_metadata(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50)
);

-- ============================================
-- ANALYTICS SERVICE TABLES
-- ============================================

-- Training analytics
CREATE TABLE training_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    training_id UUID REFERENCES trainings(id) ON DELETE CASCADE,
    total_enrollments INT DEFAULT 0,
    total_completions INT DEFAULT 0,
    average_score DECIMAL(5,2),
    average_completion_time_hours DECIMAL(8,2),
    completion_rate DECIMAL(5,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User analytics
CREATE TABLE user_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    total_trainings_completed INT DEFAULT 0,
    total_hours_learned DECIMAL(8,2),
    average_quiz_score DECIMAL(5,2),
    last_activity_at TIMESTAMP,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Department analytics
CREATE TABLE department_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    department VARCHAR(100),
    total_users INT DEFAULT 0,
    active_users INT DEFAULT 0,
    completion_rate DECIMAL(5,2),
    average_score DECIMAL(5,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Report requests
CREATE TABLE report_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    requested_by UUID REFERENCES users(id),
    report_type VARCHAR(50),
    filters JSONB,
    status VARCHAR(20) DEFAULT 'PENDING',
    file_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- ============================================
-- WORKFLOW SERVICE TABLES
-- ============================================

-- Workflows
CREATE TABLE workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_type VARCHAR(50),
    entity_id UUID,
    entity_type VARCHAR(50),
    initiated_by UUID REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Workflow steps
CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id UUID REFERENCES workflows(id) ON DELETE CASCADE,
    step_number INT,
    approver_role VARCHAR(50),
    approver_id UUID REFERENCES users(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    comments TEXT,
    approved_at TIMESTAMP
);

-- Workflow templates
CREATE TABLE workflow_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_type VARCHAR(50) UNIQUE NOT NULL,
    steps JSONB,
    created_by UUID REFERENCES users(id)
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- User Service Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(token);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);

-- Training Service Indexes
CREATE INDEX idx_trainings_category ON trainings(category);
CREATE INDEX idx_trainings_status ON trainings(status);
CREATE INDEX idx_trainings_created_by ON trainings(created_by);
CREATE INDEX idx_training_modules_training_id ON training_modules(training_id);
CREATE INDEX idx_training_content_module_id ON training_content(module_id);

-- Assignment Service Indexes
CREATE INDEX idx_assignments_training_id ON assignments(training_id);
CREATE INDEX idx_user_assignments_user_id ON user_assignments(user_id);
CREATE INDEX idx_user_assignments_status ON user_assignments(status);
CREATE INDEX idx_user_assignments_user_status ON user_assignments(user_id, status);

-- Progress Service Indexes
CREATE INDEX idx_user_progress_user_training ON user_progress(user_id, training_id);
CREATE INDEX idx_quiz_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX idx_video_progress_user_video ON video_progress(user_id, video_id);

-- Notification Service Indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_status ON notifications(read_status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Content Service Indexes
CREATE INDEX idx_content_metadata_uploaded_by ON content_metadata(uploaded_by);
CREATE INDEX idx_content_access_log_content_id ON content_access_log(content_id);
CREATE INDEX idx_content_access_log_user_id ON content_access_log(user_id);

-- Analytics Service Indexes
CREATE INDEX idx_training_analytics_training_id ON training_analytics(training_id);
CREATE INDEX idx_user_analytics_user_id ON user_analytics(user_id);
CREATE INDEX idx_department_analytics_department ON department_analytics(department);

-- Workflow Service Indexes
CREATE INDEX idx_workflows_entity_id ON workflows(entity_id);
CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflow_steps_workflow_id ON workflow_steps(workflow_id);

-- ============================================
-- SEED DATA
-- ============================================

-- Insert default roles

-- Insert roles with correct JSON arrays
INSERT INTO roles (name, permissions) VALUES
('EMPLOYEE', '["read:trainings", "write:progress", "read:own_profile"]'::jsonb),
('ADMIN', '["read:trainings", "write:trainings", "read:users", "write:assignments", "read:analytics"]'::jsonb),
('SUPER_ADMIN', '["read:*", "write:*", "delete:*"]'::jsonb);

-- Insert default notification templates
INSERT INTO notification_templates (type, channel, subject, body_template, variables) VALUES
('TRAINING_ASSIGNED', 'EMAIL', 'New Training Assigned',
 'Hello {{user_name}}, you have been assigned a new training: {{training_title}}',
 '{"user_name": "string", "training_title": "string"}'::jsonb),

('TRAINING_COMPLETED', 'EMAIL', 'Training Completed',
 'Congratulations {{user_name}}! You have completed {{training_title}}',
 '{"user_name": "string", "training_title": "string"}'::jsonb),

('TRAINING_OVERDUE', 'EMAIL', 'Training Overdue',
 'Reminder: Your training {{training_title}} is overdue',
 '{"user_name": "string", "training_title": "string"}'::jsonb);

-- Insert a default super admin user (password: Admin@123)
INSERT INTO users (email, password_hash, first_name, last_name, employee_id, department, role, status)
VALUES ('admin@company.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Super', 'Admin', 'EMP001', 'IT', 'SUPER_ADMIN', 'ACTIVE');

-- Link super admin to SUPER_ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@company.com' AND r.name = 'SUPER_ADMIN';

-- Insert default notification preferences for admin
INSERT INTO notification_preferences (user_id, email_enabled, push_enabled, in_app_enabled, sms_enabled)
SELECT id, TRUE, TRUE, TRUE, FALSE FROM users WHERE email = 'admin@company.com';

COMMIT;