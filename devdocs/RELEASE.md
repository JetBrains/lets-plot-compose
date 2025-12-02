## Releasing the Project

### Make Version

##### 1. Update Documentation

- Update CHANGELOG.md
- Update the "Dependencies" section in README.md

##### 2. Set the Release Version

Update the 'version' property in the root "build.gradle.kts":                                     

- Remove the "-SNAPSHOT" qualifier 
- For pre-release add "-alpha1", "-rc1" etc. qualifier 

##### 3. Build and Release Artifacts to Sonatype Central Repository

- `./gradlew clean`

> Note: make sure the `lets-plot-compose/build` is clean (e.g., no old artifacts in `lets-plot-compose/build/maven`).

- `./gradlew build`
- `./gradlew publishAllPublicationsToMavenRepository`
- `./gradlew uploadMavenArtifacts`

Go to the Sonatype Central Repository deployments page:

https://central.sonatype.com/publishing/deployments

Check all artifacts were uploaded and validated, then push the "Publish" button.

> **Note**: For more details see [PUBLISHING.md](PUBLISHING.md).

##### 4. Prepare for the Next Dev Cycle

- Increment the version and add _"-SNAPSHOT"_ qualifier (the 'version' property in the root 'build.gradle.kts')
- Push all to git and add the version git tag:
    - `git add --all && git commit -m "Release vX.X.X" && git push`
    - `git tag vX.X.X && git push --tags`

### Add the GitHub Release:
 
 * Open the link: https://github.com/JetBrains/lets-plot-compose/releases/new
 * Fill `Tag version` and `Release title` with the released version: "vX.X.X"
 * Fill the description field - copy from the CHANGELOG.md

### Update Dependant Projects 

- Update the version of the `lets-plot-compose` dependency in the [lets-plot-compose-demos](https://github.com/JetBrains/lets-plot-compose-demos) project.
