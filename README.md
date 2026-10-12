# FullForm Portal

A complete full-stack web application combining frontend and backend to collect text, images, and videos from users, store them in the local file system and database, and display them on demand with media playback and raw file inspection.

---

## 🌟 Features

1. **Comprehensive Input Collection**:
   - **Text Inputs**: Name, Email, Password, Phone, Website, Search Keywords, About/Bio, Age, Preferred Colors (favourite & hated), Skill level slider, Date of Birth, Call slot time, Graduation month, Week, Meeting datetime, Country, Languages (multi-select), Framework (datalist), Gender, Interests.
   - **Image Upload**: Photo / profile image (`JPEG`, `PNG`, `WebP`) with live preview thumbnail.
   - **Video Upload**: Intro / audition video (`MP4`, `WebM`, `MOV`) with live video preview player.
   - **Document / Resume**: PDF / Word document upload.

2. **Local File System Storage**:
   - Uploaded images are stored in `FullFormBackend/FullForm/uploads/images/`.
   - Uploaded videos are stored in `FullFormBackend/FullForm/uploads/videos/`.
   - Uploaded documents are stored in `FullFormBackend/FullForm/uploads/documents/`.
   - Complete submitted text records are saved as structured JSON files in `FullFormBackend/FullForm/uploads/submissions/submission_{id}.json`.

3. **On-Demand Display & Playback**:
   - **Instant Feedback**: Automatically displays newly submitted records upon form submission.
   - **Submissions Dashboard**: View all submissions in a sidebar with real-time search/filtering.
   - **Media Playback**:
     - Embedded HTML5 `<video controls>` player streaming video directly from local storage with byte-range seeking.
     - Embedded image gallery showing the saved photos.
   - **Local File Inspection**: Displays the exact local file paths and renders the raw local JSON file contents.
   - **Delete Action**: Removes submission from database and deletes associated local files.

4. **Unified Frontend & Backend**:
   - The Spring Boot backend serves the frontend directly from `http://localhost:8080/`.
   - Standalone frontend files in `FullFormFrontend/` can also be run via Live Server or static file servers with CORS support enabled.

---

## 🚀 How to Run

### Prerequisites
- **Java 21**
- **PostgreSQL 16** (Database: `audition_db`, User: `postgres`, Password: `root` or configured in `application.properties`)

### Step 1: Start Backend Application
Navigate to the backend directory and run:
```powershell
cd FullFormBackend\FullForm
.\mvnw.cmd spring-boot:run
```
Or run the packaged jar:
```powershell
java -jar target\FullForm-0.0.1-SNAPSHOT.jar
```

### Step 2: Access the Application
Open your web browser and navigate to:
```
http://localhost:8080/
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Serves the web frontend UI |
| `POST` | `/api/v1/submissions` | Multipart endpoint to upload text, images, videos & documents |
| `GET` | `/api/v1/submissions` | Retrieve all submissions (newest first) |
| `GET` | `/api/v1/submissions/{id}` | Retrieve a specific submission with file links & details |
| `GET` | `/api/v1/submissions/{id}/local-file` | Retrieve raw content of the saved local JSON file |
| `DELETE` | `/api/v1/submissions/{id}` | Delete submission and delete files from local storage |
| `GET` | `/uploads/{folder}/{filename}` | Stream/serve local image, video, document, or JSON files |
