plugins {
    `java-library`
}

dependencies {
    implementation(project(":bc:shared"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
