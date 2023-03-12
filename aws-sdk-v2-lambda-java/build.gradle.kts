plugins {
    id("java")
}

group = "com.mdadzerkin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("com.amazonaws:aws-lambda-java-runtime-interface-client:2.2.0")
    implementation("software.amazon.awssdk:dynamodb:2.20.21")
    implementation("software.amazon.awssdk:dynamodb-enhanced:2.20.21")
    implementation("software.amazon.awssdk:sqs:2.20.21")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

task<Jar>("uberJar") {
    archiveFileName.set("lambdaV2.jar")
    manifest.attributes["Main-Class"] = "com.amazonaws.services.lambda.runtime.api.client.AWSLambda"
    val dependencies = configurations.runtimeClasspath.get()
        .map(::zipTree)
    from(dependencies)
    from("$projectDir/build/classes/java/main")
    from("$projectDir/build/resources/main")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

task<Exec>("nativeImage") {
    doFirst {
        commandLine(
            "docker",
            "run",
            "-v", "${projectDir}/build/libs:/app",
            "--rm", "native-image",
            "-jar", "lambdaV2.jar",
            "--no-fallback",
            "--verbose",
            "-J-Xmx4g",
            "-H:ClassInitialization=org.slf4j:build_time"
        ).workingDir(projectDir)
    }
}

tasks.getByName("nativeImage") {
    dependsOn("build", "uberJar")
}

task<Zip>("awsLambda") {
    archiveFileName.set("lambdaV2.zip")
    from("${projectDir}/build/resources/main/bootstrap") {
        // permissions: 0777
        fileMode = 0b111101101
    }
    from("${projectDir}/build/libs/lambdaV2") {
        // permissions: 0777
        fileMode = 0b111101101
    }
}

tasks.getByName("uberJar") {
    dependsOn("build", "processResources")
}

tasks.getByName("awsLambda") {
    dependsOn("nativeImage")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
