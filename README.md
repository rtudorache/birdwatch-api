# Bird Watch Application

The application requires a running PostgreSQL database. This is managed using Docker Compose.

1.  **Navigate to the Backend Directory:**
    Open your terminal and go to the directory containing the `docker-compose.yml` file (usually at the root of the backend service project).
2. **Start the Database:**
   Run the following command to start the PostgreSQL container in detached mode:

    ```bash
    docker-compose up -d
    ```
3. **Verify the container is running:**
    
    ```bash
    docker ps
    ```
4. **Build the App:**

       ```bash
   ./gradlew clean build
    ```

5. **Start the App:**

    ```bash
    ./gradlew bootRun
    ```
   
    The app will start on:

   http://localhost:8080/api/v1



# Bird Watch Eclipse RCP Update Site Installation Guide
This section explains how to install or update the Bird Watch Eclipse RCP application feature
using the provided update site ZIP archive.

---

## Prerequisites

- An existing Eclipse IDE or Eclipse RCP application installed.
- Java runtime matching the application's requirements (e.g., Java 11).
- The update site ZIP file (e.g., spirent-birdwatch-update-site.zip) available locally.

---

## Installation / Update Steps

1. **Start your Eclipse IDE or Eclipse RCP application.**

2. **Open the Install New Software dialog:**
    - Go to the menu: **Help > Install New Software...**

3. **Add the update site from the ZIP archive:**
    - Click on the **Add...** button.
    - In the dialog that appears, click on **Archive...**.
    - Browse to the location of the downloaded update site ZIP file (`spirent-birdwatch-update-site.zip`).
    - Select the file and click **OK**.

4. **Select the Birdwatch feature to install or update:**
    - In the list of available software, find the Birdwatch feature or category.
    - Check the checkbox next to the Birdwatch feature.
    - Click **Next**.

5. **Review the installation details and click Next.**

6. **Accept the license agreement:**
    - Read and accept the terms.
    - Click **Finish**.

7. **Wait for the installation to complete:**
    - Eclipse will download, verify, and install the feature from the ZIP archive.
    - If you see any security warnings about unsigned content, confirm to proceed.

8. **Restart Eclipse when prompted:**
    - It is recommended to restart Eclipse after installation/update for changes to take effect.
    - Click **Yes** when asked to restart.
