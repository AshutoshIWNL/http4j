# http4j 🚀
A lightweight, plug-and-play HTTP/1.1 server written in Java - ideal for serving static files, mocking APIs, and rapid prototyping without writing a single line of backend code.

---

## ✨ Features

- 📁 **Serve static files** from any directory
- 🔀 **Built-in routing** for GET, POST, HEAD
- ⚙️ **No-code routing** via `routes.json`
- 🧠 **Persistent connections** with clean keep-alive handling
- 🗂️ **Directory listing UI** (auto-index for folders)
- 🪵 **Minimal dependencies** (`Log4j`, `Jackson`, `Picocli`)
- 🔧 **CLI configurable** (port, static root, route file)
- 📦 **Packaged as a single runnable JAR**
- 🧪 **Built-in unit tests** for request parser

---

## 🚀 Getting Started

### 1️⃣ Download or Build

```bash
git clone https://github.com/AshutoshIWNL/http4j.git
cd http4j
mvn clean package
```

### 2️⃣ Run the Server

```bash
java -jar target/http4j.jar --debug=true --port 9090 --static-root /path/to/public
```

Optionally, pass a route config file:

```bash
java -jar target/http4j.jar --debug=true --port 9090 --static-root ./public --routes ./routes.json
```

---

## 📜 Example: routes.json

```json
{
  "routes": [
    {
      "method": "GET",
      "path": "/hello",
      "response": {
        "status": 200,
        "contentType": "text/plain",
        "body": "Hello from config!"
      }
    },
    {
      "method": "POST",
      "path": "/echo",
      "response": {
        "status": 200,
        "contentType": "application/json",
        "body": "{\"message\":\"Echoed from config\"}"
      }
    }
  ]
}
```

---

## 🧪 API Examples

```bash
curl http://localhost:9090/hello
# → Hello from config!

curl -X POST http://localhost:9090/echo -d "data=123"
# → {"message":"Echoed from config"}
```

---

## 🗂️ Directory Listing

Start the server with `--static-root` pointing to any folder. Visit `http://localhost:9090/` in your browser and browse files and subfolders with a clean auto-generated UI.

---

## 📌 CLI Options

| Flag            | Description                     | Default        |
|-----------------|---------------------------------|----------------|
| `--port`        | Port to bind the server on      | `8080`         |
| `--static-root` | Path to serve static files from | `.`            |
| `--routes`      | Path to `routes.json` config    | './routes.json' |
| `--debug`       | Enable debug logging            | false          |

---

## 🔧 Dev Notes

- Fully supports core aspects of HTTP/1.1 spec
- HEAD requests automatically skip response body
- Handles malformed headers, transfer encoding, and keep-alive
- Easily embeddable or extendable as a Java library

---

## 👨‍💻 Author

Built with ❤️ by **Ashutosh Mishra**  
GitHub: [@AshutoshIWNL](https://github.com/AshutoshIWNL)

---

## 📄 License

MIT