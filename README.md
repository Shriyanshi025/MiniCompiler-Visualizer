# 🚀 Mini Compiler Visualizer

A web-based interactive tool to visualize how a compiler works step-by-step, including:

- 🔍 **Lexical Analysis** (Tokenization)
- 🧠 **Semantic Analysis** (Symbol Table)
- ⚙️ **Execution Engine** (Runtime Simulation)
- 📊 **Execution Trace** (Step-by-step timeline)

## ✨ Features

- **Monaco Editor**: A full VS Code-like editing experience.
- **Resizable Output Panel**: A draggable divider to manage your workspace (similar to the VS Code terminal).
- **Tabbed Visualization**:
  - **Tokens**: View every lexeme categorized by type and line number.
  - **Symbol Table**: Track variables, their types, and their final memory states.
  - **Execution Trace**: A chronological replay of every program action.
- **Real-time Execution**: Immediate feedback on code changes.
- **Clean Output**: Console results contain ONLY user-defined `print` results.

## 🧪 Sample Input

```c
int a = 10;
a = a + 1;
print a;
```

## ✅ Output
```text
11
```

## 🧩 Architecture

**Lexer** → **Parser** → **Semantic** → **Executor** → **WebServer** → **UI**

## ⚙️ How to Run

1. **Compile the source**:
   ```powershell
   javac -d build src/*.java
   ```

2. **Start the WebServer**:
   ```powershell
   java -cp build WebServer
   ```

3. **Access the IDE**:
   Open `index.html` in your browser or navigate to `http://localhost:8080` (if serving locally).

---
**MiniCompiler** - *Visualizing the magic behind the code.*
