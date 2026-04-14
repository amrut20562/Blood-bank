🩸 Blood Bank Management System (Android App)
📌 Overview

The Blood Bank Management System is an Android-based mobile application designed to connect blood donors and recipients on a single platform. It helps manage blood donation, requests, and inventory efficiently, especially during medical emergencies.

The system provides a centralized and real-time solution to reduce delays in finding blood and improve communication between users.

🚀 Features
🔐 User Registration & Login
👤 Profile Management
🩸 Blood Donation Module
📢 Blood Request System (with emergency flag)
🔄 Request Status Tracking (Pending, Approved, Rejected, Completed)
🧬 Blood Compatibility Matching
📦 Real-time Blood Inventory Management
🛠️ Admin Dashboard for monitoring system activities
🏗️ System Architecture

The application follows a 3-tier architecture:

Presentation Layer: Android UI (Java + XML)
Application Layer: Business logic (authentication, matching, validation)
Data Layer: MySQL database (via JDBC connectivity)
🛠️ Tech Stack
Frontend: Java, XML (Android)
IDE: Android Studio
Backend: JDBC Connectivity
Database: MySQL
Platform: Android (API 24–34)
📂 Project Modules
Authentication Module – User login & registration
Profile Module – View/edit user details
Donor Module – Manage donor info & history
Request Module – Raise & track blood requests
Inventory Module – Manage blood stock
Admin Module – Control users, requests & data
⚙️ Installation & Setup
1. Clone the repository
git clone https://github.com/your-username/blood-bank-app.git
2. Open in Android Studio
Launch Android Studio
Select Open Project
Navigate to the project folder
3. Setup Database
Install MySQL
Create database and tables (users, donors, requests, inventory)
Update JDBC connection details in your code
4. Run the App
Connect an Android device or emulator
Click Run ▶️ in Android Studio
🧪 Testing
✅ Unit Testing for individual modules
✅ Integration Testing for system workflow
✅ Input validation for secure data handling
📊 Results
Efficient handling of blood donation and requests
Real-time inventory updates
Improved response time during emergencies
Secure and reliable system performance
🔮 Future Enhancements
📍 GPS-based donor search
🔔 Push notifications for urgent requests
☁️ Cloud database integration
🔐 OTP-based authentication
🌐 Web version of the system
🤝 Contribution

Contributions are welcome! Feel free to fork this repository and submit pull requests.

📚 References
Android Developer Documentation
MySQL Documentation
Java Documentation
Software Engineering Books (Sommerville, Pressman)
👨‍💻 Author

Amrut More
B.E. Student (SPPU)

⭐ Support

If you like this project, give it a ⭐ on GitHub!
