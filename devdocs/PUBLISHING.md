### Publishing to Local Maven Repository

> **Note**: our custom local Maven repository is located at `<project root>/.maven-publish-dev-repo`.

> **Note**: set **version** to "0.0.0-SNAPSHOT"

`./gradlew publishAllPublicationsToMavenLocalRepository`


### Publishing to Sonatype Central repository

> **Note**: When publishing a "Release" version to Sonatype, a PGP signature is required.
>
> See: https://central.sonatype.org/pages/working-with-pgp-signatures.html
        
                                                               
#### Credentials
                                 
In the `local.properties` file add the following properties:
```properties
sonatype.username=<your Sonatype username>
sonatype.password=<your Sonatype access token>
sonatype.profileID=<your Sonatype profile ID>
```

#### SNAPSHOT version

- Specify "x.y.z-SNAPSHOT" version in `build.gradle.kts` file.

- Publish  

`./gradlew publishAllPublicationsToMavenRepository`

- Check Uploaded Artifacts

https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/org/jetbrains/lets-plot/

- Use SNAPSHOT artifacts

Add snapshot repository to the `repositories` section of the `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
    // SNAPSHOTS
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
}
```

#### "Release" version

  a) Specify RELEASE or PRE-RELEASE (i.e. "x.y.z-alpha1", "x.y.z-rc1" etc.) version in `build.gradle.kts` file.

  b) Build and publish artifacts to a local build directory:

`./gradlew publishAllPublicationsToMavenRepository`

> Check all artifacts are published to the local directory:
>
> `<project root>/build/maven/artifacts`
>

  c) Package and upload all artifacts to the Sonatype Central repository:

`./gradlew uploadMavenArtifacts`

  d) Check artifacts are uploaded to the Sonatype Central repository and have the status "Validated":

https://central.sonatype.com/publishing/deployments

  e) Push the button "Publish"
