# http4j 🚀

A lightweight HTTP 1.1 server written in Java - perfect for serving static files, testing webhooks, or building your own minimal API server.

---

## ✨ Features

- 📁 Serve static files from any directory
- 🌐 Built-in routing for GET and POST
- 🧠 HEAD support
- 🧪 Clean keep-alive handling
- 🗂️ Directory listing support
- 📦 Minimal dependencies (only Log4j & Picocli)
- 🐞 Optional debug logging
- 🔧 Configurable port & static root

---

## ⚙️ Build

Make sure you have **Java 21** and **Maven** installed.

```bash
git clone https://github.com/AshutoshIWNL/http4j.git
cd http4j
mvn clean package
```

The JAR will be available in `target/http4j-1.0.jar`.

---

## 🚀 Usage

```bash
java -jar target/http4j-1.0.jar --debug=true --port 9090 --static-root /path/to/public
```

### 🛠️ Command Line Options

| Flag             | Description                              | Default     |
|------------------|------------------------------------------|-------------|
| `--port`         | Port to start the server on              | `8080`      |
| `--static-root`  | Directory to serve static files from     | *none*      |
| `--debug`        | Enables debug-level logging              | `false`     |

---

## 🔍 Examples

### Serve a local folder

```bash
java -jar target/http4j-1.0.jar --static-root ./public
```

Place files like `index.html`, `favicon.ico`, etc. in `./public`.

Access in browser:

```
http://localhost:8080/
```

### Basic API routing

Two routes are available by default:

```http
GET /hello           → "Hello from Http4j"
POST /echo           → echoes back the request body
```

Example:

```bash
curl http://localhost:8080/hello
curl -X POST http://localhost:8080/echo -d "Hi!"
```

---

## 🧪 Testing

Unit tests are written using JUnit 5.

Run tests with:

```bash
mvn test
```

---

## 🔒 Security

- Basic path traversal protection for static file serving
- Only `identity` transfer encoding supported (no chunked transfer)

---


## 👨‍💻 Author

Made with ❤️ by [Ashutosh Mishra](https://github.com/your-username)

---