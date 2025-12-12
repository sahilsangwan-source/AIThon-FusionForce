package com.lms.userservice.constant;

/**
 * Application constants
 */
public class AppConstants {

    public static final String API_PREFIX = "/api";
    public static final String USERS_API = API_PREFIX + "/users";
    public static final String AUTH_API = API_PREFIX + "/auth";

    // Roles
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    // User Status
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_INACTIVE = "INACTIVE";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";

    // JWT
    public static final String JWT_SECRET = "your-secret-key-change-this-in-production";
    public static final long JWT_EXPIRATION = 3600000; // 1 hour in milliseconds
    public static final long JWT_REFRESH_EXPIRATION = 604800000; // 7 days in milliseconds

    // Redis
    public static final String REDIS_KEY_USER = "user:";
    public static final String REDIS_KEY_SESSION = "session:";

    // Kafka Topics
    public static final String KAFKA_TOPIC_USER_REGISTERED = "user.registered";
    public static final String KAFKA_TOPIC_USER_UPDATED = "user.updated";
    public static final String KAFKA_TOPIC_USER_DELETED = "user.deleted";
}
