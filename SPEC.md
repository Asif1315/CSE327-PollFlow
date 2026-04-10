# PollFlow - Polling System Specification

## Project Overview
- **Project Name**: PollFlow
- **Type**: Spring Boot REST API with MySQL
- **Core Functionality**: A dual-admin verification polling system where users can participate in polls after admin verification
- **Target Users**: General users, Poll Admins, Verification Admins

## Technology Stack
- Java 17 + Spring Boot 3.x
- MySQL Database
- JWT Authentication
- Maven
- Google Gemini AI (for chat assistant)

## Features

### User Features
- User Registration & Login
- Poll Voting (one vote per user per poll)
- 12 Pre-configured Categories
- Favorites (save polls)
- Notifications
- User Profile (gender, age, region, city, religion, marital status)
- Password toggle on login/register

### Admin Features

#### Poll Admin
- Create new polls with multiple options
- Edit polls (if no votes cast yet)
- Delete polls (soft delete)
- View analytics dashboard
- Poll types: OPEN (always active) and TIME_BASED (ends at specific time)
- Hide results option until poll ends

#### Verification Admin
- View all pending user registrations
- Approve or reject user accounts
- Provide rejection reason for rejected users
- View user list with filters (gender, age, region, city, religion, marital status)
- View analytics dashboard

### AI Chat Feature
- Gemini-powered AI assistant on poll pages
- Floating chat button for quick access
- Chat context maintained during session
- Poll-specific AI insights

### Navigation
- Role-specific admin dashboard navigation:
  - Poll Admin: Dashboard, Create Poll, Analytics, Users
  - Verification Admin: Dashboard, Analytics, Users (no Create Poll)

## Database Schema

### Users Table
- id (BIGINT, PK, AUTO_INCREMENT)
- email (VARCHAR 255, UNIQUE, NOT NULL)
- password (VARCHAR 255, NOT NULL)
- fullName (VARCHAR 255, NOT NULL)
- role (ENUM: USER, POLL_ADMIN, VERIFICATION_ADMIN)
- status (ENUM: PENDING, APPROVED, REJECTED)
- rejectionReason (TEXT, NULLABLE)
- profilePicture (VARCHAR 500, NULLABLE)
- bio (TEXT, NULLABLE)
- mobile (VARCHAR 20, NULLABLE)
- gender (VARCHAR 20, NULLABLE) - Male, Female, Other
- age (INT, NULLABLE)
- region (VARCHAR 50, NULLABLE) - Dhaka, Chittagong, etc.
- city (VARCHAR 50, NULLABLE)
- postalCode (VARCHAR 20, NULLABLE)
- religion (VARCHAR 30, NULLABLE) - Islam, Hinduism, etc.
- address (TEXT, NULLABLE)
- maritalStatus (VARCHAR 20, NULLABLE) - Single, Married, Divorced, Widowed, Separated
- createdAt (TIMESTAMP)
- updatedAt (TIMESTAMP)

### Categories Table (Pre-configured)
- id (BIGINT, PK, AUTO_INCREMENT)
- name (VARCHAR 100, UNIQUE)
- icon (VARCHAR 100)
- displayOrder (INT)

### Polls Table
- id (BIGINT, PK, AUTO_INCREMENT)
- title (VARCHAR 255, NOT NULL)
- description (TEXT)
- categoryId (BIGINT, FK)
- createdBy (BIGINT, FK - User)
- isDeleted (BOOLEAN, DEFAULT FALSE)
- pollType (ENUM: OPEN, TIME_BASED)
- endTime (TIMESTAMP, NULLABLE)
- hideResults (BOOLEAN, DEFAULT FALSE)
- createdAt (TIMESTAMP)
- updatedAt (TIMESTAMP)

### PollOptions Table
- id (BIGINT, PK, AUTO_INCREMENT)
- pollId (BIGINT, FK)
- optionText (VARCHAR 255, NOT NULL)
- voteCount (INT, DEFAULT 0)

### Votes Table
- id (BIGINT, PK, AUTO_INCREMENT)
- pollId (BIGINT, FK)
- userId (BIGINT, FK)
- optionId (BIGINT, FK)
- votedAt (TIMESTAMP)
- UNIQUE(pollId, userId)

### Favorites Table
- id (BIGINT, PK, AUTO_INCREMENT)
- userId (BIGINT, FK)
- pollId (BIGINT, FK)
- addedAt (TIMESTAMP)
- UNIQUE(userId, pollId)

### Notifications Table
- id (BIGINT, PK, AUTO_INCREMENT)
- userId (BIGINT, FK)
- title (VARCHAR 255)
- message (TEXT)
- isRead (BOOLEAN, DEFAULT FALSE)
- createdAt (TIMESTAMP)

## API Endpoints

### Authentication
- POST /api/auth/register - Register new user (status: PENDING)
- POST /api/auth/login - Login (only APPROVED users can login)
- GET /api/auth/me - Get current user

### Users
- GET /api/users/profile - Get user profile
- PUT /api/users/profile - Update profile
- GET /api/admin/users/pending - Get pending users (Verification Admin)
- PUT /api/admin/users/{id}/approve - Approve user
- PUT /api/admin/users/{id}/reject - Reject user with reason

### Polls
- GET /api/polls - Get all active polls (with pagination)
- GET /api/polls/{id} - Get poll details
- POST /api/polls - Create poll (Poll Admin)
- PUT /api/polls/{id} - Update poll (Poll Admin, only if no votes)
- DELETE /api/polls/{id} - Soft delete poll (Poll Admin)
- GET /api/polls/category/{categoryId} - Get polls by category
- GET /api/polls/user/{userId} - Get polls created by user
- POST /api/polls/{id}/vote - Vote on poll
- GET /api/polls/{id}/results - Get poll results

### Favorites
- GET /api/favorites - Get user favorites
- POST /api/favorites/{pollId} - Add to favorites
- DELETE /api/favorites/{pollId} - Remove from favorites

### Categories
- GET /api/categories - Get all categories

### Notifications
- GET /api/notifications - Get user notifications
- PUT /api/notifications/{id}/read - Mark as read
- PUT /api/notifications/read-all - Mark all as read

### AI Chat
- GET /api/ai/status - Get AI service status (public)
- GET /api/ai/models - Get available AI models (public)
- POST /api/ai/chat - Send chat message to AI
- POST /api/ai/chat/poll/{pollId} - Get AI insights for specific poll

### Analytics (Poll Admin)
- GET /api/admin/analytics/dashboard - Dashboard stats
- GET /api/admin/analytics/poll/{id} - Poll specific analytics

## Security
- JWT Token Authentication
- Role-based access control (USER, POLL_ADMIN, VERIFICATION_ADMIN)
- Password encoded with BCrypt

## Pre-configured Categories (12)
1. Politics
2. Sports
3. Entertainment
4. Technology
5. Business
6. World News
7. National
8. Science
9. Health
10. Environment
11. Education
12. Lifestyle
