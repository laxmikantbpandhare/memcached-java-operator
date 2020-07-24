package io.fabric8.memcached.operator.memcached_types;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;

public class Memcached extends CustomResource {

    public MemcachedSpec getSpec() {
        return spec;
    }

    public void setSpec(MemcachedSpec spec) {
        this.spec = spec;
    }

    public MemcachedStatus getStatus() {
        return Status;
    }

    public void setStatus(MemcachedStatus status) {
        Status = status;
    }

    private MemcachedSpec spec;
    private MemcachedStatus Status;
}
