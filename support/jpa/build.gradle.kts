plugins {
    `java-library`
}

dependencies {
    implementation(project(":bc:shared"))
    implementation(libs.spring.boot.starter.data.jpa)

    testImplementation(libs.archunit)
    testImplementation(libs.spring.boot.starter.test)
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // QueryDSL for BaseJpaEntity
    api("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
}
