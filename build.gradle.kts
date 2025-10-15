plugins {
    id("java")
    id("application")
}

group = "desastre"
version = "1.0"

val jadex_version = "4.0.267"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus.actoron.com/content/repositories/jadex/")
    }
}

dependencies {
    implementation("org.activecomponents.jadex:jadex-distribution-standard:${jadex_version}") {
        exclude(group = "org.activecomponents.jadex", module = "jadex-tools-runtimetools-web")
    }
}

application {
    mainClass.set("desastre.Main")
}