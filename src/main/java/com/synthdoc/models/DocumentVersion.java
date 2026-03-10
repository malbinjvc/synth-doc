package com.synthdoc.models;

import java.time.Instant;

public class DocumentVersion {

    private int versionNumber;
    private Document snapshot;
    private Instant timestamp;
    private String changeDescription;

    public DocumentVersion() {
        this.timestamp = Instant.now();
    }

    public DocumentVersion(int versionNumber, Document snapshot, String changeDescription) {
        this.versionNumber = versionNumber;
        this.snapshot = snapshot;
        this.timestamp = Instant.now();
        this.changeDescription = changeDescription;
    }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public Document getSnapshot() { return snapshot; }
    public void setSnapshot(Document snapshot) { this.snapshot = snapshot; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}
