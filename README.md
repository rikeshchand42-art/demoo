````markdown name=README.md

# 🔐 Secure Java Swing Authentication System

A comprehensive, production-ready authentication and user management system built with Java Swing and SQLite.

## ✨ Features

### 1. **User Authentication**
- Secure login with hashed passwords (SHA-256)
- Username/Email validation
- Failed attempt tracking (max 3 attempts)
- Automatic account lockout (15 minutes)
- Remember me functionality

### 2. **User Registration**
- Complete registration form
- Email and username validation
- Password strength requirements
- Unique user constraint enforcement
- Full user profile setup

### 3. **Forgot Password**
- Token-based password reset
- 24-hour expiration
- Email validation
- Secure token generation

### 4. **Two-Factor Authentication (2FA)**
- Enable 2FA from dashboard
- Secret code generation
- Additional security layer
- Easy enable/disable

### 5. **User Dashboard**
- **Profile Tab**: View and edit user information
- **Login History Tab**: Track all login attempts
- **2FA Tab**: Enable/disable two-factor authentication
- **Admin Panel Tab**: Manage all users (admin only)

### 6. **Admin Panel**
- View all users
- User activity management
- Lock/unlock accounts
- Delete users
- User status tracking

### 7. **Login History Tracking**
- Records all login attempts
- Success/failure tracking
- Timestamp logging
- IP address tracking (demo)

### 8. **Database Integration**
- SQLite database
- Automatic schema creation
- Data persistence
- Multiple tables for different features

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK 8+)
- SQLite JDBC Driver (included in classpath)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/rikeshchand42-art/demoo.git
cd demoo
```

2. **Compile all Java files**
```bash
javac *.java
```

3. **Run the application**
```bash
java LoginForm
```

## 📝 Default Credentials

| Field | Value |
|-------|-------|
| Username | admin |
| Password | password |

> **Note**: Create new accounts via the registration form!

## 📁 File Structure

```
demoo/
├── LoginForm.java           # Main login interface
├── RegistrationForm.java    # User registration
├── Dashboard.java           # Main user dashboard
├── EditProfileForm.java     # Edit profile information
├── ForgotPasswordForm.java  # Password reset
├── Database.java            # Database operations
└── README.md               # This file
```

## 🏗️ Architecture

### Database Schema

**Users Table**
- id (Primary Key)
- username (Unique)
- email (Unique)
- password (SHA-256 hashed)
- full_name
- phone
- created_at
- is_admin
- is_locked
- locked_until

**Login History Table**
- id (Primary Key)
- user_id (Foreign Key)
- login_time
- status (SUCCESS/FAILED)
- ip_address

**Two Factor Auth Table**
- id (Primary Key)
- user_id (Foreign Key, Unique)
- secret_code
- is_enabled

**Password Reset Table**
- id (Primary Key)
- user_id (Foreign Key)
- reset_token (Unique)
- created_at
- expires_at
- is_used

## 🔒 Security Features

✅ **Password Hashing**: SHA-256 algorithm
✅ **Failed Attempt Tracking**: Max 3 attempts before lockout
✅ **Account Lockout**: Automatic 15-minute lockout
✅ **Token-Based Reset**: Unique tokens with expiration
✅ **Input Validation**: All inputs validated
✅ **SQL Injection Prevention**: PreparedStatements used
✅ **2FA Support**: Additional security layer

## 💡 Usage Examples

### Login
1. Enter username: `admin`
2. Enter password: `password`
3. Click "Login"
4. View your dashboard

### Create New User
1. Click "📝 Register" on login form
2. Fill in all required fields
3. Click "✅ Register"
4. Use new credentials to login

### Reset Password
1. Click "❓ Forgot Password" on login form
2. Enter your email
3. Receive reset token (shown in dialog)
4. Use token to reset password

### Enable 2FA
1. Login to your account
2. Go to "🔐 2FA Setup" tab
3. Click "✅ Enable 2FA"
4. Save your secret code securely

## 🔄 Workflow Diagram

```
LoginForm
    ├─→ Valid Credentials → Dashboard
    │   ├─→ Profile Tab (View/Edit)
    │   ├─→ Login History Tab
    │   ├─→ 2FA Tab (Enable/Disable)
    │   └─→ Admin Panel (Admin Only)
    ├─→ Register Button → RegistrationForm
    ├─→ Forgot Password → ForgotPasswordForm
    └─→ Invalid Credentials → Show Error
```

## 🛠️ Customization

### Change Default Admin Password
Edit `Database.java` in `createTables()` method:
```java
pstmt.setString(3, hashPassword("your_new_password"));
```

### Modify Lockout Time
Edit `LoginForm.java`:
```java
db.lockUser(-1, 30); // Change to 30 minutes
```

### Add More User Fields
1. Add column to users table in `Database.java`
2. Update `getUserInfo()` method
3. Update UI forms

## 📊 Admin Dashboard Features

- View all registered users
- See admin status
- Check account lock status
- Access user creation dates
- Monitor user activity

## 🐛 Troubleshooting

### SQLite Driver Error
```
Error: No JDBC driver found
```
**Solution**: Download sqlite-jdbc jar and add to classpath

### Database Already Exists
```
Error: Database locked
```
**Solution**: Delete `users.db` file and restart application

### Login Form Not Appearing
```
Error: JFrame not visible
```
**Solution**: Ensure Java GUI libraries are installed

## 📚 Dependencies

- Java Standard Library
- SQLite JDBC Driver
- Swing (included with Java)

## 🚀 Future Enhancements

- [ ] Email notifications
- [ ] OAuth 2.0 integration
- [ ] Session management
- [ ] Database backups
- [ ] User roles and permissions
- [ ] Activity logging
- [ ] Two-step email verification
- [ ] Account recovery questions
- [ ] Password strength meter
- [ ] Last login tracking

## 📄 License

Open Source - Feel free to use and modify

## 👨‍💻 Author

Created for secure Java authentication demonstration

---

**Happy Coding! 🎉**

````
