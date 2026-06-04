# 🚀 Visual Compiler Studio

A web-based interactive tool to visualize how a compiler works step-by-step, including:

- 🔍 **Lexical Analysis** (Tokenization)
- 🧠 **Semantic Analysis** (Symbol Table)
- ⚙️ **Execution Engine** (Runtime Simulation)
- 📊 **Execution Trace** (Step-by-step timeline)

## 🌟 Highlights

* Built a complete compiler pipeline: Lexer → Parser → Semantic → Executor
* Supports Java-like syntax preprocessing
* Real-time token visualization
* Symbol table generation and memory tracking
* Execution trace timeline
* Monaco Editor integration
* Syntax, semantic, and type validation
* IDE-style resizable workspace

## 📸 Screenshots

### Code Editor

<img width="1920" height="1080" alt="editor-output png" src="https://github.com/user-attachments/assets/014ff3cf-d944-49af-a0de-f924f15762b5" />


### Token Visualization

<img width="1920" height="1080" alt="tokens png" src="https://github.com/user-attachments/assets/ed828d16-0f80-4db7-b0e0-c60696a46eba" />


### Symbol Table

<img width="1920" height="1080" alt="symbol-table png" src="https://github.com/user-attachments/assets/767c9b1a-44d4-40f0-8fb2-5dbed51dae97" />


### Execution Trace

<img width="1920" height="1080" alt="execution-trace png" src="https://github.com/user-attachments/assets/c4b2940c-1ae0-4a13-bca5-92d597163c88" />


### Error Detection

<img width="1920" height="1080" alt="type-error png" src="https://github.com/user-attachments/assets/3b1cfec3-b64e-411f-b7da-5d6838560d86" />


## ⚠️ Known Limitations

* Not a full Java compiler
* No object-oriented programming support
* No Scanner input support
* No array support
* No inheritance/polymorphism
* Designed primarily for compiler visualization and learning

## 🚀 Future Scope

* Function and method support
* Array handling
* String operations
* AST (Abstract Syntax Tree) visualization
* Optimization phase visualization
* Intermediate code generation
* Machine code generation simulation


## ✨ Features

- **Monaco Editor**: A full VS Code-like editing experience.
- **Resizable Output Panel**: A draggable divider to manage your workspace (similar to the VS Code terminal).
- **Tabbed Visualization**:
  - **Tokens**: View every lexeme categorized by type and line number.
  - **Symbol Table**: Track variables, their types, and their final memory states.
  - **Execution Trace**: A chronological replay of every program action.
- **Real-time Execution**: Immediate feedback on code changes.
- **Clean Output**: Console results contain ONLY user-defined `print` results.

## ☕ Java-like Syntax Support

- This is **not** a full Java compiler.
- It supports beginner-level Java-like wrapper syntax.
- Boilerplate wrappers like `public class Main` and `public static void main(String[] args)` are preprocessed and automatically stripped.
- `System.out.println(x)` and `System.out.print(x)` are both preprocessed and treated as standard `print` statements.
- **Supported Syntax**: `int`, `float`, assignment, arithmetic, `if`, `while`, `for`, `++`, `--`, and `print`/`System.out.println`.
- Full advanced Java features such as classes, objects, arrays, methods, imports, `Scanner`, and string concatenation are **not** fully supported.

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
**Visual Compiler Studio** - *Visualizing the magic behind the code.*
