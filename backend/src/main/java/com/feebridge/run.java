//package com.feebridge;
//
//@Controller
//public class HomeController {
//
//    @GetMapping(value = {
//            "/",
//            "/{path:[^\\.]*}",
//            "/**/{path:[^\\.]*}"
//    })
//    public String redirect() {
//        return "forward:/index.html";
//    }

//}
//
//
//tasks.register("installFrontend", Exec) {
//    workingDir "../frontend"
//    commandLine "npm", "install"
//}
//
//tasks.register("buildFrontend", Exec) {
//    dependsOn "installFrontend"
//    workingDir "../frontend"
//    commandLine "npm", "run", "build"
//}
//
//tasks.register("copyFrontend", Copy) {
//    dependsOn "buildFrontend"
//    from("../frontend/dist")
//    into("src/main/resources/static")
//}
//
//processResources.dependsOn("copyFrontend")
//
//
//add this to your build .gradle
//
//build the backend using
//
//gradlew build
//
//it should create a jar file
//
//That file may be in a build package(folder)
//
//Something something-SNAPSHOT.jar
//
//
//When you have done these
//
//Deploy to Render
//-Push your project to GitHub.
//Create a new Web Service on Render.
//Connect your repository.
//Use these settings for :
//
//Build Command
//gradlew build
//
//Run Command
//java -jar build/libs/your-feebridge.jar
//
//Run the app first
//
//gradlew bootRun
//
//
// ./gradlew bootRun