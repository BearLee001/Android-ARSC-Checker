# ARSC Checker 

一个用于解析 Android 资源文件 `resources.arsc` 的纯 Kotlin 工具库。

`resources.arsc` 是 Android 应用中用于存储字符串、尺寸、颜色等预编译资源的核心文件。本项目旨在提供一个简单、直接的方式来解析其二进制结构，并以人类可读的形式展示出来，便于学习、调试或进行资源分析。

## ✨ 特性

- **纯 Kotlin 实现**: 无需任何原生依赖，跨平台友好。
- **零依赖**: 轻量级，不引入任何第三方库，易于集成。
- **清晰的结构化输出**: 将复杂的二进制结构映射为直观的 Kotlin `data class`。
- **命令行支持**: 提供开箱即用的命令行工具，可快速查看 `arsc` 文件内容。
- **设计清晰**: 解析逻辑严格遵循 Android 官方源码中的 `ResourceTypes.h` 定义，可靠性高。

## 🚀 如何开始

### 环境要求

- JDK 11 或更高版本
- Gradle (用于构建项目)

### 构建项目

克隆本仓库到本地，然后执行以下命令进行构建：

```bash
# 对于 macOS / Linux
./gradlew build

# 对于 Windows
gradlew.bat build
```

构建成功后，你可以在 `build/libs/` 目录下找到可执行的 JAR 文件。

## 💡 如何使用

你可以通过两种方式使用本项目：作为独立的**命令行工具**，或将其作为**库**集成到你自己的项目中。

### 1. 作为命令行工具

`CliChecker.kt` 是本项目的命令行入口。你可以通过以下方式运行它来解析一个 `arsc` 文件。

首先，请确保项目已成功构建。

```bash
# 假设构建出的 jar 名称为 ARSC_check_opensource-1.0-SNAPSHOT.jar
java -jar ARSC_check_opensource-1.0-SNAPSHOT.jar /path/to/your/resources.arsc
```

执行后，它会将 `resources.arsc` 文件的主要结构信息打印到控制台。

### 2. 作为库集成

如果你想在自己的代码中解析 `arsc` 文件，只需使用核心的 `ARSCParser` 类即可。
好处是你可以自由的打印你需要的 ARSC 的内容，比如 offset array...
下面是一个最简单的使用案例：

```kotlin
package com.tiktok.android.infra.arsc

import java.io.File
import java.nio.ByteBuffer

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Error: No ARSC file path provided.")
        println("Usage: java -jar your-jar-name.jar <path/to/file1.arsc> [path/to/file2.arsc] ...")
        return
    }

    resolveArscs(args.toList())
}

fun resolveArscs(arscPaths: List<String>) {
    arscPaths.forEach { arscPath ->
        println("\nProcessing: $arscPath")

        val file = File(arscPath)
        if (!file.exists()) {
            println("-> Failure: File not found.")
            return@forEach
        }

        try {
            val parser = ARSCParser(ByteBuffer.wrap(file.readBytes()))
            val arsc = parser.parse()
            println("-> Success: Parsed ${arsc.packages.size} package(s) with ${arsc.stringPool.strings.size} global strings.")
        } catch (e: Exception) {
            println("-> Failure: An error occurred during parsing.")
            println("   Reason: ${e.message}")
        }
    }
}

```

## 📂 项目结构

- `CliChecker.kt`: 项目的命令行入口。展示了如何使用 `ARSCParser` 的一个简单示例。
- `ARSCParser.kt`: **核心解析器**。负责读取 `resources.arsc` 的二进制数据并将其映射到 Kotlin 数据类。
- `ResourceTypes.kt` (或类似文件): 定义了 ARSC 文件结构对应的数据类，是 `ResourceTypes.h` 的 Kotlin 版本。

## 🏛️ 设计参考

本项目的解析逻辑严格遵循 Android 官方源码中的头文件定义，以确保解析的准确性。

核心参考文件：
[**platform/frameworks/base/+/main/include/androidfw/ResourceTypes.h**](https://android.googlesource.com/platform/frameworks/base/+/main/include/androidfw/ResourceTypes.h)
