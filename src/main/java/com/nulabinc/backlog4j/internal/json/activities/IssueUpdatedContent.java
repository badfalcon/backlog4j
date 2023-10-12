package com.nulabinc.backlog4j.internal.json.activities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nulabinc.backlog4j.*;
import com.nulabinc.backlog4j.internal.json.AttachmentJSONImpl;
import com.nulabinc.backlog4j.internal.json.ChangeJSONImpl;
import com.nulabinc.backlog4j.internal.json.CommentJSONImpl;
import com.nulabinc.backlog4j.internal.json.SharedFileJSONImpl;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @author nulab-inc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueUpdatedContent extends Content {

    private long id;
    @JsonProperty("key_id")
    private long keyId;
    private String summary;
    private String description;
    @JsonDeserialize(as = CommentJSONImpl.class)
    private Comment comment;
    @JsonDeserialize(as = ChangeJSONImpl[].class)
    private Change[] changes;
    @JsonDeserialize(as = AttachmentJSONImpl[].class)
    private Attachment[] attachments;
    @JsonDeserialize(as = SharedFileJSONImpl[].class)
    @JsonProperty("shared_files")
    private SharedFile[] sharedFiles;

    public long getId() {
        return this.id;
    }

    public long getKeyId() {
        return this.keyId;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Comment getComment() {
        return this.comment;
    }

    public List<Change> getChanges() {
        return Arrays.asList(this.changes);
    }

    public List<Attachment> getAttachments() {
        return Arrays.asList(attachments);
    }

    public List<SharedFile> getSharedFiles() {
        return Arrays.asList(sharedFiles);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        IssueUpdatedContent rhs = (IssueUpdatedContent) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.keyId, rhs.keyId)
                .append(this.summary, rhs.summary)
                .append(this.description, rhs.description)
                .append(this.comment, rhs.comment)
                .append(this.changes, rhs.changes)
                .append(this.attachments, rhs.attachments)
                .append(this.sharedFiles, rhs.sharedFiles)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(keyId)
                .append(summary)
                .append(description)
                .append(comment)
                .append(changes)
                .append(attachments)
                .append(sharedFiles)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("keyId", keyId)
                .append("summary", summary)
                .append("description", description)
                .append("comment", comment)
                .append("changes", changes)
                .append("attachments", attachments)
                .append("sharedFiles", sharedFiles)
                .toString();
    }
}
