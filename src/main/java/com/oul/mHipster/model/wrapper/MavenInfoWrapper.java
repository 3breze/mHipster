package com.oul.mHipster.model.wrapper;

import org.apache.maven.project.MavenProject;

public class MavenInfoWrapper {
    private String groupId;
    private String artifactId;
    private String name;
    private MavenProject mavenProject;

    public MavenInfoWrapper() {
    }

    public MavenInfoWrapper(MavenProject mavenProject) {
        this.groupId = mavenProject.getGroupId();
        this.artifactId = mavenProject.getArtifactId();
        this.name = String.join(".", groupId, artifactId);
        this.mavenProject = mavenProject;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }
}
