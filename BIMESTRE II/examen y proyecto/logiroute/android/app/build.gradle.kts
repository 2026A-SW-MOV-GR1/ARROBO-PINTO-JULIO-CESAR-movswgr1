import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

/*
 * Lee la clave de Google Maps y las demás
 * propiedades almacenadas en android/local.properties.
 */
val localProperties = Properties().apply {
    val localPropertiesFile =
        rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile
            .inputStream()
            .use { inputStream ->
                load(inputStream)
            }
    }
}

android {
    namespace = "ec.edu.epn.logiroute"

    compileSdk =
        flutter.compileSdkVersion

    ndkVersion =
        flutter.ndkVersion

    /*
     * Configuración Java para Android.
     */
    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId =
            "ec.edu.epn.logiroute"

        minSdk = 24

        targetSdk =
            flutter.targetSdkVersion

        versionCode =
            flutter.versionCode

        versionName =
            flutter.versionName

        /*
         * Envía la clave de local.properties
         * hacia AndroidManifest.xml.
         */
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty(
                "MAPS_API_KEY_APP2",
                ""
            )
    }

    buildTypes {
        release {
            /*
             * Para esta entrega académica se utiliza
             * temporalmente la firma de depuración.
             */
            signingConfig =
                signingConfigs.getByName("debug")
        }
    }
}

/*
 * Configuración moderna del compilador Kotlin.
 * Sustituye al bloque antiguo kotlinOptions.
 */
kotlin {
    compilerOptions {
        jvmTarget =
            org.jetbrains.kotlin.gradle.dsl
                .JvmTarget
                .JVM_17
    }
}

flutter {
    source = "../.."
}