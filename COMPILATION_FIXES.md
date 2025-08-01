# Essentials Maven Compilation Fixes

## Issues Identified

### 1. **Maven Not Installed**
- The project requires Maven to compile but it's not installed on your system
- Error: `mvn : The term 'mvn' is not recognized`

### 2. **Java Version Compatibility**
- Project was configured for Java 1.6 (very old)
- You have Java 21 installed, which is incompatible
- Updated to Java 8 compatibility

### 3. **Outdated Maven Plugins**
- All Maven plugins were using very old versions
- Updated to modern, compatible versions

### 4. **Missing Repositories**
- Added Spigot and Sonatype repositories for better dependency resolution

## Solutions Applied

### ✅ Fixed POM File (`pom.xml`)
- Updated Java version from 1.6 to 8
- Updated all Maven plugin versions to latest stable releases
- Added missing repositories
- Updated Lombok to latest version
- Added proper encoding configuration

### ✅ Created Maven Installer (`install_maven.bat`)
- Automated script to download and install Maven
- Sets up environment variables automatically

## How to Fix the Compilation

### Step 1: Install Maven
Run the provided installer script:
```bash
install_maven.bat
```

Or manually install Maven:
1. Download from: https://maven.apache.org/download.cgi
2. Extract to a directory (e.g., `C:\Program Files\Apache\maven`)
3. Add to PATH: `C:\Program Files\Apache\maven\bin`

### Step 2: Verify Installation
After installing Maven, restart your command prompt and run:
```bash
mvn -version
```

### Step 3: Compile the Project
```bash
mvn clean compile
```

### Step 4: Build All Modules
```bash
mvn clean package
```

## Key Changes Made

### Java Version
- **Before**: Java 1.6 (`<source>1.6</source>`)
- **After**: Java 8 (`<source>8</source>`)

### Maven Plugin Updates
- `maven-compiler-plugin`: 3.1 → 3.11.0
- `maven-antrun-plugin`: 1.7 → 3.1.0
- `maven-dependency-plugin`: 2.8 → 3.6.1
- `maven-resources-plugin`: 2.6 → 3.3.1
- `maven-clean-plugin`: 2.5 → 3.3.2
- `maven-javadoc-plugin`: 2.9.1 → 3.6.3
- `maven-deploy-plugin`: 2.8.1 → 3.1.1

### Dependencies
- Updated Lombok from 1.12.2 to 1.18.30
- Added proper scope for Lombok (`provided`)

### Repositories Added
- Spigot repository for Bukkit dependencies
- Sonatype repository for additional dependencies

## Troubleshooting

### If Maven Still Not Found
1. Check if Maven is in your PATH
2. Try running: `echo %PATH%` to see if Maven directory is included
3. Restart command prompt after installation

### If Compilation Fails
1. Ensure all JAR files are present in the `lib/` directory
2. Check that `resources/alfheim-craftbukkit-1.7.2-FINAL.jar` exists
3. Verify Java 8 compatibility (you can install Java 8 alongside Java 21)

### If Dependencies Are Missing
The project uses system-scoped dependencies. Ensure these files exist:
- `lib/BOSEconomy.jar`
- `lib/iCo5.jar`
- `lib/iCo6.jar`
- `lib/MultiCurrency.jar`
- `lib/PermissionsBukkit.jar`
- `lib/PermissionsEx.jar`
- `lib/Privileges.jar`
- `lib/SimplyPerms.jar`
- `lib/zPermissions.jar`
- `resources/alfheim-craftbukkit-1.7.2-FINAL.jar`
- `resources/tools-1.5.0.jar`

## Expected Output

After successful compilation, you should see:
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
[INFO] Finished at: 2024-XX-XX
```

The compiled JAR files will be placed in the `jars/` directory.

## Notes

- This is an old Minecraft plugin project (Essentials for Bukkit)
- The project structure is from around 2013-2014
- Modern Java versions (9+) may have compatibility issues with some Bukkit APIs
- Consider using Java 8 for best compatibility with this legacy codebase 