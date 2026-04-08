subprojects {
    afterEvaluate {
        tasks.withType<JavaExec> {
            val envFile = project.file(".env")
            if (envFile.exists()) {
                envFile.readLines()
                    .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
                    .forEach { line ->
                        val idx = line.indexOf("=")
                        val key = line.substring(0, idx).trim()
                        val value = line.substring(idx + 1).trim()
                        environment(key, value)
                    }
            }
        }
    }
}
